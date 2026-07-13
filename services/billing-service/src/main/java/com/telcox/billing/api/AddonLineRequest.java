package com.telcox.billing.api;

import com.telcox.billing.domain.InvoiceItemType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;

import java.math.BigDecimal;

/**
 * BILL-02 / FR-22: Bir faturaya elle addon/VAS kalemi eklemek icin istek.
 * (Otomatik akiste bu bilgi subscription-service'teki abonelik addon'larindan
 * gelir; event-driven bir "addon projection" hazir olana kadar bu endpoint
 * manuel/entegrasyon amaclidir.)
 */
public record AddonLineRequest(
        @NotNull InvoiceItemType itemType,
        @NotBlank String productCode,
        @NotBlank String description,
        @NotNull @Positive BigDecimal quantity,
        @NotNull BigDecimal unitPrice,
        String taxRateCode
) {
}
