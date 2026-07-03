CREATE TABLE order_projection (
    id              BIGSERIAL PRIMARY KEY,
    order_id        UUID NOT NULL UNIQUE,
    customer_id     UUID NOT NULL,
    tariff_id       UUID,
    order_status    VARCHAR(30) NOT NULL,
    order_type      VARCHAR(30),
    created_at      TIMESTAMP NOT NULL,
    updated_at      TIMESTAMP NOT NULL
);
CREATE INDEX idx_order_projection_customer_id ON order_projection(customer_id);

CREATE TABLE payment_projection (
    id              BIGSERIAL PRIMARY KEY,
    payment_id      UUID NOT NULL UNIQUE,
    order_id        UUID,
    customer_id     UUID NOT NULL,
    amount          NUMERIC(12,2) NOT NULL,
    currency        VARCHAR(3) NOT NULL,
    payment_status  VARCHAR(30) NOT NULL,
    paid_at         TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL
);
CREATE INDEX idx_payment_projection_order_id ON payment_projection(order_id);
CREATE INDEX idx_payment_projection_customer_id ON payment_projection(customer_id);