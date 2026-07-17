package com.telcox.customer.service;

import com.telcox.common.exception.BusinessException;
import com.telcox.customer.api.CustomerRegistrationRequest;
import com.telcox.customer.api.KycDecisionRequest;
import com.telcox.customer.domain.Customer;
import com.telcox.customer.domain.CustomerOutboxEvent;
import com.telcox.customer.domain.CustomerSegment;
import com.telcox.customer.domain.CustomerStatus;
import com.telcox.customer.repository.CustomerOutboxEventRepository;
import com.telcox.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerRegistrationServiceTest {

    @Mock CustomerRepository customerRepository;
    @Mock CustomerOutboxEventRepository outboxEventRepository;
    private CustomerRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CustomerRegistrationService(customerRepository, outboxEventRepository);
    }

    private CustomerRegistrationRequest request() {
        return new CustomerRegistrationRequest(UUID.randomUUID(), "Ada", "Lovelace",
                "10000000146", LocalDate.of(1995, 1, 1), CustomerSegment.VIP);
    }

    @Test
    void shouldRegisterCustomerAndWriteOutboxEvent() {
        when(customerRepository.existsByNationalIdAndDeletedAtIsNull("10000000146")).thenReturn(false);
        when(customerRepository.existsByCustomerNumber(anyString())).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.registerIndividual(request(), "corr-1");

        assertEquals(CustomerStatus.PROSPECT, response.status());
        verify(customerRepository).save(any(Customer.class));
        verify(outboxEventRepository).save(any(CustomerOutboxEvent.class));
    }

    @Test
    void shouldRejectDuplicateNationalId() {
        when(customerRepository.existsByNationalIdAndDeletedAtIsNull("10000000146")).thenReturn(true);
        assertThrows(BusinessException.class, () -> service.registerIndividual(request(), "corr-1"));
        verify(customerRepository, never()).save(any());
        verify(outboxEventRepository, never()).save(any());
    }

    @Test
    void shouldApproveKycAndWriteOutboxEvent() {
        UUID id = UUID.randomUUID();
        Customer customer = Customer.registerIndividual(UUID.randomUUID(), "CUS-100001", "Ada", "Lovelace",
                "10000000146", LocalDate.of(1995, 1, 1), CustomerSegment.MASS);
        when(customerRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.of(customer));

        var response = service.approveKyc(id, new KycDecisionRequest("verified"));

        assertEquals(CustomerStatus.ACTIVE, response.status());
        verify(outboxEventRepository).save(any(CustomerOutboxEvent.class));
    }

    @Test
    void shouldFailWhenCustomerDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(customerRepository.findByIdAndDeletedAtIsNull(id)).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> service.get(id));
    }
}
