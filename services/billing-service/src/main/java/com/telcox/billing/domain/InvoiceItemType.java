package com.telcox.billing.domain;

/**
 * BILL-02 / FR-22: Fatura kalem tipi. db.sql seemasinda ayri bir kolon olarak
 * yok ama InvoiceItem.description/productCode ile birlikte is mantiginda
 * kalemleri siniflandirmak icin kullanilir (persist edilmez, sadece servis
 * katmaninda DTO/hesaplama amacli).
 */
public enum InvoiceItemType {
    BASE_PLAN,
    ADDON,
    VAS,
    OVERAGE
}
