package com.telcox.subscription.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "subscription")
public class Subscription {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(name = "order_id", unique = true)
    private UUID orderId;

    @Column(nullable = false, unique = true, length = 32)
    private String msisdn;

    @Column(name = "sim_iccid", length = 64)
    private String simIccid;

    @Column(name = "plan_code", nullable = false, length = 80)
    private String planCode;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private SubscriptionStatus status;

    @Column(name = "status_reason", length = 500)
    private String statusReason;

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    @Column(name = "activated_at")
    private LocalDateTime activatedAt;

    @Column(name = "suspended_at")
    private LocalDateTime suspendedAt;

    @Column(name = "terminated_at")
    private LocalDateTime terminatedAt;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @OneToMany(mappedBy = "subscription", cascade = CascadeType.ALL, orphanRemoval = false)
    private List<SubscriptionAddon> addons = new ArrayList<>();

    protected Subscription() {
    }

    private Subscription(UUID customerId, UUID orderId, String msisdn, String simIccid, String planCode,
                         SubscriptionStatus status, String correlationId) {
        LocalDateTime now = LocalDateTime.now();
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.orderId = orderId;
        this.msisdn = msisdn;
        this.simIccid = simIccid;
        this.planCode = planCode;
        this.status = status;
        this.correlationId = correlationId;
        this.createdAt = now;
        this.updatedAt = now;
        if (status == SubscriptionStatus.ACTIVE) {
            this.activatedAt = now;
        }
    }

    public static Subscription active(UUID customerId, UUID orderId, String msisdn, String simIccid, String planCode,
                                      String correlationId) {
        return new Subscription(customerId, orderId, msisdn, simIccid, planCode, SubscriptionStatus.ACTIVE, correlationId);
    }

    public static Subscription pendingPortIn(UUID customerId, String msisdn, String simIccid, String planCode,
                                             String correlationId) {
        return new Subscription(customerId, null, msisdn, simIccid, planCode, SubscriptionStatus.PENDING_PORT_IN, correlationId);
    }

    @PreUpdate
    void refreshUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }

    public void suspend(String reason) {
        requireStatus(SubscriptionStatus.ACTIVE, "Only active subscriptions can be suspended");
        status = SubscriptionStatus.SUSPENDED;
        statusReason = reason;
        suspendedAt = LocalDateTime.now();
    }

    public void reactivate(String reason) {
        requireStatus(SubscriptionStatus.SUSPENDED, "Only suspended subscriptions can be reactivated");
        status = SubscriptionStatus.ACTIVE;
        statusReason = reason;
    }

    public void changePlan(String planCode, String reason) {
        if (status != SubscriptionStatus.ACTIVE && status != SubscriptionStatus.SUSPENDED) {
            throw new IllegalStateException("Only active or suspended subscriptions can change plans");
        }
        this.planCode = planCode;
        this.statusReason = reason;
    }

    public void terminate(String reason) {
        if (status == SubscriptionStatus.TERMINATED) {
            throw new IllegalStateException("Subscription is already terminated");
        }
        status = SubscriptionStatus.TERMINATED;
        statusReason = reason;
        terminatedAt = LocalDateTime.now();
    }

    public void completePortIn() {
        requireStatus(SubscriptionStatus.PENDING_PORT_IN, "Only pending port-in subscriptions can be completed");
        status = SubscriptionStatus.ACTIVE;
        activatedAt = LocalDateTime.now();
    }

    public SubscriptionAddon addAddon(String addonCode) {
        SubscriptionAddon addon = new SubscriptionAddon(this, addonCode);
        addons.add(addon);
        return addon;
    }

    private void requireStatus(SubscriptionStatus expected, String message) {
        if (status != expected) {
            throw new IllegalStateException(message);
        }
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public String getSimIccid() {
        return simIccid;
    }

    public String getPlanCode() {
        return planCode;
    }

    public SubscriptionStatus getStatus() {
        return status;
    }

    public String getStatusReason() {
        return statusReason;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public LocalDateTime getActivatedAt() {
        return activatedAt;
    }

    public LocalDateTime getSuspendedAt() {
        return suspendedAt;
    }

    public LocalDateTime getTerminatedAt() {
        return terminatedAt;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public List<SubscriptionAddon> getAddons() {
        return addons;
    }
}
