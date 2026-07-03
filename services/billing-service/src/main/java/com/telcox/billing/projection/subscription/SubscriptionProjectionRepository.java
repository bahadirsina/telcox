package com.telcox.billing.projection.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionProjectionRepository extends JpaRepository<SubscriptionProjection, Long> {
    Optional<SubscriptionProjection> findBySubscriptionId(UUID subscriptionId);
}
