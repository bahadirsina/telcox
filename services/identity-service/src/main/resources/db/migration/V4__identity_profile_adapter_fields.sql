ALTER TABLE identity_service_user ADD COLUMN IF NOT EXISTS display_name VARCHAR(255);
ALTER TABLE identity_service_user ADD COLUMN IF NOT EXISTS phone_number VARCHAR(30);
ALTER TABLE identity_service_user ADD COLUMN IF NOT EXISTS locale VARCHAR(20) DEFAULT 'tr-TR';
ALTER TABLE identity_service_user ADD COLUMN IF NOT EXISTS last_seen_at TIMESTAMP;

CREATE INDEX IF NOT EXISTS idx_identity_user_keycloak_subject
    ON identity_service_user(keycloak_subject);
