package com.example.banking.web;

import com.example.banking.domain.Transaction;
import com.example.banking.web.dto.TransactionDto;
import com.example.banking.service.TransactionService;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/transactions")
public class TransactionController {

    private final TransactionService service;

    public TransactionController(TransactionService service) {
        this.service = service;
    }

    @GetMapping("/byAccount/{accountId}")
    public List<TransactionDto> byAccount(@PathVariable Long accountId) {
        return service.findByAccountId(accountId).stream()
                .map(TransactionDto::from)
                .collect(Collectors.toList());
    }

    public static class PostTxnRequest { public String type; public BigDecimal amount; public String description; }

    @PostMapping("/post/{accountId}")
    public TransactionDto post(@PathVariable Long accountId,
                            @RequestBody PostTxnRequest req) {
        Transaction t = service.create(accountId, req.type, req.amount, req.description);
        return TransactionDto.from(t);
    }

    @PostMapping("/approve/{txnId}")
    public TransactionDto approve(@PathVariable Long txnId) {
        Transaction t = service.approve(txnId);
        return TransactionDto.from(t);
    }

    @PostMapping("/reject/{txnId}")
    public TransactionDto reject(@PathVariable Long txnId, @RequestBody(required = false) String reason) {
        Transaction t = service.reject(txnId, reason);
        return TransactionDto.from(t);
    }
}
