package com.telcox.common.event;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.Objects;
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
        Objects.requireNonNull(eventId, "eventId must not be null");
        requireText(type, "type");
        Objects.requireNonNull(aggregateId, "aggregateId must not be null");
        requireText(correlationId, "correlationId");
        if (schemaVersion < 1) {
            throw new IllegalArgumentException("schemaVersion must be greater than zero");
        }
        metadata = metadata == null ? Map.of() : Map.copyOf(metadata);
    }

    public static <T> EventEnvelope<T> withGeneratedId(
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
        return new EventEnvelope<>(
                UUID.randomUUID(),
                type,
                aggregateId,
                correlationId,
                schemaVersion,
                sourceService,
                aggregateType,
                occurredAt,
                payload,
                metadata
        );
    }

    private static void requireText(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " must not be blank");
        }
    }
}
