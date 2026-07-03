package com.telcox.billing.projection.subscription;

import jakarta.persistence.*;
import java.time.Instant;
import java.util.UUID;

@Entity
@Table(name = "subscription_projection")
public class SubscriptionProjection {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "subscription_id", nullable = false, unique = true)
    private UUID subscriptionId;
    @Column(name = "customer_id", nullable = false)
    private UUID customerId;
    @Column(name = "tariff_id", nullable = false)
    private UUID tariffId;
    @Column(name = "subscription_status", nullable = false)
    private String subscriptionStatus;
    @Column(name = "billing_cycle_day")
    private Integer billingCycleDay;
    @Column(name = "started_at", nullable = false)
    private Instant startedAt;
    @Column(name = "updated_at", nullable = false)
    private Instant updatedAt;

    protected SubscriptionProjection() {}

    public SubscriptionProjection(UUID subscriptionId, UUID customerId, UUID tariffId,
                                   String subscriptionStatus, Integer billingCycleDay,
                                   Instant startedAt, Instant updatedAt) {
        this.subscriptionId = subscriptionId; this.customerId = customerId; this.tariffId = tariffId;
        this.subscriptionStatus = subscriptionStatus; this.billingCycleDay = billingCycleDay;
        this.startedAt = startedAt; this.updatedAt = updatedAt;
    }

    public void applyStatusUpdate(String status, Instant updatedAt) {
        this.subscriptionStatus = status; this.updatedAt = updatedAt;
    }

    public Long getId() { return id; }
    public UUID getSubscriptionId() { return subscriptionId; }
    public UUID getCustomerId() { return customerId; }
    public UUID getTariffId() { return tariffId; }
    public String getSubscriptionStatus() { return subscriptionStatus; }
    public Integer getBillingCycleDay() { return billingCycleDay; }
    public Instant getStartedAt() { return startedAt; }
    public Instant getUpdatedAt() { return updatedAt; }
}
