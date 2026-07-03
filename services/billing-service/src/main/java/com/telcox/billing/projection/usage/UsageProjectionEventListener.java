package com.telcox.billing.projection.usage;

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
public class UsageProjectionEventListener {

    private static final String PROCESSED_TABLE = "billing_service_processed_event";

    private final UsageProjectionRepository repository;
    private final CacheService cacheService;
    private final ProcessedEventGuard guard;

    public UsageProjectionEventListener(UsageProjectionRepository repository,
                                         CacheService cacheService, ProcessedEventGuard guard) {
        this.repository = repository;
        this.cacheService = cacheService;
        this.guard = guard;
    }

    @KafkaListener(
        topics = "${topics.usage-recorded:telcox.usage.usage-recorded.v1}",
        groupId = "billing-service-usage-projection"
    )
    @Transactional
    public void onUsageEvent(EventEnvelope<UsagePayload> event) {
        boolean isNew = guard.tryBeginProcessing(PROCESSED_TABLE, event.eventId(),
                event.type(), event.sourceService(), event.aggregateId());
        if (!isNew) return;

        UsagePayload p = event.payload();
        repository.save(new UsageProjection(p.usageId(), p.subscriptionId(), p.customerId(),
                p.usageType(), p.quantity(), p.unit(), p.periodStart(), p.periodEnd(), Instant.now()));

        cacheService.evict(CacheKeyPrefix.buildKey(
                CacheKeyPrefix.BILLING_USAGE_PROJECTION, p.subscriptionId()));
        guard.markProcessed(PROCESSED_TABLE, event.eventId());
    }

    public record UsagePayload(UUID usageId, UUID subscriptionId, UUID customerId,
                                String usageType, java.math.BigDecimal quantity, String unit,
                                Instant periodStart, Instant periodEnd) {}
}
