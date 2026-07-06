CREATE TABLE IF NOT EXISTS notification_delivery (
    id              UUID PRIMARY KEY,
    customer_id     UUID,
    channel         VARCHAR(20) NOT NULL,
    recipient       VARCHAR(320) NOT NULL,
    subject         VARCHAR(255),
    content         TEXT NOT NULL,
    status          VARCHAR(20) NOT NULL,
    failure_reason  VARCHAR(500),
    correlation_id  VARCHAR(100),
    created_at      TIMESTAMP WITH TIME ZONE NOT NULL,
    sent_at         TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_notification_delivery_channel CHECK (channel IN ('EMAIL', 'SMS')),
    CONSTRAINT chk_notification_delivery_status CHECK (status IN ('PENDING', 'SENT', 'FAILED'))
);

CREATE INDEX IF NOT EXISTS idx_notification_delivery_customer_created
    ON notification_delivery(customer_id, created_at DESC);

CREATE INDEX IF NOT EXISTS idx_notification_delivery_status_created
    ON notification_delivery(status, created_at);
