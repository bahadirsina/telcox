package com.telcox.subscription.repository;

import com.telcox.subscription.domain.SubscriptionOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface SubscriptionOutboxEventRepository extends JpaRepository<SubscriptionOutboxEvent, UUID> {
}
