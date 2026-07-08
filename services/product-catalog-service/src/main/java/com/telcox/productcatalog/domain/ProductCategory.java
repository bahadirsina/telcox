package com.telcox.productcatalog.domain;

import jakarta.persistence.EmbeddedId;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;

/**
 * CAT-02 / FR-08: Urun-kategori iliskisi (many-to-many join tablosu).
 * ER: db.sql -> PRODUCT_SERVICE_PRODUCT_CATEGORY
 */
@Entity
@Table(name = "product_service_product_category")
public class ProductCategory {

    @EmbeddedId
    private ProductCategoryId id;

    protected ProductCategory() {
    }

    public ProductCategory(ProductCategoryId id) {
        this.id = id;
    }

    public static ProductCategory of(java.util.UUID productId, java.util.UUID categoryId) {
        return new ProductCategory(new ProductCategoryId(productId, categoryId));
    }

    public ProductCategoryId getId() {
        return id;
    }
}
