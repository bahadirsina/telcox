package com.telcox.notification.repository;

import com.telcox.notification.domain.NotificationDelivery;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationDeliveryRepository extends JpaRepository<NotificationDelivery, UUID> {
    List<NotificationDelivery> findAllByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
