package com.example.banking.service;

import com.example.banking.domain.Account;
import com.example.banking.domain.Transaction;
import com.example.banking.domain.GLEntry;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
import com.example.banking.repository.GLEntryRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository txnRepo;
    private final AccountRepository accountRepo;
    private final AuditService auditService;
    private final GLEntryRepository glRepo;

    public TransactionService(TransactionRepository txnRepo, AccountRepository accountRepo, AuditService auditService, GLEntryRepository glRepo) {
        this.txnRepo = txnRepo;
        this.accountRepo = accountRepo;
        this.auditService = auditService;
        this.glRepo = glRepo;
    }

    public List<Transaction> findByAccountId(Long accountId) {
        Account a = accountRepo.findById(accountId).orElseThrow();
        return txnRepo.findByAccount(a);
    }

    @Transactional
    public Transaction create(Long accountId, String type, BigDecimal amount, String description) {
        Account a = accountRepo.findById(accountId).orElseThrow();

        Transaction t = Transaction.of(
                a,
                type.toUpperCase(),
                amount,
                description,
                UUID.randomUUID().toString(),
                OffsetDateTime.now()
        );

        Transaction saved = txnRepo.save(t);

        // perform basic AML/sanctions screening
        boolean suspicious = amlCheck(saved);
        if (suspicious) {
            saved.setSuspicious(true);
            saved.setComplianceFlag("AML");
        }
        txnRepo.save(saved);

        auditService.record(
                "system",
                "CREATE_TRANSACTION",
                "TRANSACTION",
                String.valueOf(saved.getId()),
                Map.of(
                        "accountId", String.valueOf(a.getId()),
                        "type", saved.getType(),
                        "amount", saved.getAmount().toPlainString(),
                        "status", saved.getStatus()
                )
        );

        return saved;
    }

    @Transactional
    public Transaction approve(Long txnId) {
        Transaction t = txnRepo.findById(txnId).orElseThrow();
        if (!"PENDING".equalsIgnoreCase(t.getStatus())) throw new IllegalStateException("Txn not pending");

        // risk/compliance checks
        if (t.getAmount().compareTo(new BigDecimal("10000")) > 0) {
            // mark for CTR / high-value reporting
            t.setComplianceFlag("CTR");
        }

        // calculate charges
        BigDecimal charge = calculateCharge(t.getAmount());
        t.setCharge(charge);
        t.setStatus("APPROVED");
        txnRepo.save(t);

        auditService.record(
                "system",
                "APPROVE_TRANSACTION",
                "TRANSACTION",
                String.valueOf(t.getId()),
                Map.of(
                        "charge", charge.toPlainString(),
                        "complianceFlag", t.getComplianceFlag() == null ? "" : t.getComplianceFlag()
                )
        );

        // post to GL and update balances
        postToGL(t);

        return t;
    }

    @Transactional
    public Transaction reject(Long txnId, String reason) {
        Transaction t = txnRepo.findById(txnId).orElseThrow();
        if (!"PENDING".equalsIgnoreCase(t.getStatus())) throw new IllegalStateException("Txn not pending");
        t.setStatus("REJECTED");
        txnRepo.save(t);

        auditService.record(
                "system",
                "REJECT_TRANSACTION",
                "TRANSACTION",
                String.valueOf(t.getId()),
                Map.of("reason", reason == null ? "" : reason)
        );
        return t;
    }

    private boolean amlCheck(Transaction t) {
        // placeholder: flag txns above threshold or with suspicious descriptions
        if (t.getAmount().compareTo(new BigDecimal("5000")) > 0) return true;
        if (t.getDescription() != null && t.getDescription().toLowerCase().contains("suspicious")) return true;
        return false;
    }

    private BigDecimal calculateCharge(BigDecimal amount) {
        // simple tiered charge matrix
        if (amount.compareTo(new BigDecimal("100")) <= 0) return BigDecimal.ZERO;
        if (amount.compareTo(new BigDecimal("1000")) <= 0) return new BigDecimal("1.00");
        if (amount.compareTo(new BigDecimal("5000")) <= 0) return new BigDecimal("5.00");
        return new BigDecimal("10.00");
    }

    @Transactional
    protected void postToGL(Transaction t) {
        // create GL entries: debit/credit and fee
        Account a = accountRepo.findById(t.getAccount().getId()).orElseThrow();
        BigDecimal amount = t.getAmount();
        BigDecimal charge = t.getCharge() == null ? BigDecimal.ZERO : t.getCharge();

        // debit or credit logic: if DEBIT, decrease account (customer pays out)
        if ("DEBIT".equalsIgnoreCase(t.getType())) {
            // customer account: credit GL (customer balance is asset decrease)
            GLEntry e1 = GLEntry.builder()
                    .accountNumber(a.getAccountNumber())
                    .glAccount("CUSTOMER_ASSET")
                    .amount(amount)
                    .side("CREDIT")
                    .entryTime(OffsetDateTime.now())
                    .reference(t.getReference())
                    .build();

            // fees
            if (charge.compareTo(BigDecimal.ZERO) > 0) {
                GLEntry feeDebit = GLEntry.builder()
                        .accountNumber(a.getAccountNumber())
                        .glAccount("FEES")
                        .amount(charge)
                        .side("DEBIT")
                        .entryTime(OffsetDateTime.now())
                        .reference(t.getReference())
                        .build();
                glRepo.save(feeDebit);
            }

            glRepo.save(e1);

            // update account balance
            BigDecimal prev = a.getBalance() == null ? BigDecimal.ZERO : a.getBalance();
            BigDecimal next = prev.subtract(amount).subtract(charge);
            a.setBalance(next);
            accountRepo.save(a);
        } else {
            // CREDIT: increase account
            GLEntry e1 = GLEntry.builder()
                    .accountNumber(a.getAccountNumber())
                    .glAccount("CUSTOMER_ASSET")
                    .amount(amount)
                    .side("DEBIT")
                    .entryTime(OffsetDateTime.now())
                    .reference(t.getReference())
                    .build();

            glRepo.save(e1);
            if (charge.compareTo(BigDecimal.ZERO) > 0) {
                GLEntry feeDebit = GLEntry.builder()
                        .accountNumber(a.getAccountNumber())
                        .glAccount("FEES")
                        .amount(charge)
                        .side("DEBIT")
                        .entryTime(OffsetDateTime.now())
                        .reference(t.getReference())
                        .build();
                glRepo.save(feeDebit);
                // deduct fee from account after credit
                BigDecimal prev = a.getBalance() == null ? BigDecimal.ZERO : a.getBalance();
                BigDecimal next = prev.add(amount).subtract(charge);
                a.setBalance(next);
                accountRepo.save(a);
            } else {
                BigDecimal prev = a.getBalance() == null ? BigDecimal.ZERO : a.getBalance();
                BigDecimal next = prev.add(amount);
                a.setBalance(next);
                accountRepo.save(a);
            }
        }

        // mark txn as posted
        t.setStatus("POSTED");
        txnRepo.save(t);

        auditService.record(
                "system",
                "POST_TXN_TO_GL",
                "TRANSACTION",
                String.valueOf(t.getId()),
                Map.of(
                        "amount", amount.toPlainString(),
                        "charge", charge.toPlainString(),
                        "newBalance", a.getBalance() == null ? "0" : a.getBalance().toPlainString()
                )
        );
    }
}
