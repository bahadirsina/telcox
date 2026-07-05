package com.telcox.order.repository;

import com.telcox.order.domain.OrderProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderProcessedEventRepository extends JpaRepository<OrderProcessedEvent, UUID> {
    boolean existsByEventId(UUID eventId);
}
