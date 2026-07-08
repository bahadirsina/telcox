package com.telcox.customer.repository;

import com.telcox.customer.domain.CustomerDocument;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.UUID;

public interface CustomerDocumentRepository extends JpaRepository<CustomerDocument, UUID> {
    List<CustomerDocument> findByCustomerId(UUID customerId);
}
