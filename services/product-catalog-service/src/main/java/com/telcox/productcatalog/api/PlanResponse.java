package com.telcox.productcatalog.api;

import com.telcox.productcatalog.domain.Plan;
import com.telcox.productcatalog.domain.PlanType;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public record PlanResponse(
        UUID id,
        UUID productId,
        PlanType planType,
        Integer commitmentMonths,
        BigDecimal monthlyPrice,
        String currency,
        LocalDate validFrom,
        LocalDate validTo,
        List<PlanFeatureResponse> features
) {
    public static PlanResponse from(Plan plan, List<PlanFeatureResponse> features) {
        return new PlanResponse(plan.getId(), plan.getProductId(), plan.getPlanType(), plan.getCommitmentMonths(),
                plan.getMonthlyPrice(), plan.getCurrency(), plan.getValidFrom(), plan.getValidTo(), features);
    }
}
