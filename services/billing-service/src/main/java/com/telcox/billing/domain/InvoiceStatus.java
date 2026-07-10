package com.telcox.billing.domain;

/**
 * BILL-01/02/03 / FR-21/22/23: Fatura durumu. ER: BILLING_SERVICE_INVOICE.status
 */
public enum InvoiceStatus {
    DRAFT,
    ISSUED,
    PARTIALLY_PAID,
    PAID,
    OVERDUE,
    CANCELLED
}
