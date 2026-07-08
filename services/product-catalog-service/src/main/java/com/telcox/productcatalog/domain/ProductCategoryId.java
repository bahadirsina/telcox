package com.telcox.productcatalog.domain;

import jakarta.persistence.Embeddable;

import java.io.Serializable;
import java.util.Objects;
import java.util.UUID;

/**
 * CAT-02: PRODUCT_SERVICE_PRODUCT_CATEGORY tablosunun bilesik (composite) anahtari.
 */
@Embeddable
public class ProductCategoryId implements Serializable {

    private UUID productId;
    private UUID categoryId;

    protected ProductCategoryId() {
    }

    public ProductCategoryId(UUID productId, UUID categoryId) {
        this.productId = productId;
        this.categoryId = categoryId;
    }

    public UUID getProductId() {
        return productId;
    }

    public UUID getCategoryId() {
        return categoryId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof ProductCategoryId that)) {
            return false;
        }
        return Objects.equals(productId, that.productId) && Objects.equals(categoryId, that.categoryId);
    }

    @Override
    public int hashCode() {
        return Objects.hash(productId, categoryId);
    }
}
