package com.telcox.subscription.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "mnp_port_request")
public class MnpPortRequest {

    @Id
    private UUID id;

    @Column(name = "subscription_id")
    private UUID subscriptionId;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false, length = 32)
    private String msisdn;

    @Column(name = "donor_operator", nullable = false, length = 80)
    private String donorOperator;

    @Column(name = "recipient_operator", nullable = false, length = 80)
    private String recipientOperator;

    @Column(name = "plan_code", nullable = false, length = 80)
    private String planCode;

    @Column(name = "sim_iccid", length = 64)
    private String simIccid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private MnpStatus status;

    @Column(name = "rejection_reason", length = 500)
    private String rejectionReason;

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    protected MnpPortRequest() {
    }

    public MnpPortRequest(Subscription subscription, String donorOperator, String recipientOperator) {
        LocalDateTime now = LocalDateTime.now();
        this.id = UUID.randomUUID();
        this.subscriptionId = subscription.getId();
        this.customerId = subscription.getCustomerId();
        this.msisdn = subscription.getMsisdn();
        this.donorOperator = donorOperator;
        this.recipientOperator = recipientOperator;
        this.planCode = subscription.getPlanCode();
        this.simIccid = subscription.getSimIccid();
        this.status = MnpStatus.REQUESTED;
        this.correlationId = subscription.getCorrelationId();
        this.createdAt = now;
        this.updatedAt = now;
    }

    @PreUpdate
    void refreshUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }

    public void advance(MnpStatus nextStatus, String reason) {
        if (!isAllowedTransition(status, nextStatus)) {
            throw new IllegalStateException("Invalid MNP transition: " + status + " -> " + nextStatus);
        }
        status = nextStatus;
        if (nextStatus == MnpStatus.REJECTED) {
            rejectionReason = reason;
        }
        if (nextStatus == MnpStatus.COMPLETED || nextStatus == MnpStatus.REJECTED || nextStatus == MnpStatus.CANCELLED) {
            completedAt = LocalDateTime.now();
        }
    }

    private static boolean isAllowedTransition(MnpStatus current, MnpStatus next) {
        return switch (current) {
            case REQUESTED -> next == MnpStatus.VALIDATING || next == MnpStatus.CANCELLED || next == MnpStatus.REJECTED;
            case VALIDATING -> next == MnpStatus.PORTING || next == MnpStatus.REJECTED || next == MnpStatus.CANCELLED;
            case PORTING -> next == MnpStatus.COMPLETED || next == MnpStatus.REJECTED || next == MnpStatus.CANCELLED;
            case COMPLETED, REJECTED, CANCELLED -> false;
        };
    }

    public UUID getId() {
        return id;
    }

    public UUID getSubscriptionId() {
        return subscriptionId;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public String getDonorOperator() {
        return donorOperator;
    }

    public String getRecipientOperator() {
        return recipientOperator;
    }

    public String getPlanCode() {
        return planCode;
    }

    public String getSimIccid() {
        return simIccid;
    }

    public MnpStatus getStatus() {
        return status;
    }

    public String getRejectionReason() {
        return rejectionReason;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public LocalDateTime getUpdatedAt() {
        return updatedAt;
    }

    public LocalDateTime getCompletedAt() {
        return completedAt;
    }
}
