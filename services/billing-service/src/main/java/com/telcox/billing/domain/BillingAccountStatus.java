package com.telcox.billing.domain;

/**
 * BILL-01 / FR-21: Fatura hesabi durumu. ER: BILLING_SERVICE_BILLING_ACCOUNT.status
 */
public enum BillingAccountStatus {
    ACTIVE,
    SUSPENDED,
    CLOSED
}
