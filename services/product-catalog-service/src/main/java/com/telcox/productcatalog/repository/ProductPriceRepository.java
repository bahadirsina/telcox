package com.telcox.productcatalog.repository;

import com.telcox.productcatalog.domain.ProductPrice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ProductPriceRepository extends JpaRepository<ProductPrice, UUID> {
    List<ProductPrice> findByProductIdOrderByValidFromDesc(UUID productId);

    /** validTo IS NULL olan, yani halen acik/gecerli fiyat kaydi. */
    Optional<ProductPrice> findByProductIdAndValidToIsNull(UUID productId);
}
