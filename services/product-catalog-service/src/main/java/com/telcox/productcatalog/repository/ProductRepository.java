package com.telcox.productcatalog.repository;

import com.telcox.productcatalog.domain.Product;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductRepository extends JpaRepository<Product, UUID> {
    Optional<Product> findByCode(String code);

    List<Product> findAllByOrderByCreatedAtDesc();

    boolean existsByCode(String code);
}
