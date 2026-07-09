package com.telcox.productcatalog.api;

import com.telcox.productcatalog.domain.PlanType;
import jakarta.validation.constraints.NotNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

/**
 * CAT-02 / FR-07/08: Yeni tarife (plan) versiyonu olusturma istegi.
 * productCode: yeni versiyon icin Product.code (ör. "PLAN_4G_V2") - CAT-01'deki
 * ProductCatalogService.createProduct ile once Product olusturulmus olmalidir.
 */
public record PlanRequest(
        @NotNull PlanType planType,
        Integer commitmentMonths,
        BigDecimal monthlyPrice,
        String currency,
        LocalDate validFrom,
        List<PlanFeatureRequest> features
) {
}
