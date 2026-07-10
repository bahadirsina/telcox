package com.telcox.billing.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * BILL-02 / FR-22: Kullanim asimi (overage) hesaplama istegi.
 * usageType, UsageProjection.usageType ile eslesmelidir (ör. "DATA_MB").
 * allowance, o donem icin tarifenin tanidigi kota (PlanFeature.allowance,
 * ayni ADR-0006 notu geregi lokalde tutulmadigindan disaridan verilir).
 */
public record OverageRequest(
        @NotBlank String usageType,
        @NotNull BigDecimal allowance,
        @NotNull BigDecimal overageUnitPrice,
        String taxRateCode
) {
}
