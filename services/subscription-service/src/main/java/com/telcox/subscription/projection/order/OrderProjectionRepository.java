package com.telcox.subscription.projection.order;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface OrderProjectionRepository extends JpaRepository<OrderProjection, Long> {
    Optional<OrderProjection> findByOrderId(UUID orderId);
}