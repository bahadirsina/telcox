package com.telcox.subscription.repository;

import com.telcox.subscription.domain.SubscriptionAddon;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface SubscriptionAddonRepository extends JpaRepository<SubscriptionAddon, UUID> {
    Optional<SubscriptionAddon> findBySubscription_IdAndAddonCode(UUID subscriptionId, String addonCode);
}
