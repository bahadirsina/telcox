package com.telcox.billing.repository;

import com.telcox.billing.domain.Invoice;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface InvoiceRepository extends JpaRepository<Invoice, UUID> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findAllByOrderByCreatedAtDesc();

    List<Invoice> findByBillingAccountIdOrderByCreatedAtDesc(UUID billingAccountId);

    Optional<Invoice> findBySubscriptionIdAndBillingCycleId(UUID subscriptionId, UUID billingCycleId);

    boolean existsByInvoiceNumber(String invoiceNumber);
}
