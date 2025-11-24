package com.example.banking.service;

import com.example.banking.domain.Customer;
import com.example.banking.repository.CustomerRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;

@Service
public class CustomerService {

    private final CustomerRepository repo;
    private final AuditService auditService;

    public CustomerService(CustomerRepository repo, AuditService auditService) {
        this.repo = repo;
        this.auditService = auditService;
    }

    public List<Customer> findAll() { return repo.findAll(); }

    public Customer create(Customer c) {
        Customer saved = repo.save(c);
        try {
            auditService.record(
                    "system",
                    "CREATE_CUSTOMER",
                    "CUSTOMER",
                    String.valueOf(saved.getId()),
                    Map.of(
                            "name", saved.getFullName(),
                            "email", saved.getEmail() == null ? "" : saved.getEmail()
                    )
            );
        } catch (Exception e) {
            // don't fail the create flow if auditing fails
            System.err.println("Audit record failed: " + e.getMessage());
        }
        return saved;
    }
}
