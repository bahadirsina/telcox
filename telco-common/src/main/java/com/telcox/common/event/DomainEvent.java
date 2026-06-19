package com.telcox.common.event;

import java.time.OffsetDateTime;
import java.util.UUID;

/**
 * Tum domain event'lerin uymasi gereken minimum sozlesme.
 * Outbox pattern + idempotent consumer icin gerekli alanlari icerir.
 */
public interface DomainEvent {

    /** Event icin global unique id; consumer tarafindaki PROCESSED_EVENT.eventId. */
    UUID eventId();

    /** Event tipi (customer-created, order-confirmed vb.). */
    String type();

    /** Geriye uyumluluk icin eski eventType accessor'i. */
    default String eventType() {
        return type();
    }

    /** Olusturan servis adi (correlation icin). */
    String sourceService();

    /** Event'in ait oldugu aggregate kimligi. */
    UUID aggregateId();

    /** Aggregate tipi (CUSTOMER, ORDER, SUBSCRIPTION vb.). */
    String aggregateType();

    /** Event olusturma zamani (UTC). */
    OffsetDateTime occurredAt();

    /** Distributed tracing icin gateway tarafindan enjekte edilen id. */
    String correlationId();

    /** Event semasinin major versiyonu. */
    int schemaVersion();
}
