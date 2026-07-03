package com.telcox.subscription.projection.payment;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;
import java.util.UUID;

public interface PaymentProjectionRepository extends JpaRepository<PaymentProjection, Long> {
    Optional<PaymentProjection> findByPaymentId(UUID paymentId);
    Optional<PaymentProjection> findByOrderId(UUID orderId);
}