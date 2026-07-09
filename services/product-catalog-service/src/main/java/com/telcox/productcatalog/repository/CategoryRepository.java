package com.telcox.productcatalog.repository;

import com.telcox.productcatalog.domain.Category;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CategoryRepository extends JpaRepository<Category, UUID> {
    List<Category> findByParentCategoryId(UUID parentCategoryId);

    List<Category> findByParentCategoryIdIsNull();
}
