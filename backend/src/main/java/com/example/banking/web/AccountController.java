package com.example.banking.web;

import com.example.banking.domain.Account;
import com.example.banking.web.dto.AccountDto;
import com.example.banking.service.AccountService;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/accounts")
public class AccountController {

    private final AccountService service;

    public AccountController(AccountService service) {
        this.service = service;
    }

    @GetMapping("/byCustomer/{customerId}")
    public List<AccountDto> byCustomer(@PathVariable Long customerId) {
        return service.findByCustomerId(customerId).stream()
                .map(AccountDto::from)
                .collect(Collectors.toList());
    }

    @PostMapping("/forCustomer/{customerId}")
    public AccountDto create(@PathVariable Long customerId,
                          @RequestBody Account account) {
        Account created = service.create(customerId, account);
        return AccountDto.from(created);
    }

    @GetMapping("/{accountId}/balance")
    public com.example.banking.web.dto.BalanceDto balance(@PathVariable Long accountId) {
        BigDecimal bal = service.getBalance(accountId);
        return new com.example.banking.web.dto.BalanceDto(accountId, bal);
    }
}
