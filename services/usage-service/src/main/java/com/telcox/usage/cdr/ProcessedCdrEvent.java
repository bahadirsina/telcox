package com.telcox.usage.cdr;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usage_service_processed_event")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProcessedCdrEvent {
    @Id private UUID id;
    private UUID eventId;
    private String eventType;
    private String sourceService;
    private UUID aggregateId;
    private Instant processedAt;
    private String status;
    private Instant createdAt;

    static ProcessedCdrEvent success(UUID eventId, CdrEvent event, Instant now) {
        ProcessedCdrEvent processed = new ProcessedCdrEvent();
        processed.id = UUID.randomUUID();
        processed.eventId = eventId;
        processed.eventType = "cdr.usage.recorded";
        processed.sourceService = "cdr";
        processed.aggregateId = event.subscriptionId();
        processed.processedAt = now;
        processed.status = "PROCESSED";
        processed.createdAt = now;
        return processed;
    }
}
