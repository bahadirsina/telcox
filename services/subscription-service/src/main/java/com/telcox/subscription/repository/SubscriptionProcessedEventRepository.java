package com.telcox.subscription.repository;

import com.telcox.subscription.domain.SubscriptionProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubscriptionProcessedEventRepository extends JpaRepository<SubscriptionProcessedEvent, UUID> {
    boolean existsByEventId(UUID eventId);
}
