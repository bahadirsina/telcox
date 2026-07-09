package com.telcox.productcatalog.api;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;
import java.time.LocalDate;

/**
 * CAT-01 / FR-06: Yeni fiyat tanimlama istegi. validFrom belirtilmezse "bugun" varsayilir.
 * Bu istek geldiginde mevcut acik fiyat kaydi otomatik kapatilir (effective-date modeli).
 */
public record ProductPriceRequest(
        @NotNull @Positive BigDecimal price,
        @NotBlank String currency,
        boolean taxIncluded,
        LocalDate validFrom
) {
}
