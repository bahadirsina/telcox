package com.telcox.subscription.projection.order;

import java.util.UUID;
import com.telcox.common.cache.CacheKeyPrefix;
import com.telcox.common.cache.CacheService;
import com.telcox.common.event.EventEnvelope;
import com.telcox.common.event.ProcessedEventGuard;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;

@Component
public class OrderProjectionEventListener {

    private static final String PROCESSED_TABLE = "subscription_service_processed_event";

    private final OrderProjectionRepository repository;
    private final CacheService cacheService;
    private final ProcessedEventGuard guard;

    public OrderProjectionEventListener(OrderProjectionRepository repository,
                                         CacheService cacheService, ProcessedEventGuard guard) {
        this.repository = repository;
        this.cacheService = cacheService;
        this.guard = guard;
    }

    // NOT: event_type henuz order-service'te sabitlenmedi. Netlesince
    // application.yaml'da topics.order-confirmed degerini gercek isimle guncelle.
    @KafkaListener(
        topics = "${topics.order-confirmed:telcox.order.order-confirmed.v1}",
        groupId = "subscription-service-order-projection"
    )
    @Transactional
    public void onOrderEvent(EventEnvelope<OrderPayload> event) {
        boolean isNew = guard.tryBeginProcessing(PROCESSED_TABLE, event.eventId(),
                event.type(), event.sourceService(), event.aggregateId());
        if (!isNew) return;

        OrderPayload p = event.payload();
        var existing = repository.findByOrderId(p.orderId());
        if (existing.isPresent()) {
            existing.get().applyStatusUpdate(p.orderStatus(), Instant.now());
            repository.save(existing.get());
        } else {
            repository.save(new OrderProjection(p.orderId(), p.customerId(), p.tariffId(),
                    p.orderStatus(), p.orderType(), Instant.now(), Instant.now()));
        }

        cacheService.evict(CacheKeyPrefix.buildKey(CacheKeyPrefix.SUB_ORDER_PROJECTION, p.orderId()));
        guard.markProcessed(PROCESSED_TABLE, event.eventId());
    }

    public record OrderPayload(UUID orderId, UUID customerId, UUID tariffId,
                                String orderStatus, String orderType) {}
}
