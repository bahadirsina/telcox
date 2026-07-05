package com.telcox.order.repository;

import com.telcox.order.domain.OrderSagaHistory;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface OrderSagaHistoryRepository extends JpaRepository<OrderSagaHistory, UUID> {
    List<OrderSagaHistory> findByOrderIdOrderByOccurredAtAsc(UUID orderId);
}
