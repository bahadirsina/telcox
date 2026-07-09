package com.telcox.usage.event;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.telcox.usage.quota.UsageQuota;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class UsageQuotaEventPublisher {
    static final String THRESHOLD_EVENT_TYPE = "quota-threshold-reached";
    static final String OVERAGE_EVENT_TYPE = "quota-overage-aggregated";
    private static final List<Integer> THRESHOLDS = List.of(80, 90, 100);
    private static final String AGGREGATE_TYPE = "USAGE_QUOTA";
    private static final int SCHEMA_VERSION = 1;

    private final UsageOutboxEventRepository outboxEvents;
    private final ObjectMapper objectMapper;

    public UsageQuotaEventPublisher(UsageOutboxEventRepository outboxEvents, ObjectMapper objectMapper) {
        this.outboxEvents = outboxEvents;
        this.objectMapper = objectMapper;
    }

    public void publishQuotaEvents(
            UsageQuota quota,
            BigDecimal previousUsedAmount,
            String correlationId,
            Instant occurredAt) {
        publishThresholdEvents(quota, correlationId, occurredAt);
        publishOverageEvent(quota, previousUsedAmount, correlationId, occurredAt);
    }

    private void publishThresholdEvents(UsageQuota quota, String correlationId, Instant occurredAt) {
        int currentPercent = usagePercent(quota.getUsedAmount(), quota.getTotalAllowance());
        for (Integer threshold : THRESHOLDS) {
            if (threshold > quota.getLastThresholdPercent() && currentPercent >= threshold) {
                outboxEvents.save(outboxEvent(
                        quota,
                        THRESHOLD_EVENT_TYPE,
                        new QuotaThresholdReachedEvent(
                                quota.getSubscriptionId(),
                                quota.getQuotaType(),
                                threshold,
                                quota.getTotalAllowance(),
                                quota.getUsedAmount(),
                                remainingAmount(quota),
                                quota.getPeriodStart(),
                                quota.getPeriodEnd(),
                                occurredAt),
                        correlationId,
                        occurredAt
                ));
                quota.markThresholdPublished(threshold, occurredAt);
            }
        }
    }

    private void publishOverageEvent(
            UsageQuota quota,
            BigDecimal previousUsedAmount,
            String correlationId,
            Instant occurredAt) {
        BigDecimal previousOverage = previousUsedAmount.subtract(quota.getTotalAllowance()).max(BigDecimal.ZERO);
        BigDecimal currentOverage = quota.overageAmount();
        BigDecimal delta = currentOverage.subtract(previousOverage);
        if (delta.signum() <= 0) {
            return;
        }
        outboxEvents.save(outboxEvent(
                quota,
                OVERAGE_EVENT_TYPE,
                new QuotaOverageAggregatedEvent(
                        quota.getSubscriptionId(),
                        quota.getQuotaType(),
                        quota.getTotalAllowance(),
                        quota.getUsedAmount(),
                        currentOverage,
                        delta,
                        quota.getPeriodStart(),
                        quota.getPeriodEnd(),
                        occurredAt),
                correlationId,
                occurredAt
        ));
    }

    private UsageOutboxEvent outboxEvent(
            UsageQuota quota,
            String eventType,
            Object payload,
            String correlationId,
            Instant occurredAt) {
        return UsageOutboxEvent.pending(
                quota.getId(),
                AGGREGATE_TYPE,
                eventType,
                writeJson(payload),
                correlationId,
                occurredAt,
                SCHEMA_VERSION
        );
    }

    private String writeJson(Object payload) {
        try {
            return objectMapper.writeValueAsString(payload);
        } catch (JsonProcessingException exception) {
            throw new IllegalStateException("Usage quota event payload could not be serialized", exception);
        }
    }

    private static int usagePercent(BigDecimal usedAmount, BigDecimal totalAllowance) {
        if (totalAllowance.signum() == 0) {
            return usedAmount.signum() > 0 ? 100 : 0;
        }
        return usedAmount.multiply(BigDecimal.valueOf(100))
                .divide(totalAllowance, 0, RoundingMode.DOWN)
                .intValue();
    }

    private static BigDecimal remainingAmount(UsageQuota quota) {
        return quota.getTotalAllowance().subtract(quota.getUsedAmount()).max(BigDecimal.ZERO);
    }
}
