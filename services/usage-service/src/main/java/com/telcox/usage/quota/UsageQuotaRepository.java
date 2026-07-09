package com.telcox.usage.quota;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface UsageQuotaRepository extends JpaRepository<UsageQuota, UUID> {
    @Query("""
            select quota from UsageQuota quota
            where quota.subscriptionId = :subscriptionId and quota.quotaType = :quotaType
              and quota.periodStart <= :at and quota.periodEnd > :at
            """)
    Optional<UsageQuota> findActive(UUID subscriptionId, UsageQuota.QuotaType quotaType, Instant at);

    @Query("""
            select quota from UsageQuota quota
            where quota.subscriptionId = :subscriptionId
              and quota.periodStart <= :at and quota.periodEnd > :at
            order by quota.quotaType
            """)
    List<UsageQuota> findCurrent(UUID subscriptionId, Instant at);
}
