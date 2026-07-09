package com.telcox.usage.event;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usage_service_outbox_event")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsageOutboxEvent {
    @Id private UUID id;
    private UUID aggregateId;
    private String aggregateType;
    private String eventType;
    private String payloadJson;
    private String status;
    private Instant createdAt;
    private Instant publishedAt;
    private String correlationId;
    private int schemaVersion;

    static UsageOutboxEvent pending(
            UUID aggregateId,
            String aggregateType,
            String eventType,
            String payloadJson,
            String correlationId,
            Instant createdAt,
            int schemaVersion) {
        UsageOutboxEvent event = new UsageOutboxEvent();
        event.id = UUID.randomUUID();
        event.aggregateId = aggregateId;
        event.aggregateType = aggregateType;
        event.eventType = eventType;
        event.payloadJson = payloadJson;
        event.status = "PENDING";
        event.createdAt = createdAt;
        event.correlationId = correlationId;
        event.schemaVersion = schemaVersion;
        return event;
    }
}
