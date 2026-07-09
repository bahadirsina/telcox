package com.telcox.productcatalog.api;

import com.telcox.productcatalog.domain.Product;
import com.telcox.productcatalog.domain.ProductStatus;
import com.telcox.productcatalog.domain.ProductType;

import java.time.LocalDateTime;
import java.util.UUID;

public record ProductResponse(
        UUID id,
        String code,
        String name,
        String description,
        ProductType productType,
        ProductStatus status,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static ProductResponse from(Product product) {
        return new ProductResponse(
                product.getId(), product.getCode(), product.getName(), product.getDescription(),
                product.getProductType(), product.getStatus(), product.getCreatedAt(), product.getUpdatedAt()
        );
    }
}
