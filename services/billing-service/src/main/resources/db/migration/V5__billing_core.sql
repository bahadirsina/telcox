-- =============================================================================
-- BILL-01 / FR-21: billing_account, billing_cycle ve invoice (iskelet) tablolari
-- ER: db.sql -> BILLING_SERVICE_BILLING_ACCOUNT, BILLING_SERVICE_BILLING_CYCLE,
--               BILLING_SERVICE_INVOICE
-- =============================================================================

CREATE TABLE IF NOT EXISTS billing_service_billing_account (
    id          UUID PRIMARY KEY,
    customer_id UUID NOT NULL UNIQUE,
    currency    VARCHAR(3),
    balance     NUMERIC(19, 4) NOT NULL DEFAULT 0,
    credit_limit NUMERIC(19, 4),
    status      VARCHAR(20) NOT NULL,
    created_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at  TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);
CREATE INDEX IF NOT EXISTS idx_billing_account_customer_id ON billing_service_billing_account(customer_id);

CREATE TABLE IF NOT EXISTS billing_service_billing_cycle (
    id                 UUID PRIMARY KEY,
    billing_account_id UUID NOT NULL,
    cycle_type         VARCHAR(20) NOT NULL,
    period_start       DATE NOT NULL,
    period_end         DATE NOT NULL,
    status             VARCHAR(20) NOT NULL,
    CONSTRAINT fk_billing_cycle_account_id
        FOREIGN KEY (billing_account_id) REFERENCES billing_service_billing_account(id)
);
CREATE INDEX IF NOT EXISTS idx_billing_cycle_account_period ON billing_service_billing_cycle(billing_account_id, period_start, period_end);

CREATE TABLE IF NOT EXISTS billing_service_invoice (
    id                 UUID PRIMARY KEY,
    invoice_number     VARCHAR(50) NOT NULL UNIQUE,
    billing_account_id UUID NOT NULL,
    billing_cycle_id   UUID,
    subscription_id    UUID,
    period_start       DATE NOT NULL,
    period_end         DATE NOT NULL,
    subtotal           NUMERIC(19, 4) NOT NULL DEFAULT 0,
    tax_amount         NUMERIC(19, 4) NOT NULL DEFAULT 0,
    total_amount       NUMERIC(19, 4) NOT NULL DEFAULT 0,
    currency           VARCHAR(3),
    status             VARCHAR(20) NOT NULL,
    issued_at          TIMESTAMP,
    due_date           DATE,
    paid_at            TIMESTAMP,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT fk_invoice_billing_account_id
        FOREIGN KEY (billing_account_id) REFERENCES billing_service_billing_account(id),
    CONSTRAINT fk_invoice_billing_cycle_id
        FOREIGN KEY (billing_cycle_id) REFERENCES billing_service_billing_cycle(id)
);
CREATE INDEX IF NOT EXISTS idx_invoice_billing_account_id ON billing_service_invoice(billing_account_id);
CREATE INDEX IF NOT EXISTS idx_invoice_subscription_cycle ON billing_service_invoice(subscription_id, billing_cycle_id);
