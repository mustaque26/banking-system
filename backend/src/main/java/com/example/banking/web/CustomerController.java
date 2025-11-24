package com.example.banking.web;

import com.example.banking.domain.Customer;
import com.example.banking.service.CustomerService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/customers")
public class CustomerController {

    private final CustomerService service;

    public CustomerController(CustomerService service) {
        this.service = service;
    }

    @GetMapping
    public List<Customer> all() {
        return service.findAll();
    }

    @PostMapping
    public Customer create(@RequestBody Customer customer) {
        return service.create(customer);
    }
}
