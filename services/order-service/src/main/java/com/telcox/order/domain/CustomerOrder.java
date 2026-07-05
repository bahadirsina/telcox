package com.telcox.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name = "customer_order")
public class CustomerOrder {

    @Id
    private UUID id;

    @Column(name = "order_number", nullable = false, unique = true, length = 40)
    private String orderNumber;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Enumerated(EnumType.STRING)
    @Column(name = "order_type", nullable = false, length = 40)
    private OrderType orderType;

    @Column(name = "plan_code", length = 80)
    private String planCode;

    @Column(name = "addon_codes")
    private String addonCodes;

    @Column(name = "msisdn", length = 32)
    private String msisdn;

    @Column(name = "sim_iccid", length = 64)
    private String simIccid;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OrderStatus status;

    @Column(name = "cancellation_reason", length = 500)
    private String cancellationReason;

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(name = "completed_at")
    private LocalDateTime completedAt;

    protected CustomerOrder() {
    }

    private CustomerOrder(UUID id, String orderNumber, UUID customerId, OrderType orderType, String planCode,
                          List<String> addonCodes, String msisdn, String simIccid, String correlationId) {
        LocalDateTime now = LocalDateTime.now();
        this.id = id;
        this.orderNumber = orderNumber;
        this.customerId = customerId;
        this.orderType = orderType;
        this.planCode = planCode;
        this.addonCodes = encodeAddons(addonCodes);
        this.msisdn = msisdn;
        this.simIccid = simIccid;
        this.status = OrderStatus.PAYMENT_REQUESTED;
        this.correlationId = correlationId;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static CustomerOrder create(UUID customerId, OrderType orderType, String planCode, List<String> addonCodes,
                                       String msisdn, String simIccid, String correlationId) {
        return new CustomerOrder(
                UUID.randomUUID(),
                "ORD-" + LocalDateTime.now().getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase(),
                customerId,
                orderType,
                planCode,
                addonCodes,
                msisdn,
                simIccid,
                correlationId
        );
    }

    @PreUpdate
    void refreshUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }

    public void markPaymentConfirmed() {
        status = OrderStatus.PAYMENT_CONFIRMED;
    }

    public void markActivationRequested() {
        status = OrderStatus.ACTIVATION_REQUESTED;
    }

    public void markCompleted() {
        status = OrderStatus.COMPLETED;
        completedAt = LocalDateTime.now();
    }

    public void markFailed(String reason) {
        status = OrderStatus.FAILED;
        cancellationReason = reason;
        completedAt = LocalDateTime.now();
    }

    public void markCompensating(String reason) {
        status = OrderStatus.COMPENSATING;
        cancellationReason = reason;
    }

    public void markCancelled() {
        status = OrderStatus.CANCELLED;
        completedAt = LocalDateTime.now();
    }

    public boolean isTerminal() {
        return status == OrderStatus.COMPLETED || status == OrderStatus.CANCELLED || status == OrderStatus.FAILED;
    }

    public List<String> addonCodeList() {
        if (addonCodes == null || addonCodes.isBlank()) {
            return List.of();
        }
        return List.of(addonCodes.split(","));
    }

    private static String encodeAddons(List<String> addonCodes) {
        if (addonCodes == null || addonCodes.isEmpty()) {
            return null;
        }
        return String.join(",", addonCodes.stream().filter(code -> code != null && !code.isBlank()).toList());
    }

    public UUID getId() {
        return id;
    }

    public String getOrderNumber() {
        return orderNumber;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public OrderType getOrderType() {
        return orderType;
    }

    public String getPlanCode() {
        return planCode;
    }

    public String getMsisdn() {
        return msisdn;
    }

    public String getSimIccid() {
        return simIccid;
    }

    public OrderStatus getStatus() {
        return status;
    }

    public String getCancellationReason() {
        return cancellationReason;
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
