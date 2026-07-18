package com.telcox.identity.repository;

import com.telcox.identity.domain.IdentityAuditLog;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface IdentityAuditLogRepository extends JpaRepository<IdentityAuditLog, UUID> {

    List<IdentityAuditLog> findByActorUserIdOrderByCreatedAtDesc(UUID actorUserId);
}
