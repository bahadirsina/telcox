package com.telcox.billing.domain;

/**
 * BILL-01 / FR-21: Fatura donemi durumu. ER: BILLING_SERVICE_BILLING_CYCLE.status
 */
public enum CycleStatus {
    OPEN,
    CLOSED,
    INVOICED
}
