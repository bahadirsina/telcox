package com.telcox.billing.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "billing_service_outbox_event")
public class BillingOutboxEvent {

    @Id
    private UUID id;

    @Column(name = "aggregate_id")
    private UUID aggregateId;

    @Column(name = "aggregate_type", length = 100)
    private String aggregateType;

    @Column(name = "event_type", nullable = false, length = 100)
    private String eventType;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "payload_json", nullable = false, columnDefinition = "jsonb")
    private Map<String, Object> payloadJson;

    @Column(nullable = false, length = 50)
    private String status;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "published_at")
    private LocalDateTime publishedAt;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "schema_version", nullable = false)
    private int schemaVersion;

    protected BillingOutboxEvent() {
    }

    public BillingOutboxEvent(UUID aggregateId, String aggregateType, String eventType,
                               Map<String, Object> payloadJson, String correlationId) {
        this.id = UUID.randomUUID();
        this.aggregateId = aggregateId;
        this.aggregateType = aggregateType;
        this.eventType = eventType;
        this.payloadJson = payloadJson;
        this.status = "NEW";
        this.createdAt = LocalDateTime.now();
        this.correlationId = correlationId;
        this.schemaVersion = 1;
    }
}
