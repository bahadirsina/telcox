package com.telcox.productcatalog.api;

import com.telcox.productcatalog.domain.FeatureType;
import com.telcox.productcatalog.domain.PlanFeature;

import java.math.BigDecimal;
import java.util.UUID;

public record PlanFeatureResponse(
        UUID id,
        UUID planId,
        FeatureType featureType,
        BigDecimal allowance,
        String unit,
        boolean isUnlimited
) {
    public static PlanFeatureResponse from(PlanFeature feature) {
        return new PlanFeatureResponse(feature.getId(), feature.getPlanId(), feature.getFeatureType(),
                feature.getAllowance(), feature.getUnit(), feature.isUnlimited());
    }
}
