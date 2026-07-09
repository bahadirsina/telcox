package com.telcox.customer.repository;

import com.telcox.customer.domain.CustomerOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface CustomerOutboxEventRepository extends JpaRepository<CustomerOutboxEvent, UUID> {
}
