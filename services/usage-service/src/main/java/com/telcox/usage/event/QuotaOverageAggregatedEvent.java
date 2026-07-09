package com.telcox.usage.event;

import com.telcox.usage.quota.UsageQuota;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record QuotaOverageAggregatedEvent(
        UUID subscriptionId,
        UsageQuota.QuotaType quotaType,
        BigDecimal totalAllowance,
        BigDecimal usedAmount,
        BigDecimal overageAmount,
        BigDecimal overageDeltaAmount,
        Instant periodStart,
        Instant periodEnd,
        Instant occurredAt) {
}
