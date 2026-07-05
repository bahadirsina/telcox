CREATE TABLE IF NOT EXISTS customer_order (
    id                  UUID PRIMARY KEY,
    order_number        VARCHAR(40) NOT NULL UNIQUE,
    customer_id         UUID NOT NULL,
    order_type          VARCHAR(40) NOT NULL,
    plan_code           VARCHAR(80),
    addon_codes         TEXT,
    msisdn              VARCHAR(32),
    sim_iccid           VARCHAR(64),
    status              VARCHAR(50) NOT NULL,
    cancellation_reason VARCHAR(500),
    correlation_id      VARCHAR(100) NOT NULL,
    created_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at          TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at        TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_customer_order_customer ON customer_order(customer_id);
CREATE INDEX IF NOT EXISTS idx_customer_order_status ON customer_order(status);
CREATE INDEX IF NOT EXISTS idx_customer_order_correlation ON customer_order(correlation_id);

CREATE TABLE IF NOT EXISTS order_saga_history (
    id            UUID PRIMARY KEY,
    order_id      UUID NOT NULL REFERENCES customer_order(id) ON DELETE CASCADE,
    step_name     VARCHAR(80) NOT NULL,
    step_status   VARCHAR(40) NOT NULL,
    event_type    VARCHAR(100),
    message       VARCHAR(500),
    occurred_at   TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_order_saga_history_order ON order_saga_history(order_id, occurred_at);
