package com.telcox.billing.projection.subscription;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionProjectionRepository extends JpaRepository<SubscriptionProjection, Long> {
    Optional<SubscriptionProjection> findBySubscriptionId(UUID subscriptionId);

    /** BILL-01: Bill-run job'un donduracegi aktif abonelikler. */
    List<SubscriptionProjection> findBySubscriptionStatus(String subscriptionStatus);
}