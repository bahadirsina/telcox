package com.telcox.customer.service;

import com.telcox.customer.domain.Customer;
import com.telcox.customer.domain.CustomerAuditLog;
import com.telcox.customer.domain.CustomerSegment;
import com.telcox.customer.repository.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerAuditServiceTest {

    @Mock CustomerRepository customerRepository;
    @Mock CustomerConsentRepository consentRepository;
    @Mock CustomerNoteRepository noteRepository;
    @Mock CustomerAuditLogRepository auditLogRepository;
    private CustomerAuditService service;

    @BeforeEach
    void setUp() {
        service = new CustomerAuditService(customerRepository, consentRepository, noteRepository, auditLogRepository);
    }

    @Test
    void shouldSoftDeleteAndCreateAuditLog() {
        UUID id = UUID.randomUUID();
        Customer customer = Customer.registerIndividual(UUID.randomUUID(), "CUS-100001", "Ada", "Lovelace",
                "10000000146", LocalDate.of(1995, 1, 1), CustomerSegment.MASS);
        when(customerRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(customer));

        service.softDeleteCustomer(id, UUID.randomUUID(), "KVKK request");

        assertTrue(customer.isDeleted());
        verify(auditLogRepository).save(any(CustomerAuditLog.class));
    }
}
