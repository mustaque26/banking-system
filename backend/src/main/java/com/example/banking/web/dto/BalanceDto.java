package com.example.banking.web.dto;

import java.math.BigDecimal;

public record BalanceDto(Long accountId, BigDecimal balance) {}

