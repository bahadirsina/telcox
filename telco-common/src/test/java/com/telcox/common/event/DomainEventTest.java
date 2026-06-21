package com.telcox.common.event;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.OffsetDateTime;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class DomainEventTest {

    @Test
    void keepsEventTypeContractAndProvidesTypeAlias() {
        DomainEvent event = new TestDomainEvent();

        assertThat(event.eventType()).isEqualTo("customer-created");
        assertThat(event.type()).isEqualTo("customer-created");
    }

    @Test
    void defaultsSchemaVersionToOne() {
        assertThat(new TestDomainEvent().schemaVersion()).isEqualTo(1);
    }

    private record TestDomainEvent(
            UUID eventId,
            String eventType,
            String sourceService,
            UUID aggregateId,
            String aggregateType,
            OffsetDateTime occurredAt,
            String correlationId
    ) implements DomainEvent {
        private TestDomainEvent() {
            this(
                    UUID.randomUUID(),
                    "customer-created",
                    "customer-service",
                    UUID.randomUUID(),
                    "CUSTOMER",
                    OffsetDateTime.parse("2026-06-22T00:00:00Z"),
                    "corr-1"
            );
        }
    }
}
