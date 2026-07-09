package com.telcox.productcatalog.repository;

import com.telcox.productcatalog.domain.ProductOutboxEvent;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.UUID;

public interface ProductOutboxEventRepository extends JpaRepository<ProductOutboxEvent, UUID> {
}
