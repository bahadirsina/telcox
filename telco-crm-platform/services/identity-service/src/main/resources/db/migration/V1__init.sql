-- =============================================================================
-- identity-service initial schema
-- =============================================================================

CREATE TABLE IF NOT EXISTS identity_service_user (
    id              UUID PRIMARY KEY,
    email           VARCHAR(255) NOT NULL UNIQUE,
    password_hash   VARCHAR(255) NOT NULL,
    status          VARCHAR(50)  NOT NULL,
    created_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP    NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE IF NOT EXISTS identity_service_role (
    id          UUID PRIMARY KEY,
    name        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS identity_service_permission (
    id          UUID PRIMARY KEY,
    code        VARCHAR(100) NOT NULL UNIQUE,
    description VARCHAR(500)
);

CREATE TABLE IF NOT EXISTS identity_service_user_role (
    user_id UUID NOT NULL REFERENCES identity_service_user(id) ON DELETE CASCADE,
    role_id UUID NOT NULL REFERENCES identity_service_role(id) ON DELETE CASCADE,
    PRIMARY KEY (user_id, role_id)
);

CREATE TABLE IF NOT EXISTS identity_service_role_permission (
    role_id       UUID NOT NULL REFERENCES identity_service_role(id) ON DELETE CASCADE,
    permission_id UUID NOT NULL REFERENCES identity_service_permission(id) ON DELETE CASCADE,
    PRIMARY KEY (role_id, permission_id)
);

CREATE TABLE IF NOT EXISTS identity_service_refresh_token (
    id          UUID PRIMARY KEY,
    user_id     UUID NOT NULL REFERENCES identity_service_user(id) ON DELETE CASCADE,
    token_hash  VARCHAR(255) NOT NULL,
    expires_at  TIMESTAMP NOT NULL,
    revoked_at  TIMESTAMP,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX idx_refresh_token_user ON identity_service_refresh_token(user_id);

CREATE TABLE IF NOT EXISTS identity_service_audit_log (
    id              UUID PRIMARY KEY,
    actor_user_id   UUID REFERENCES identity_service_user(id),
    action          VARCHAR(100),
    entity_type     VARCHAR(100),
    entity_id       UUID,
    old_value_json  JSONB,
    new_value_json  JSONB,
    correlation_id  VARCHAR(100),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

-- Outbox: heterojen aggregateId, FK YOK (kasitli)
CREATE TABLE IF NOT EXISTS identity_service_outbox_event (
    id              UUID PRIMARY KEY,
    aggregate_id    UUID,
    aggregate_type  VARCHAR(100),
    event_type      VARCHAR(100) NOT NULL,
    payload_json    JSONB NOT NULL,
    status          VARCHAR(50) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    published_at    TIMESTAMP
);
CREATE INDEX idx_outbox_status_created ON identity_service_outbox_event(status, created_at);

CREATE TABLE IF NOT EXISTS identity_service_processed_event (
    id              UUID PRIMARY KEY,
    event_id        UUID NOT NULL UNIQUE,
    event_type      VARCHAR(100),
    source_service  VARCHAR(100),
    aggregate_id    UUID,
    processed_at    TIMESTAMP,
    status          VARCHAR(50),
    error_message   VARCHAR(500),
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
