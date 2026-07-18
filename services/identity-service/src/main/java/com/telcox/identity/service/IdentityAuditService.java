package com.telcox.identity.service;

import com.telcox.identity.domain.IdentityAuditLog;
import com.telcox.identity.repository.IdentityAuditLogRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@Service
public class IdentityAuditService {

    private final IdentityAuditLogRepository auditLogRepository;

    public IdentityAuditService(IdentityAuditLogRepository auditLogRepository) {
        this.auditLogRepository = auditLogRepository;
    }

    @Transactional
    public void writeAuditLog(UUID actorUserId, String action, String entityType, UUID entityId,
                              Map<String, Object> oldValueJson, Map<String, Object> newValueJson,
                              String correlationId) {
        auditLogRepository.save(new IdentityAuditLog(
                actorUserId, action, entityType, entityId, oldValueJson, newValueJson, correlationId));
    }

    public List<IdentityAuditLog> listForActor(UUID actorUserId) {
        return auditLogRepository.findByActorUserIdOrderByCreatedAtDesc(actorUserId);
    }
}
