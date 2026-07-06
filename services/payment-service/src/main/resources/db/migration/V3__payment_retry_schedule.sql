-- PAY-01 / FR-25, PAY-02 / FR-26 and PAY-03 / FR-27 payment processing schema.

CREATE TABLE IF NOT EXISTS payment_service_payment (
    id                  UUID PRIMARY KEY,
    payment_reference   VARCHAR(100) NOT NULL UNIQUE,
    idempotency_key     VARCHAR(255) NOT NULL UNIQUE,
    customer_id         UUID NOT NULL,
    invoice_id          UUID,
    amount              NUMERIC(19, 4) NOT NULL,
    currency            VARCHAR(3) NOT NULL,
    card_last_four      VARCHAR(4) NOT NULL,
    card_holder_name    VARCHAR(150),
    provider_auth_code  VARCHAR(80),
    status              VARCHAR(20) NOT NULL,
    failure_reason      VARCHAR(500),
    initiated_at        TIMESTAMP WITH TIME ZONE NOT NULL,
    completed_at        TIMESTAMP WITH TIME ZONE,
    CONSTRAINT chk_payment_status
        CHECK (status IN ('PENDING', 'AUTHORIZED', 'CAPTURED', 'FAILED', 'REFUNDED'))
);

CREATE INDEX IF NOT EXISTS idx_payment_customer_created
    ON payment_service_payment(customer_id, initiated_at);

CREATE TABLE IF NOT EXISTS payment_service_retry_schedule (
    id                  UUID PRIMARY KEY,
    payment_id          UUID NOT NULL REFERENCES payment_service_payment(id),
    first_failed_at     TIMESTAMP WITH TIME ZONE NOT NULL,
    attempt_number      INTEGER NOT NULL,
    next_retry_at       TIMESTAMP WITH TIME ZONE NOT NULL,
    status              VARCHAR(20) NOT NULL,
    last_error          VARCHAR(500),
    updated_at          TIMESTAMP WITH TIME ZONE NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT uq_payment_retry_attempt UNIQUE (payment_id, attempt_number),
    CONSTRAINT chk_payment_retry_attempt CHECK (attempt_number BETWEEN 1 AND 3),
    CONSTRAINT chk_payment_retry_status
        CHECK (status IN ('READY', 'PROCESSING', 'COMPLETED', 'EXHAUSTED'))
);

CREATE INDEX IF NOT EXISTS idx_payment_retry_due
    ON payment_service_retry_schedule(next_retry_at)
    WHERE status = 'READY';
