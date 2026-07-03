CREATE TABLE usage_projection (
    id              BIGSERIAL PRIMARY KEY,
    usage_id        UUID NOT NULL UNIQUE,
    subscription_id UUID NOT NULL,
    customer_id     UUID NOT NULL,
    usage_type      VARCHAR(30) NOT NULL,
    quantity        NUMERIC(14,4) NOT NULL,
    unit            VARCHAR(10) NOT NULL,
    period_start    TIMESTAMP NOT NULL,
    period_end      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);
CREATE INDEX idx_usage_projection_subscription_id ON usage_projection(subscription_id);

CREATE TABLE subscription_projection (
    id                  BIGSERIAL PRIMARY KEY,
    subscription_id     UUID NOT NULL UNIQUE,
    customer_id         UUID NOT NULL,
    tariff_id           UUID NOT NULL,
    subscription_status VARCHAR(30) NOT NULL,
    billing_cycle_day   INT,
    started_at          TIMESTAMP NOT NULL,
    updated_at          TIMESTAMP NOT NULL
);
CREATE INDEX idx_subscription_projection_customer_id ON subscription_projection(customer_id);