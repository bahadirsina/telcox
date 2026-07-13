package com.telcox.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * BILL-01 / FR-21: Musteri basina bir fatura hesabi. ER: db.sql -> BILLING_SERVICE_BILLING_ACCOUNT
 */
@Entity
@Table(name = "billing_service_billing_account")
public class BillingAccount {

    @Id
    private UUID id;

    /** logical ref -> customer-service.CUSTOMER.id */
    @Column(name = "customer_id", nullable = false, unique = true)
    private UUID customerId;

    @Column(length = 3)
    private String currency;

    @Column(nullable = false, precision = 19, scale = 4)
    private BigDecimal balance;

    @Column(name = "credit_limit", precision = 19, scale = 4)
    private BigDecimal creditLimit;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private BillingAccountStatus status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected BillingAccount() {
    }

    public BillingAccount(UUID customerId, String currency, BigDecimal creditLimit) {
        LocalDateTime now = LocalDateTime.now();
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.currency = currency;
        this.balance = BigDecimal.ZERO;
        this.creditLimit = creditLimit;
        this.status = BillingAccountStatus.ACTIVE;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public void applyCharge(BigDecimal amount) {
        this.balance = this.balance.add(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public void applyPayment(BigDecimal amount) {
        this.balance = this.balance.subtract(amount);
        this.updatedAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getCurrency() {
        return currency;
    }

    public BigDecimal getBalance() {
        return balance;
    }

    public BigDecimal getCreditLimit() {
        return creditLimit;
    }

    public BillingAccountStatus getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }
}
