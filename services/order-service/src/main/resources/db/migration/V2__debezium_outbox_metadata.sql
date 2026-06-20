ALTER TABLE order_service_outbox_event ADD COLUMN IF NOT EXISTS correlation_id VARCHAR(100);
ALTER TABLE order_service_outbox_event ADD COLUMN IF NOT EXISTS schema_version INTEGER NOT NULL DEFAULT 1;
CREATE INDEX IF NOT EXISTS idx_order_outbox_correlation ON order_service_outbox_event(correlation_id);
