INSERT INTO product_service_product (
    id, code, name, description, product_type, status, created_at, updated_at
)
VALUES
    ('44444444-4444-4444-8444-000000000020', 'ATL-20-P', 'Atlas 20 GB', 'Postpaid mobile plan with 20 GB data allowance.', 'PLAN', 'ACTIVE', TIMESTAMP '2026-06-01 09:00:00', TIMESTAMP '2026-06-01 09:00:00'),
    ('44444444-4444-4444-8444-000000000040', 'ATL-40-P', 'Atlas 40 GB', 'Postpaid mobile plan with 40 GB data allowance.', 'PLAN', 'ACTIVE', TIMESTAMP '2026-06-01 09:00:00', TIMESTAMP '2026-06-01 09:00:00'),
    ('44444444-4444-4444-8444-000000000999', 'ATL-UNL-P', 'Atlas Limitsiz', 'Premium postpaid mobile plan with unlimited usage policy.', 'PLAN', 'DRAFT', TIMESTAMP '2026-06-01 09:00:00', TIMESTAMP '2026-06-01 09:00:00'),
    ('44444444-4444-4444-8444-000000001020', 'ADD-NIGHT-20GB', 'Gece 20 GB', 'Night data add-on valid between 00:00 and 06:00.', 'ADDON', 'ACTIVE', TIMESTAMP '2026-06-01 09:00:00', TIMESTAMP '2026-06-01 09:00:00'),
    ('44444444-4444-4444-8444-000000001005', 'ADD-ROAM-5GB', 'Yurt Disi 5 GB', 'Roaming data add-on valid in selected countries.', 'ADDON', 'ACTIVE', TIMESTAMP '2026-06-01 09:00:00', TIMESTAMP '2026-06-01 09:00:00'),
    ('44444444-4444-4444-8444-000000001777', 'VAS-MUSIC-PREMIUM', 'Muzik Premium', 'Premium music value added service.', 'SERVICE', 'ACTIVE', TIMESTAMP '2026-06-01 09:00:00', TIMESTAMP '2026-06-01 09:00:00')
ON CONFLICT (id) DO NOTHING;

INSERT INTO product_service_price (
    id, product_id, price, currency, tax_included, valid_from, valid_to
)
SELECT price_id, product.id, price, 'TRY', TRUE, DATE '2026-06-01', NULL
FROM (
    VALUES
        ('44444444-4444-4444-8444-000000100020'::UUID, 'ATL-20-P', 649.9000),
        ('44444444-4444-4444-8444-000000100040'::UUID, 'ATL-40-P', 899.9000),
        ('44444444-4444-4444-8444-000000100999'::UUID, 'ATL-UNL-P', 1499.9000),
        ('44444444-4444-4444-8444-000000101020'::UUID, 'ADD-NIGHT-20GB', 129.9000),
        ('44444444-4444-4444-8444-000000101005'::UUID, 'ADD-ROAM-5GB', 249.9000),
        ('44444444-4444-4444-8444-000000101777'::UUID, 'VAS-MUSIC-PREMIUM', 69.9000)
) AS seed(price_id, product_code, price)
JOIN product_service_product product ON product.code = seed.product_code
ON CONFLICT (id) DO NOTHING;

INSERT INTO product_service_plan (
    id, product_id, plan_type, commitment_months, monthly_price, currency, valid_from, valid_to
)
SELECT plan_id, product.id, 'POSTPAID', 12, monthly_price, 'TRY', DATE '2026-06-01', NULL
FROM (
    VALUES
        ('44444444-4444-4444-8444-000000200020'::UUID, 'ATL-20-P', 649.9000),
        ('44444444-4444-4444-8444-000000200040'::UUID, 'ATL-40-P', 899.9000),
        ('44444444-4444-4444-8444-000000200999'::UUID, 'ATL-UNL-P', 1499.9000)
) AS seed(plan_id, product_code, monthly_price)
JOIN product_service_product product ON product.code = seed.product_code
ON CONFLICT (id) DO NOTHING;

INSERT INTO product_service_plan_feature (
    id, plan_id, feature_type, allowance, unit, is_unlimited
)
SELECT feature_id, plan.id, feature_type, allowance, unit, is_unlimited
FROM (
    VALUES
        ('44444444-4444-4444-8444-000000300201'::UUID, 'ATL-20-P', 'DATA_MB', 20480.0000, 'MB', FALSE),
        ('44444444-4444-4444-8444-000000300202'::UUID, 'ATL-20-P', 'VOICE_MIN', 1000.0000, 'MIN', FALSE),
        ('44444444-4444-4444-8444-000000300203'::UUID, 'ATL-20-P', 'SMS_COUNT', 1000.0000, 'SMS', FALSE),
        ('44444444-4444-4444-8444-000000300401'::UUID, 'ATL-40-P', 'DATA_MB', 40960.0000, 'MB', FALSE),
        ('44444444-4444-4444-8444-000000300402'::UUID, 'ATL-40-P', 'VOICE_MIN', 2000.0000, 'MIN', FALSE),
        ('44444444-4444-4444-8444-000000300403'::UUID, 'ATL-40-P', 'SMS_COUNT', 1000.0000, 'SMS', FALSE),
        ('44444444-4444-4444-8444-000000309991'::UUID, 'ATL-UNL-P', 'DATA_MB', NULL, 'MB', TRUE),
        ('44444444-4444-4444-8444-000000309992'::UUID, 'ATL-UNL-P', 'VOICE_MIN', NULL, 'MIN', TRUE),
        ('44444444-4444-4444-8444-000000309993'::UUID, 'ATL-UNL-P', 'SMS_COUNT', NULL, 'SMS', TRUE)
) AS seed(feature_id, product_code, feature_type, allowance, unit, is_unlimited)
JOIN product_service_product product ON product.code = seed.product_code
JOIN product_service_plan plan ON plan.product_id = product.id
ON CONFLICT (id) DO NOTHING;

INSERT INTO product_service_category (
    id, code, name, parent_category_id
)
VALUES
    ('55555555-5555-4555-8555-000000000001', 'MOBILE_PLANS', 'Mobil Tarifeler', NULL),
    ('55555555-5555-4555-8555-000000000002', 'MOBILE_ADDONS', 'Mobil Ek Paketler', NULL),
    ('55555555-5555-4555-8555-000000000003', 'VALUE_ADDED_SERVICES', 'Katma Degerli Servisler', NULL)
ON CONFLICT (id) DO NOTHING;

INSERT INTO product_service_product_category (product_id, category_id)
SELECT product.id, category.id
FROM product_service_product product
JOIN product_service_category category ON category.code =
    CASE
        WHEN product.product_type = 'PLAN' THEN 'MOBILE_PLANS'
        WHEN product.product_type = 'ADDON' THEN 'MOBILE_ADDONS'
        ELSE 'VALUE_ADDED_SERVICES'
    END
WHERE product.code IN ('ATL-20-P', 'ATL-40-P', 'ATL-UNL-P', 'ADD-NIGHT-20GB', 'ADD-ROAM-5GB', 'VAS-MUSIC-PREMIUM')
ON CONFLICT (product_id, category_id) DO NOTHING;
