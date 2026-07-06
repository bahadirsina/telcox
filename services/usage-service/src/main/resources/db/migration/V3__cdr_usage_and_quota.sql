-- USG-01 / USG-02: idempotent CDR storage and quota read model.

CREATE TABLE IF NOT EXISTS usage_service_usage_record (
    id                  UUID PRIMARY KEY,
    external_cdr_id     VARCHAR(128) NOT NULL UNIQUE,
    subscription_id     UUID NOT NULL,
    msisdn              VARCHAR(32) NOT NULL,
    record_type         VARCHAR(20) NOT NULL,
    occurred_at         TIMESTAMP WITH TIME ZONE NOT NULL,
    duration_seconds    BIGINT NOT NULL DEFAULT 0,
    data_volume_bytes   BIGINT NOT NULL DEFAULT 0,
    sms_count           BIGINT NOT NULL DEFAULT 0,
    created_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_usage_record_type
        CHECK (record_type IN ('VOICE', 'SMS', 'DATA'))
);

CREATE INDEX IF NOT EXISTS idx_usage_record_subscription_occurred
    ON usage_service_usage_record(subscription_id, occurred_at DESC);

CREATE TABLE IF NOT EXISTS usage_service_quota (
    id                  UUID PRIMARY KEY,
    subscription_id     UUID NOT NULL,
    quota_type          VARCHAR(20) NOT NULL,
    total_allowance     NUMERIC(19, 4) NOT NULL,
    used_amount         NUMERIC(19, 4) NOT NULL DEFAULT 0,
    period_start        TIMESTAMP WITH TIME ZONE NOT NULL,
    period_end          TIMESTAMP WITH TIME ZONE NOT NULL,
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_usage_quota_period
        UNIQUE (subscription_id, quota_type, period_start, period_end),
    CONSTRAINT chk_usage_quota_type
        CHECK (quota_type IN ('VOICE_MIN', 'SMS_COUNT', 'DATA_MB')),
    CONSTRAINT chk_usage_quota_amounts
        CHECK (total_allowance >= 0 AND used_amount >= 0)
);

CREATE INDEX IF NOT EXISTS idx_usage_quota_subscription_period
    ON usage_service_quota(subscription_id, period_start, period_end);
