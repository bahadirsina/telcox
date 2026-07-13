package com.telcox.common.cache;

public final class CacheKeyPrefix {
    private CacheKeyPrefix() {}

    public static final String SUB_ORDER_PROJECTION = "subscription:order-projection:";
    public static final String SUB_PAYMENT_PROJECTION = "subscription:payment-projection:";
    public static final String BILLING_USAGE_PROJECTION = "billing:usage-projection:";
    public static final String BILLING_SUBSCRIPTION_PROJECTION = "billing:subscription-projection:";
    public static final String BFF_DASHBOARD_SUMMARY = "bff:dashboard-summary:";
    public static final String BFF_OPERATION_STATUS = "bff:operation-status:";

    public static String buildKey(String prefix, Object id) {
        return prefix + id;
    }
}
