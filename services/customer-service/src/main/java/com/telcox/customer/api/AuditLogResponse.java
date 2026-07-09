package com.telcox.customer.api;

import com.telcox.customer.domain.CustomerAuditLog;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record AuditLogResponse(
        UUID id,
        UUID actorUserId,
        String action,
        String entityType,
        UUID entityId,
        Map<String, Object> oldValueJson,
        Map<String, Object> newValueJson,
        String correlationId,
        LocalDateTime createdAt
) {
    public static AuditLogResponse from(CustomerAuditLog log) {
        return new AuditLogResponse(
                log.getId(), log.getActorUserId(), log.getAction(), log.getEntityType(), log.getEntityId(),
                log.getOldValueJson(), log.getNewValueJson(), log.getCorrelationId(), log.getCreatedAt()
        );
    }
}
