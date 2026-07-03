package com.telcox.billing.projection.subscription;

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
public class SubscriptionProjectionEventListener {

    private static final String PROCESSED_TABLE = "billing_service_processed_event";

    private final SubscriptionProjectionRepository repository;
    private final CacheService cacheService;
    private final ProcessedEventGuard guard;

    public SubscriptionProjectionEventListener(SubscriptionProjectionRepository repository,
                                                 CacheService cacheService, ProcessedEventGuard guard) {
        this.repository = repository;
        this.cacheService = cacheService;
        this.guard = guard;
    }

    @KafkaListener(
        topics = "${topics.subscription-activated:telcox.subscription.subscription-activated.v1}",
        groupId = "billing-service-subscription-projection"
    )
    @Transactional
    public void onSubscriptionEvent(EventEnvelope<SubscriptionPayload> event) {
        boolean isNew = guard.tryBeginProcessing(PROCESSED_TABLE, event.eventId(),
                event.type(), event.sourceService(), event.aggregateId());
        if (!isNew) return;

        SubscriptionPayload p = event.payload();
        var existing = repository.findBySubscriptionId(p.subscriptionId());
        if (existing.isPresent()) {
            existing.get().applyStatusUpdate(p.subscriptionStatus(), Instant.now());
            repository.save(existing.get());
        } else {
            repository.save(new SubscriptionProjection(p.subscriptionId(), p.customerId(), p.tariffId(),
                    p.subscriptionStatus(), p.billingCycleDay(), p.startedAt(), Instant.now()));
        }

        cacheService.evict(CacheKeyPrefix.buildKey(
                CacheKeyPrefix.BILLING_SUBSCRIPTION_PROJECTION, p.subscriptionId()));
        guard.markProcessed(PROCESSED_TABLE, event.eventId());
    }

    public record SubscriptionPayload(UUID subscriptionId, UUID customerId, UUID tariffId,
                                       String subscriptionStatus, Integer billingCycleDay,
                                       Instant startedAt) {}
}
