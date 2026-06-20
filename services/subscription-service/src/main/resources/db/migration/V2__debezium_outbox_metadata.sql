ALTER TABLE subscription_service_outbox_event ADD COLUMN IF NOT EXISTS correlation_id VARCHAR(100);
ALTER TABLE subscription_service_outbox_event ADD COLUMN IF NOT EXISTS schema_version INTEGER NOT NULL DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_subscription_outbox_correlation ON subscription_service_outbox_event(correlation_id);
