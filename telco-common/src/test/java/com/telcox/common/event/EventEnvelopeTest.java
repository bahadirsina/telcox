package com.telcox.common.event;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.time.OffsetDateTime;
import java.util.Map;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class EventEnvelopeTest {

    @Test
    void requiresEventId() {
        assertThatThrownBy(() -> envelope(null, "customer-created", UUID.randomUUID(), "corr-1", 1))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("eventId must not be null");
    }

    @Test
    void requiresType() {
        assertThatThrownBy(() -> envelope(UUID.randomUUID(), " ", UUID.randomUUID(), "corr-1", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("type must not be blank");
    }

    @Test
    void requiresAggregateId() {
        assertThatThrownBy(() -> envelope(UUID.randomUUID(), "customer-created", null, "corr-1", 1))
                .isInstanceOf(NullPointerException.class)
                .hasMessage("aggregateId must not be null");
    }

    @Test
    void requiresCorrelationId() {
        assertThatThrownBy(() -> envelope(UUID.randomUUID(), "customer-created", UUID.randomUUID(), "", 1))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("correlationId must not be blank");
    }

    @Test
    void requiresPositiveSchemaVersion() {
        assertThatThrownBy(() -> envelope(UUID.randomUUID(), "customer-created", UUID.randomUUID(), "corr-1", 0))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("schemaVersion must be greater than zero");
    }

    @Test
    void factoryGeneratesEventIdExplicitly() {
        EventEnvelope<Map<String, String>> envelope = EventEnvelope.withGeneratedId(
                "customer-created",
                UUID.randomUUID(),
                "corr-1",
                1,
                "customer-service",
                "CUSTOMER",
                OffsetDateTime.parse("2026-06-22T00:00:00Z"),
                Map.of("customerId", "CUST-1"),
                Map.of("traceId", "trace-1")
        );

        assertThat(envelope.eventId()).isNotNull();
        assertThat(envelope.schemaVersion()).isEqualTo(1);
        assertThat(envelope.type()).isEqualTo("customer-created");
    }

    private static EventEnvelope<Map<String, String>> envelope(
            UUID eventId,
            String type,
            UUID aggregateId,
            String correlationId,
            int schemaVersion
    ) {
        return new EventEnvelope<>(
                eventId,
                type,
                aggregateId,
                correlationId,
                schemaVersion,
                "customer-service",
                "CUSTOMER",
                OffsetDateTime.parse("2026-06-22T00:00:00Z"),
                Map.of("customerId", "CUST-1"),
                null
        );
    }
}
