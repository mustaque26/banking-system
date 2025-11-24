package com.example.banking.web.dto;

import java.math.BigDecimal;

public record AccountDto(Long id, String accountNumber, Long customerId, String currency, BigDecimal balance, String status) {

    public static AccountDto from(com.example.banking.domain.Account a) {
        return new AccountDto(
                a.getId(),
                a.getAccountNumber(),
                a.getCustomer() != null ? a.getCustomer().getId() : null,
                a.getCurrency(),
                a.getBalance(),
                a.getStatus()
        );
    }
}

