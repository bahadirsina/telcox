package com.telcox.identity.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import org.hibernate.annotations.JdbcTypeCode;
import org.hibernate.type.SqlTypes;

import java.time.LocalDateTime;
import java.util.Map;
import java.util.UUID;

@Entity
@Table(name = "identity_service_audit_log")
public class IdentityAuditLog {

    @Id
    private UUID id;

    @Column(name = "actor_user_id")
    private UUID actorUserId;

    @Column(length = 100)
    private String action;

    @Column(name = "entity_type", length = 100)
    private String entityType;

    @Column(name = "entity_id")
    private UUID entityId;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "old_value_json", columnDefinition = "jsonb")
    private Map<String, Object> oldValueJson;

    @JdbcTypeCode(SqlTypes.JSON)
    @Column(name = "new_value_json", columnDefinition = "jsonb")
    private Map<String, Object> newValueJson;

    @Column(name = "correlation_id", length = 100)
    private String correlationId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected IdentityAuditLog() {
    }

    public IdentityAuditLog(UUID actorUserId, String action, String entityType, UUID entityId,
                            Map<String, Object> oldValueJson, Map<String, Object> newValueJson,
                            String correlationId) {
        this.id = UUID.randomUUID();
        this.actorUserId = actorUserId;
        this.action = action;
        this.entityType = entityType;
        this.entityId = entityId;
        this.oldValueJson = oldValueJson;
        this.newValueJson = newValueJson;
        this.correlationId = correlationId;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getActorUserId() {
        return actorUserId;
    }

    public String getAction() {
        return action;
    }

    public String getEntityType() {
        return entityType;
    }

    public UUID getEntityId() {
        return entityId;
    }

    public Map<String, Object> getOldValueJson() {
        return oldValueJson;
    }

    public Map<String, Object> getNewValueJson() {
        return newValueJson;
    }

    public String getCorrelationId() {
        return correlationId;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
