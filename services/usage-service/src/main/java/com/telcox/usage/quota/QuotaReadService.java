package com.telcox.usage.quota;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Clock;
import java.time.Instant;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class QuotaReadService {
    private final UsageQuotaRepository quotas;
    private final Clock clock;

    public QuotaReadService(UsageQuotaRepository quotas) {
        this(quotas, Clock.systemUTC());
    }

    QuotaReadService(UsageQuotaRepository quotas, Clock clock) {
        this.quotas = quotas;
        this.clock = clock;
    }

    @Transactional(readOnly = true)
    public List<QuotaResponse> current(UUID subscriptionId, Instant at) {
        Instant effectiveAt = at == null ? clock.instant() : at;
        return quotas.findCurrent(subscriptionId, effectiveAt).stream().map(QuotaResponse::from).toList();
    }

    public record QuotaResponse(
            UsageQuota.QuotaType type, BigDecimal total, BigDecimal used, BigDecimal remaining,
            BigDecimal usagePercent, Instant periodStart, Instant periodEnd) {
        static QuotaResponse from(UsageQuota quota) {
            BigDecimal remaining = quota.getTotalAllowance().subtract(quota.getUsedAmount()).max(BigDecimal.ZERO);
            BigDecimal percent = quota.getTotalAllowance().signum() == 0
                    ? BigDecimal.ZERO
                    : quota.getUsedAmount().multiply(BigDecimal.valueOf(100))
                            .divide(quota.getTotalAllowance(), 2, RoundingMode.HALF_UP);
            return new QuotaResponse(quota.getQuotaType(), quota.getTotalAllowance(), quota.getUsedAmount(),
                    remaining, percent, quota.getPeriodStart(), quota.getPeriodEnd());
        }
    }
}
