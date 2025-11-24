package com.example.banking.domain;

import jakarta.persistence.*;
import lombok.*;

import java.math.BigDecimal;
import java.time.OffsetDateTime;

@Entity
@Getter @Setter
@NoArgsConstructor @AllArgsConstructor
@Builder
public class Transaction {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    private String reference;
    private String type; // CREDIT / DEBIT

    @ManyToOne(fetch = FetchType.LAZY)
    private Account account;

    private BigDecimal amount;
    private OffsetDateTime txnTime;
    private String description;

    // lifecycle fields
    private String status; // PENDING, APPROVED, REJECTED, POSTED
    private BigDecimal charge;
    private String complianceFlag; // e.g., AML, CTR
    private Boolean suspicious;

    // explicit accessors
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getReference() { return reference; }
    public void setReference(String reference) { this.reference = reference; }
    public String getType() { return type; }
    public void setType(String type) { this.type = type; }
    public Account getAccount() { return account; }
    public void setAccount(Account account) { this.account = account; }
    public BigDecimal getAmount() { return amount; }
    public void setAmount(BigDecimal amount) { this.amount = amount; }
    public OffsetDateTime getTxnTime() { return txnTime; }
    public void setTxnTime(OffsetDateTime txnTime) { this.txnTime = txnTime; }
    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }
    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
    public BigDecimal getCharge() { return charge; }
    public void setCharge(BigDecimal charge) { this.charge = charge; }
    public String getComplianceFlag() { return complianceFlag; }
    public void setComplianceFlag(String complianceFlag) { this.complianceFlag = complianceFlag; }
    public Boolean getSuspicious() { return suspicious; }
    public void setSuspicious(Boolean suspicious) { this.suspicious = suspicious; }

    public static Transaction of(Account account, String type, BigDecimal amount, String description, String reference, OffsetDateTime txnTime) {
        Transaction t = new Transaction();
        t.setAccount(account);
        t.setType(type);
        t.setAmount(amount);
        t.setDescription(description);
        t.setReference(reference);
        t.setTxnTime(txnTime);
        t.setStatus("PENDING");
        t.setCharge(BigDecimal.ZERO);
        t.setComplianceFlag(null);
        t.setSuspicious(false);
        return t;
    }
}
