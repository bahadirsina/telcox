-- PRJ-05: local customer communication preference projection.
-- Source of truth remains customer-service.

CREATE TABLE IF NOT EXISTS notification_customer_preference_projection (
    customer_id             UUID PRIMARY KEY,
    email                   VARCHAR(320),
    phone_number            VARCHAR(32),
    preferred_channel       VARCHAR(20) NOT NULL,
    email_enabled           BOOLEAN NOT NULL DEFAULT TRUE,
    sms_enabled             BOOLEAN NOT NULL DEFAULT TRUE,
    push_enabled            BOOLEAN NOT NULL DEFAULT TRUE,
    marketing_consent       BOOLEAN NOT NULL DEFAULT FALSE,
    transactional_consent   BOOLEAN NOT NULL DEFAULT TRUE,
    last_event_id           UUID NOT NULL,
    source_updated_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    projected_at            TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_notification_customer_preference_channel
        CHECK (preferred_channel IN ('EMAIL', 'SMS', 'PUSH', 'NONE'))
);

CREATE INDEX IF NOT EXISTS idx_notification_customer_preference_channel
    ON notification_customer_preference_projection(preferred_channel);

CREATE INDEX IF NOT EXISTS idx_notification_customer_preference_source_updated
    ON notification_customer_preference_projection(source_updated_at);
