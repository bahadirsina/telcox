package com.telcox.subscription.repository;

import com.telcox.subscription.domain.Subscription;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface SubscriptionRepository extends JpaRepository<Subscription, UUID> {
    List<Subscription> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);

    Optional<Subscription> findByOrderId(UUID orderId);

    Optional<Subscription> findByMsisdn(String msisdn);

    boolean existsByMsisdn(String msisdn);
}
