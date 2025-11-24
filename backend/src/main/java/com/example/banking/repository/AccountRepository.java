package com.example.banking.repository;

import com.example.banking.domain.Account;
import com.example.banking.domain.Customer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface AccountRepository extends JpaRepository<Account, Long> {
    List<Account> findByCustomer(Customer customer);
}
