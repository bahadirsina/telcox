package com.telcox.order.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "order_saga_history")
public class OrderSagaHistory {

    @Id
    private UUID id;

    @Column(name = "order_id", nullable = false)
    private UUID orderId;

    @Column(name = "step_name", nullable = false, length = 80)
    private String stepName;

    @Enumerated(EnumType.STRING)
    @Column(name = "step_status", nullable = false, length = 40)
    private SagaStepStatus stepStatus;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @Column(length = 500)
    private String message;

    @Column(name = "occurred_at", nullable = false)
    private LocalDateTime occurredAt;

    protected OrderSagaHistory() {
    }

    public OrderSagaHistory(UUID orderId, String stepName, SagaStepStatus stepStatus, String eventType, String message) {
        this.id = UUID.randomUUID();
        this.orderId = orderId;
        this.stepName = stepName;
        this.stepStatus = stepStatus;
        this.eventType = eventType;
        this.message = message;
        this.occurredAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getOrderId() {
        return orderId;
    }

    public String getStepName() {
        return stepName;
    }

    public SagaStepStatus getStepStatus() {
        return stepStatus;
    }

    public String getEventType() {
        return eventType;
    }

    public String getMessage() {
        return message;
    }

    public LocalDateTime getOccurredAt() {
        return occurredAt;
    }
}
