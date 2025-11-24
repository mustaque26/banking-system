package com.example.banking.service;

import com.example.banking.domain.Account;
import com.example.banking.domain.Customer;
import com.example.banking.repository.AccountRepository;
import com.example.banking.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

@Service
public class AccountService {

    private final AccountRepository accountRepo;
    private final CustomerRepository customerRepo;

    public AccountService(AccountRepository accountRepo, CustomerRepository customerRepo) {
        this.accountRepo = accountRepo;
        this.customerRepo = customerRepo;
    }

    public List<Account> findByCustomerId(Long customerId) {
        Customer c = customerRepo.findById(customerId).orElseThrow();
        return accountRepo.findByCustomer(c);
    }

    @Transactional
    public Account create(Long customerId, Account account) {
        Customer c = customerRepo.findById(customerId).orElseThrow();
        account.setCustomer(c);
        if (account.getBalance() == null) {
            account.setBalance(BigDecimal.ZERO);
        }
        return accountRepo.save(account);
    }

    public BigDecimal getBalance(Long accountId) {
        Account a = accountRepo.findById(accountId).orElseThrow();
        return a.getBalance();
    }
}
