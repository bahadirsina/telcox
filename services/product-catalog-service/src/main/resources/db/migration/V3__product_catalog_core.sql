-- CAT-01 / CAT-02: product catalog core tables.

CREATE TABLE IF NOT EXISTS product_service_product (
    id              UUID PRIMARY KEY,
    code            VARCHAR(100) NOT NULL UNIQUE,
    name            VARCHAR(200) NOT NULL,
    description     TEXT,
    product_type    VARCHAR(20) NOT NULL,
    status          VARCHAR(20) NOT NULL,
    created_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    updated_at      TIMESTAMP NOT NULL DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_product_type CHECK (product_type IN ('PLAN', 'ADDON', 'DEVICE', 'SERVICE')),
    CONSTRAINT chk_product_status CHECK (status IN ('DRAFT', 'ACTIVE', 'RETIRED'))
);

CREATE INDEX IF NOT EXISTS idx_product_status_created
    ON product_service_product(status, created_at DESC);

CREATE TABLE IF NOT EXISTS product_service_price (
    id              UUID PRIMARY KEY,
    product_id      UUID NOT NULL,
    price           NUMERIC(19, 4) NOT NULL,
    currency        VARCHAR(3) NOT NULL,
    tax_included    BOOLEAN NOT NULL,
    valid_from      DATE NOT NULL,
    valid_to        DATE,
    CONSTRAINT fk_product_price_product
        FOREIGN KEY (product_id) REFERENCES product_service_product(id),
    CONSTRAINT chk_product_price_amount CHECK (price >= 0),
    CONSTRAINT chk_product_price_validity CHECK (valid_to IS NULL OR valid_to >= valid_from)
);

CREATE INDEX IF NOT EXISTS idx_product_price_product_validity
    ON product_service_price(product_id, valid_from DESC, valid_to);

CREATE TABLE IF NOT EXISTS product_service_plan (
    id                  UUID PRIMARY KEY,
    product_id          UUID NOT NULL UNIQUE,
    plan_type           VARCHAR(20) NOT NULL,
    commitment_months   INTEGER,
    monthly_price       NUMERIC(19, 4),
    currency            VARCHAR(3),
    valid_from          DATE,
    valid_to            DATE,
    CONSTRAINT fk_product_plan_product
        FOREIGN KEY (product_id) REFERENCES product_service_product(id),
    CONSTRAINT chk_product_plan_type CHECK (plan_type IN ('PREPAID', 'POSTPAID', 'HYBRID')),
    CONSTRAINT chk_product_plan_commitment CHECK (commitment_months IS NULL OR commitment_months >= 0),
    CONSTRAINT chk_product_plan_monthly_price CHECK (monthly_price IS NULL OR monthly_price >= 0),
    CONSTRAINT chk_product_plan_validity CHECK (valid_to IS NULL OR valid_from IS NULL OR valid_to >= valid_from)
);

CREATE TABLE IF NOT EXISTS product_service_plan_feature (
    id              UUID PRIMARY KEY,
    plan_id         UUID NOT NULL,
    feature_type    VARCHAR(20) NOT NULL,
    allowance       NUMERIC(19, 4),
    unit            VARCHAR(20),
    is_unlimited    BOOLEAN NOT NULL,
    CONSTRAINT fk_product_plan_feature_plan
        FOREIGN KEY (plan_id) REFERENCES product_service_plan(id),
    CONSTRAINT chk_product_plan_feature_type CHECK (feature_type IN ('VOICE_MIN', 'SMS_COUNT', 'DATA_MB', 'INTL_MIN')),
    CONSTRAINT chk_product_plan_feature_allowance CHECK (allowance IS NULL OR allowance >= 0)
);

CREATE INDEX IF NOT EXISTS idx_product_plan_feature_plan
    ON product_service_plan_feature(plan_id);

CREATE TABLE IF NOT EXISTS product_service_category (
    id                  UUID PRIMARY KEY,
    code                VARCHAR(100) NOT NULL UNIQUE,
    name                VARCHAR(200) NOT NULL,
    parent_category_id  UUID,
    CONSTRAINT fk_product_category_parent
        FOREIGN KEY (parent_category_id) REFERENCES product_service_category(id)
);

CREATE INDEX IF NOT EXISTS idx_product_category_parent
    ON product_service_category(parent_category_id);

CREATE TABLE IF NOT EXISTS product_service_product_category (
    product_id      UUID NOT NULL,
    category_id     UUID NOT NULL,
    PRIMARY KEY (product_id, category_id),
    CONSTRAINT fk_product_category_product
        FOREIGN KEY (product_id) REFERENCES product_service_product(id),
    CONSTRAINT fk_product_category_category
        FOREIGN KEY (category_id) REFERENCES product_service_category(id)
);
