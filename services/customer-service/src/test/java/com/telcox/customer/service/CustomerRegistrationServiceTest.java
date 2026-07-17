package com.telcox.customer.service;

import com.telcox.common.exception.BusinessException;
import com.telcox.customer.api.CustomerRegistrationRequest;
import com.telcox.customer.api.CustomerResponse;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CustomerRegistrationServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerOutboxEventRepository outboxEventRepository;

    private CustomerRegistrationService service;

    @BeforeEach
    void setUp() {
        service = new CustomerRegistrationService(customerRepository, outboxEventRepository);
    }

    @Test
    void registerIndividual_shouldSaveCustomerAndOutboxEvent() {
        CustomerRegistrationRequest request = validRequest();
        when(customerRepository.existsByNationalIdAndDeletedAtIsNull(request.nationalId())).thenReturn(false);
        when(customerRepository.existsByCustomerNumber(any(String.class))).thenReturn(false);
        when(customerRepository.save(any(Customer.class))).thenAnswer(invocation -> invocation.getArgument(0));

        CustomerResponse response = service.registerIndividual(request, "corr-123");

        assertNotNull(response);
        assertEquals(request.firstName(), response.firstName());
        assertEquals(request.lastName(), response.lastName());
        assertEquals(CustomerStatus.PROSPECT, response.status());

        ArgumentCaptor<Customer> customerCaptor = ArgumentCaptor.forClass(Customer.class);
        verify(customerRepository).save(customerCaptor.capture());
        assertTrue(customerCaptor.getValue().getCustomerNumber().startsWith("CUS-"));

        verify(outboxEventRepository).save(any(CustomerOutboxEvent.class));
    }

    @Test
    void registerIndividual_shouldRejectDuplicateNationalId() {
        CustomerRegistrationRequest request = validRequest();
        when(customerRepository.existsByNationalIdAndDeletedAtIsNull(request.nationalId())).thenReturn(true);

        assertThrows(BusinessException.class, () -> service.registerIndividual(request, "corr-123"));

        verify(customerRepository, never()).save(any(Customer.class));
        verify(outboxEventRepository, never()).save(any(CustomerOutboxEvent.class));
    }

    @Test
    void list_shouldReturnOnlyRepositoryResults() {
        Customer customer = createCustomer();
        when(customerRepository.findByDeletedAtIsNullOrderByCreatedAtDesc()).thenReturn(List.of(customer));

        List<CustomerResponse> result = service.list();

        assertEquals(1, result.size());
        assertEquals(customer.getId(), result.getFirst().id());
    }

    @Test
    void get_shouldReturnCustomer_whenCustomerExists() {
        Customer customer = createCustomer();
        when(customerRepository.findByIdAndDeletedAtIsNull(customer.getId())).thenReturn(Optional.of(customer));

        CustomerResponse result = service.get(customer.getId());

        assertEquals(customer.getId(), result.id());
    }

    @Test
    void get_shouldThrowBusinessException_whenCustomerDoesNotExist() {
        UUID customerId = UUID.randomUUID();
        when(customerRepository.findByIdAndDeletedAtIsNull(customerId)).thenReturn(Optional.empty());

        assertThrows(BusinessException.class, () -> service.get(customerId));
    }

    @Test
    void approveKyc_shouldActivateCustomerAndPublishEvent() {
        Customer customer = createCustomer();
        when(customerRepository.findByIdAndDeletedAtIsNull(customer.getId())).thenReturn(Optional.of(customer));

        CustomerResponse result = service.approveKyc(customer.getId(), new KycDecisionRequest("Belgeler uygun"));

        assertEquals(CustomerStatus.ACTIVE, result.status());
        assertEquals("Belgeler uygun", result.statusReason());
        verify(outboxEventRepository).save(any(CustomerOutboxEvent.class));
    }

    @Test
    void rejectKyc_shouldCloseCustomerAndPublishEvent() {
        Customer customer = createCustomer();
        when(customerRepository.findByIdAndDeletedAtIsNull(customer.getId())).thenReturn(Optional.of(customer));

        CustomerResponse result = service.rejectKyc(customer.getId(), new KycDecisionRequest("Belge gecersiz"));

        assertEquals(CustomerStatus.CLOSED, result.status());
        assertEquals("Belge gecersiz", result.statusReason());
        verify(outboxEventRepository).save(any(CustomerOutboxEvent.class));
    }

    private CustomerRegistrationRequest validRequest() {
        return new CustomerRegistrationRequest(
                UUID.randomUUID(),
                "Ayse",
                "Yilmaz",
                "12345678901",
                LocalDate.of(2000, 1, 15),
                CustomerSegment.MASS
        );
    }

    private Customer createCustomer() {
        return Customer.registerIndividual(
                UUID.randomUUID(),
                "CUS-123456",
                "Ayse",
                "Yilmaz",
                "12345678901",
                LocalDate.of(2000, 1, 15),
                CustomerSegment.MASS
        );
    }
}
