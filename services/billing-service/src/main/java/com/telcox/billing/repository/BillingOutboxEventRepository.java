package com.telcox.billing.repository;

import com.telcox.billing.domain.BillingOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface BillingOutboxEventRepository extends JpaRepository<BillingOutboxEvent, UUID> {
}
