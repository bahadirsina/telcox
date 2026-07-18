package com.telcox.identity.api;

import com.telcox.identity.domain.IdentityAuditLog;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

public record IdentityAuditLogResponse(
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
    public static IdentityAuditLogResponse from(IdentityAuditLog log) {
        return new IdentityAuditLogResponse(
                log.getId(),
                log.getActorUserId(),
                log.getAction(),
                log.getEntityType(),
                log.getEntityId(),
                log.getOldValueJson(),
                log.getNewValueJson(),
                log.getCorrelationId(),
                log.getCreatedAt()
        );
    }
}
