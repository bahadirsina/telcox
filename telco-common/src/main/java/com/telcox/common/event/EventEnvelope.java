package com.telcox.common.event;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;

/**
 * Kafka uzerinden tasinan tum domain event'leri icin standart envelope.
 */
public record EventEnvelope<T>(
        UUID eventId,
        String type,
        UUID aggregateId,
        String correlationId,
        int schemaVersion,
        String sourceService,
        String aggregateType,
        OffsetDateTime occurredAt,
        T payload,
        Map<String, String> metadata
) {
    public EventEnvelope {
        if (eventId == null) {
            eventId = UUID.randomUUID();
        }
        if (schemaVersion < 1) {
            throw new IllegalArgumentException("schemaVersion must be greater than zero");
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }
}
