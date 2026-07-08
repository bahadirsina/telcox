package com.telcox.productcatalog.repository;

import com.telcox.productcatalog.domain.ProductCategory;
import com.telcox.productcatalog.domain.ProductCategoryId;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface ProductCategoryRepository extends JpaRepository<ProductCategory, ProductCategoryId> {
    List<ProductCategory> findById_ProductId(UUID productId);

    List<ProductCategory> findById_CategoryId(UUID categoryId);
}
