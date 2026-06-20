-- Keycloak is the sole identity provider and token issuer.
-- This service stores only application-owned profile and audit data.

ALTER TABLE identity_service_user
    ADD COLUMN IF NOT EXISTS keycloak_subject UUID;

ALTER TABLE identity_service_user
    ALTER COLUMN password_hash DROP NOT NULL;

CREATE UNIQUE INDEX IF NOT EXISTS uk_identity_user_keycloak_subject
    ON identity_service_user(keycloak_subject)
    WHERE keycloak_subject IS NOT NULL;

COMMENT ON COLUMN identity_service_user.keycloak_subject IS
    'Keycloak JWT sub claim; token/password material is never stored locally.';

COMMENT ON TABLE identity_service_refresh_token IS
    'Legacy table. New refresh tokens are owned and revoked by Keycloak.';
