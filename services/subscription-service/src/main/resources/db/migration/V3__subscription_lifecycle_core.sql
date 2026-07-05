CREATE TABLE IF NOT EXISTS subscription (
    id             UUID PRIMARY KEY,
    customer_id    UUID NOT NULL,
    order_id       UUID UNIQUE,
    msisdn         VARCHAR(32) NOT NULL UNIQUE,
    sim_iccid      VARCHAR(64),
    plan_code      VARCHAR(80) NOT NULL,
    status         VARCHAR(50) NOT NULL,
    status_reason  VARCHAR(500),
    correlation_id VARCHAR(100) NOT NULL,
    activated_at   TIMESTAMP,
    suspended_at   TIMESTAMP,
    terminated_at  TIMESTAMP,
    created_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at     TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_subscription_customer ON subscription(customer_id);
CREATE INDEX IF NOT EXISTS idx_subscription_status ON subscription(status);
CREATE INDEX IF NOT EXISTS idx_subscription_order ON subscription(order_id);

CREATE TABLE IF NOT EXISTS subscription_addon (
    id              UUID PRIMARY KEY,
    subscription_id UUID NOT NULL REFERENCES subscription(id) ON DELETE CASCADE,
    addon_code      VARCHAR(80) NOT NULL,
    status          VARCHAR(40) NOT NULL,
    effective_at    TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    UNIQUE(subscription_id, addon_code)
);

CREATE TABLE IF NOT EXISTS mnp_port_request (
    id                 UUID PRIMARY KEY,
    subscription_id    UUID REFERENCES subscription(id),
    customer_id        UUID NOT NULL,
    msisdn             VARCHAR(32) NOT NULL,
    donor_operator     VARCHAR(80) NOT NULL,
    recipient_operator VARCHAR(80) NOT NULL,
    plan_code          VARCHAR(80) NOT NULL,
    sim_iccid          VARCHAR(64),
    status             VARCHAR(50) NOT NULL,
    rejection_reason   VARCHAR(500),
    correlation_id     VARCHAR(100) NOT NULL,
    created_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at         TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    completed_at       TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_mnp_port_request_customer ON mnp_port_request(customer_id);
CREATE INDEX IF NOT EXISTS idx_mnp_port_request_status ON mnp_port_request(status);
CREATE INDEX IF NOT EXISTS idx_mnp_port_request_msisdn ON mnp_port_request(msisdn);
