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
import java.util.UUID;

@Service
public class TransactionService {

    private final TransactionRepository txnRepo;
    private final AccountRepository accountRepo;

    public TransactionService(TransactionRepository txnRepo, AccountRepository accountRepo) {
        this.txnRepo = txnRepo;
        this.accountRepo = accountRepo;
    }

    public List<Transaction> findByAccountId(Long accountId) {
        Account a = accountRepo.findById(accountId).orElseThrow();
        return txnRepo.findByAccount(a);
    }

    @Transactional
    public Transaction post(Long accountId, String type, BigDecimal amount, String description) {
        Account a = accountRepo.findById(accountId).orElseThrow();

        if ("DEBIT".equalsIgnoreCase(type)) {
            a.setBalance(a.getBalance().subtract(amount));
        } else {
            a.setBalance(a.getBalance().add(amount));
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

        return txnRepo.save(t);
    }
}
