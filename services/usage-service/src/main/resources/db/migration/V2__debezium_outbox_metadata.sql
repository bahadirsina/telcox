ALTER TABLE usage_service_outbox_event ADD COLUMN IF NOT EXISTS correlation_id VARCHAR(100);
ALTER TABLE usage_service_outbox_event ADD COLUMN IF NOT EXISTS schema_version INTEGER NOT NULL DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_usage_outbox_correlation ON usage_service_outbox_event(correlation_id);
