package com.example.banking.web.dto;

import com.example.banking.domain.Transaction;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

public class TransactionDto {
    public Long id;
    public String reference;
    public String type;
    public BigDecimal amount;
    public OffsetDateTime txnTime;
    public String description;
    public String status;
    public BigDecimal charge;
    public String complianceFlag;
    public Boolean suspicious;

    public static TransactionDto from(Transaction t) {
        TransactionDto d = new TransactionDto();
        d.id = t.getId();
        d.reference = t.getReference();
        d.type = t.getType();
        d.amount = t.getAmount();
        d.txnTime = t.getTxnTime();
        d.description = t.getDescription();
        d.status = t.getStatus();
        d.charge = t.getCharge();
        d.complianceFlag = t.getComplianceFlag();
        d.suspicious = t.getSuspicious();
        return d;
    }
}
