package com.telcox.usage.quota;

import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.Instant;
import java.util.UUID;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "usage_service_quota")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class UsageQuota {
    @Id private UUID id;
    private UUID subscriptionId;
    @Enumerated(EnumType.STRING)
    private QuotaType quotaType;
    private BigDecimal totalAllowance;
    private BigDecimal usedAmount;
    private Instant periodStart;
    private Instant periodEnd;
    private Instant updatedAt;

    public static UsageQuota create(
            UUID subscriptionId, QuotaType type, BigDecimal totalAllowance,
            Instant periodStart, Instant periodEnd) {
        UsageQuota quota = new UsageQuota();
        quota.id = UUID.randomUUID();
        quota.subscriptionId = subscriptionId;
        quota.quotaType = type;
        quota.totalAllowance = totalAllowance;
        quota.usedAmount = BigDecimal.ZERO;
        quota.periodStart = periodStart;
        quota.periodEnd = periodEnd;
        quota.updatedAt = periodStart;
        return quota;
    }

    public void consume(BigDecimal amount, Instant now) {
        usedAmount = usedAmount.add(amount);
        updatedAt = now;
    }

    public enum QuotaType {
        VOICE_MIN, SMS_COUNT, DATA_MB
    }
}
