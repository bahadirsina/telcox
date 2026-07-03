package com.telcox.subscription.projection.payment;

import jakarta.persistence.*;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "payment_projection")
public class PaymentProjection {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "payment_id", nullable = false, unique = true)
    private UUID paymentId;
    @Column(name = "order_id")
    private UUID orderId;
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    @Column(name = "amount", nullable = false)
    private BigDecimal amount;
    @Column(name = "currency", nullable = false)
    private String currency;
    @Column(name = "payment_status", nullable = false)
    private String paymentStatus;
    @Column(name = "paid_at")
    private Instant paidAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected PaymentProjection() {}

    public PaymentProjection(UUID paymentId, UUID orderId, UUID customerId, BigDecimal amount,
                              String currency, String paymentStatus, Instant paidAt, Instant updatedAt) {
        this.paymentId = paymentId; this.orderId = orderId; this.customerId = customerId;
        this.amount = amount; this.currency = currency; this.paymentStatus = paymentStatus;
        this.paidAt = paidAt; this.updatedAt = updatedAt;
    }

    public void applyStatusUpdate(String status, Instant paidAt, Instant updatedAt) {
        this.paymentStatus = status; this.paidAt = paidAt; this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public UUID getPaymentId() { return paymentId; }
    public UUID getOrderId() { return orderId; }
    public UUID getCustomerId() { return customerId; }
    public BigDecimal getAmount() { return amount; }
    public String getCurrency() { return currency; }
    public String getPaymentStatus() { return paymentStatus; }
    public Instant getPaidAt() { return paidAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}