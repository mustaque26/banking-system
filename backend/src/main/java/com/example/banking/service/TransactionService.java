package com.example.banking.service;

import com.example.banking.domain.Account;
import com.example.banking.domain.Transaction;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.TransactionRepository;
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

    public TransactionService(TransactionRepository txnRepo, AccountRepository accountRepo, AuditService auditService) {
        this.txnRepo = txnRepo;
        this.accountRepo = accountRepo;
        this.auditService = auditService;
    }

    public List<Transaction> findByAccountId(Long accountId) {
        Account a = accountRepo.findById(accountId).orElseThrow();
        return txnRepo.findByAccount(a);
    }

    @Transactional
    public Transaction post(Long accountId, String type, BigDecimal amount, String description) {
        Account a = accountRepo.findById(accountId).orElseThrow();

        BigDecimal prev = a.getBalance() == null ? BigDecimal.ZERO : a.getBalance();
        BigDecimal next;
        if ("DEBIT".equalsIgnoreCase(type)) {
            next = prev.subtract(amount);
            a.setBalance(next);
        } else {
            next = prev.add(amount);
            a.setBalance(next);
        }
        accountRepo.save(a);

        Transaction t = Transaction.of(
                a,
                type.toUpperCase(),
                amount,
                description,
                UUID.randomUUID().toString(),
                OffsetDateTime.now()
        );

        Transaction saved = txnRepo.save(t);

        try {
            auditService.record(
                    "system",
                    "POST_TRANSACTION",
                    "TRANSACTION",
                    String.valueOf(saved.getId()),
                    Map.of(
                            "accountId", String.valueOf(a.getId()),
                            "type", type.toUpperCase(),
                            "amount", amount.toPlainString(),
                            "prevBalance", prev.toPlainString(),
                            "newBalance", next.toPlainString(),
                            "reference", saved.getReference()
                    )
            );
        } catch (Exception e) {
            // ignore auditing failures
            e.printStackTrace();
        }

        return saved;
    }
}
