-- =============================================================================

-- audit log, KVKK riza (consent) ve not tablolari
-- ER: db.sql -> CUSTOMER_SERVICE_CONSENT, CUSTOMER_SERVICE_AUDIT_LOG, CUSTOMER_SERVICE_NOTE
-- =============================================================================

CREATE TABLE IF NOT EXISTS customer_service_consent (
    id            UUID PRIMARY KEY,
    customer_id   UUID NOT NULL,
    consent_type  VARCHAR(20) NOT NULL,
    channel       VARCHAR(20),
    is_granted    BOOLEAN NOT NULL DEFAULT FALSE,
    granted_at    TIMESTAMP,
    revoked_at    TIMESTAMP,
    CONSTRAINT fk_customer_consent_customer_id
        FOREIGN KEY (customer_id) REFERENCES customer_service_customer(id)
);
CREATE INDEX IF NOT EXISTS idx_customer_consent_customer_id ON customer_service_consent(customer_id);
CREATE UNIQUE INDEX IF NOT EXISTS uq_customer_consent_customer_type ON customer_service_consent(customer_id, consent_type);

CREATE TABLE IF NOT EXISTS customer_service_audit_log (
    id             UUID PRIMARY KEY,
    actor_user_id  UUID,
    action         VARCHAR(100),
    entity_type    VARCHAR(100),
    entity_id      UUID,
    old_value_json JSONB,
    new_value_json JSONB,
    correlation_id VARCHAR(100),
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_customer_audit_log_entity ON customer_service_audit_log(entity_type, entity_id);

CREATE TABLE IF NOT EXISTS customer_service_note (
    id             UUID PRIMARY KEY,
    customer_id    UUID NOT NULL,
    author_user_id UUID,
    note_text      TEXT,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_customer_note_customer_id
        FOREIGN KEY (customer_id) REFERENCES customer_service_customer(id)
);
CREATE INDEX IF NOT EXISTS idx_customer_note_customer_id ON customer_service_note(customer_id);
