package com.example.banking.web.dto;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public record TransactionDto(Long id, String reference, String type, Long accountId, BigDecimal amount, OffsetDateTime txnTime, String description) {

    public static TransactionDto from(com.example.banking.domain.Transaction t) {
        return new TransactionDto(
                t.getId(),
                t.getReference(),
                t.getType(),
                t.getAccount() != null ? t.getAccount().getId() : null,
                t.getAmount(),
                t.getTxnTime(),
                t.getDescription()
        );
    }
}

