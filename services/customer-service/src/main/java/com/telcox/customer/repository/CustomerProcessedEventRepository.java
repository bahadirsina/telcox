package com.telcox.customer.repository;

import com.telcox.customer.domain.CustomerProcessedEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface CustomerProcessedEventRepository extends JpaRepository<CustomerProcessedEvent, UUID> {
    Optional<CustomerProcessedEvent> findByEventId(UUID eventId);
}
