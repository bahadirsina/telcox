package com.telcox.customer.repository;

import com.telcox.customer.domain.CustomerAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerAuditLogRepository extends JpaRepository<CustomerAuditLog, UUID> {
    List<CustomerAuditLog> findByEntityTypeAndEntityIdOrderByCreatedAtDesc(String entityType, UUID entityId);
}
