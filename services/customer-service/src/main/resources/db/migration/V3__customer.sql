-- =============================================================================
-- FR-01/02: customer_service_customer tablosu (bireysel musteri kaydi + KYC state)
-- ER: db.sql -> CUSTOMER_SERVICE_CUSTOMER
-- =============================================================================

CREATE TABLE IF NOT EXISTS customer_service_customer (
    id                UUID PRIMARY KEY,
    identity_user_id  UUID UNIQUE,
    customer_number   VARCHAR(50) NOT NULL UNIQUE,
    customer_type     VARCHAR(20) NOT NULL,
    first_name        VARCHAR(100),
    last_name         VARCHAR(100),
    company_name      VARCHAR(200),
    tax_number        VARCHAR(50),
    national_id       VARCHAR(255) NOT NULL,
    date_of_birth     DATE,
    segment           VARCHAR(20),
    status            VARCHAR(20) NOT NULL,
    status_reason     VARCHAR(500),
    created_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at        TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    deleted_at        TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_customer_national_id ON customer_service_customer(national_id);
CREATE INDEX IF NOT EXISTS idx_customer_status ON customer_service_customer(status);
CREATE INDEX IF NOT EXISTS idx_customer_deleted_at ON customer_service_customer(deleted_at);
