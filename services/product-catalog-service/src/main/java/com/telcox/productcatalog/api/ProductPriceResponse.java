package com.telcox.productcatalog.api;

import com.telcox.productcatalog.domain.ProductPrice;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

public record ProductPriceResponse(
        UUID id,
        UUID productId,
        BigDecimal price,
        String currency,
        boolean taxIncluded,
        LocalDate validFrom,
        LocalDate validTo
) {
    public static ProductPriceResponse from(ProductPrice price) {
        return new ProductPriceResponse(
                price.getId(), price.getProductId(), price.getPrice(), price.getCurrency(),
                price.isTaxIncluded(), price.getValidFrom(), price.getValidTo()
        );
    }
}
