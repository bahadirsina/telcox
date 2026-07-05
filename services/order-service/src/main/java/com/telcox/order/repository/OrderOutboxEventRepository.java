package com.telcox.order.repository;

import com.telcox.order.domain.OrderOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface OrderOutboxEventRepository extends JpaRepository<OrderOutboxEvent, UUID> {
}
