-- USG-03 / USG-04: quota threshold tracking and usage outbox event indexes.

ALTER TABLE usage_service_quota
    ADD COLUMN IF NOT EXISTS last_threshold_percent INTEGER NOT NULL DEFAULT 0;

ALTER TABLE usage_service_quota
    ADD CONSTRAINT chk_usage_quota_last_threshold
        CHECK (last_threshold_percent IN (0, 80, 90, 100));

CREATE INDEX IF NOT EXISTS idx_usage_outbox_event_type_created
    ON usage_service_outbox_event(event_type, created_at);
