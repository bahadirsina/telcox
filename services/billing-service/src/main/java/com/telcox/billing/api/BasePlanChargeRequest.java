package com.telcox.billing.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;

/**
 * BILL-02 / FR-22: Tarife (base plan) aylik ucretini faturaya isleme istegi.
 *
 * NOT: billing-service su an tarife fiyatini lokal olarak tutmuyor (bkz.
 * ADR-0006 ve product-catalog-service.ProductPrice). Gercek entegrasyonda
 * bu tutar, product-catalog-service'ten Kafka event'i ile senkronize edilen
 * bir "tariff-price projection" tablosundan otomatik okunmali. O projeksiyon
 * hazir olana kadar tutar bu istekle disaridan/manuel saglanir.
 */
public record BasePlanChargeRequest(
        @NotBlank String tariffCode,
        @NotNull BigDecimal monthlyPrice,
        String taxRateCode
) {
}
