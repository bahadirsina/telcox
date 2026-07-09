package com.telcox.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "customer_service_processed_event")
public class CustomerProcessedEvent {

    @Id
    private UUID id;

    @Column(name = "event_id", nullable = false, unique = true)
    private UUID eventId;

    @Column(name = "event_type", length = 100)
    private String eventType;

    @Column(name = "source_service", length = 100)
    private String sourceService;

    @Column(name = "aggregate_id")
    private UUID aggregateId;

    @Column(name = "processed_at")
    private LocalDateTime processedAt;

    @Column(length = 50)
    private String status;

    @Column(name = "error_message", length = 500)
    private String errorMessage;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected CustomerProcessedEvent() {
    }

    public CustomerProcessedEvent(UUID eventId, String eventType, String sourceService, UUID aggregateId) {
        this.id = UUID.randomUUID();
        this.eventId = eventId;
        this.eventType = eventType;
        this.sourceService = sourceService;
        this.aggregateId = aggregateId;
        this.status = "PROCESSING";
        this.createdAt = LocalDateTime.now();
    }

    public void markProcessed() {
        this.status = "PROCESSED";
        this.processedAt = LocalDateTime.now();
    }
}
