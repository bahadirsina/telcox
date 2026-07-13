package com.telcox.billing.repository;

import com.telcox.billing.domain.BillingCycle;
import com.telcox.billing.domain.CycleStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface BillingCycleRepository extends JpaRepository<BillingCycle, UUID> {
    Optional<BillingCycle> findByBillingAccountIdAndPeriodStartAndPeriodEnd(UUID billingAccountId, LocalDate periodStart, LocalDate periodEnd);

    List<BillingCycle> findByStatus(CycleStatus status);
}
