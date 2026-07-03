package com.telcox.subscription.projection.payment;

import com.telcox.common.cache.CacheKeyPrefix;
import com.telcox.common.cache.CacheService;
import com.telcox.common.event.EventEnvelope;
import com.telcox.common.event.ProcessedEventGuard;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.UUID;

@Component
public class PaymentProjectionEventListener {

    private static final String PROCESSED_TABLE = "subscription_service_processed_event";

    private final PaymentProjectionRepository repository;
    private final CacheService cacheService;
    private final ProcessedEventGuard guard;

    public PaymentProjectionEventListener(PaymentProjectionRepository repository,
                                           CacheService cacheService, ProcessedEventGuard guard) {
        this.repository = repository;
        this.cacheService = cacheService;
        this.guard = guard;
    }

    @KafkaListener(
        topics = "${topics.payment-captured:telcox.payment.payment-captured.v1}",
        groupId = "subscription-service-payment-projection"
    )
    @Transactional
    public void onPaymentEvent(EventEnvelope<PaymentPayload> event) {
        boolean isNew = guard.tryBeginProcessing(PROCESSED_TABLE, event.eventId(),
                event.type(), event.sourceService(), event.aggregateId());
        if (!isNew) return;

        PaymentPayload p = event.payload();
        var existing = repository.findByPaymentId(p.paymentId());
        if (existing.isPresent()) {
            existing.get().applyStatusUpdate(p.paymentStatus(), p.paidAt(), Instant.now());
            repository.save(existing.get());
        } else {
            repository.save(new PaymentProjection(p.paymentId(), p.orderId(), p.customerId(),
                    p.amount(), p.currency(), p.paymentStatus(), p.paidAt(), Instant.now()));
        }

        cacheService.evict(CacheKeyPrefix.buildKey(CacheKeyPrefix.SUB_PAYMENT_PROJECTION, p.paymentId()));
        guard.markProcessed(PROCESSED_TABLE, event.eventId());
    }

    public record PaymentPayload(UUID paymentId, UUID orderId, UUID customerId,
                                  java.math.BigDecimal amount, String currency,
                                  String paymentStatus, Instant paidAt) {}
}
