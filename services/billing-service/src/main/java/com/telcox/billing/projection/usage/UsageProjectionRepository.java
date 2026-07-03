package com.telcox.billing.projection.usage;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface UsageProjectionRepository extends JpaRepository<UsageProjection, Long> {
    Optional<UsageProjection> findByUsageId(UUID usageId);
    List<UsageProjection> findBySubscriptionId(UUID subscriptionId);
}
