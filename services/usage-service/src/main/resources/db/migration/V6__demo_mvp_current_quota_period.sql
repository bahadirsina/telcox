UPDATE usage_service_quota
SET period_start = date_trunc('month', now()),
    period_end = date_trunc('month', now()) + interval '1 month',
    updated_at = now()
WHERE id IN (
    '66666666-6666-4666-8666-000000010481',
    '66666666-6666-4666-8666-000000010482',
    '66666666-6666-4666-8666-000000010483',
    '66666666-6666-4666-8666-000000010461',
    '66666666-6666-4666-8666-000000010398'
);
