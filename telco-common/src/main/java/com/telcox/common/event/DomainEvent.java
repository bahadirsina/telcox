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

    /** Event'in turu (CustomerRegistered, OrderConfirmed vb.). */
    String eventType();

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
}
