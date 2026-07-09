package com.telcox.usage.event;

import com.telcox.usage.quota.UsageQuota;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;

public record QuotaThresholdReachedEvent(
        UUID subscriptionId,
        UsageQuota.QuotaType quotaType,
        int thresholdPercent,
        BigDecimal totalAllowance,
        BigDecimal usedAmount,
        BigDecimal remainingAmount,
        Instant periodStart,
        Instant periodEnd,
        Instant occurredAt) {
}
