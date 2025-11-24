package com.example.banking.service;

import com.example.banking.domain.Customer;
import com.example.banking.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class CustomerService {

    private final CustomerRepository repo;

    public CustomerService(CustomerRepository repo) {
        this.repo = repo;
    }

    public List<Customer> findAll() { return repo.findAll(); }

    public Customer create(Customer c) { return repo.save(c); }
}
