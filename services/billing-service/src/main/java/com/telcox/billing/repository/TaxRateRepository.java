package com.telcox.billing.repository;

import com.telcox.billing.domain.TaxRate;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface TaxRateRepository extends JpaRepository<TaxRate, UUID> {
    Optional<TaxRate> findByCode(String code);

    List<TaxRate> findByValidToIsNull();
}
