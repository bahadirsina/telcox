package com.telcox.billing.api;

import java.time.YearMonth;

/**
 * BILL-01: Manuel bill-run tetikleme istegi. period belirtilmezse
 * bir onceki ay kullanilir (otomatik Quartz job'un davranisiyla ayni).
 */
public record BillRunRequest(YearMonth period) {
}
