package com.telcox.customer.service;

import com.telcox.common.exception.BusinessException;
import com.telcox.customer.api.AddressRequest;
import com.telcox.customer.api.ContactRequest;
import com.telcox.customer.api.DocumentUploadRequest;
import com.telcox.customer.domain.AddressType;
import com.telcox.customer.domain.ContactType;
import com.telcox.customer.domain.Customer;
import com.telcox.customer.domain.CustomerAddress;
import com.telcox.customer.domain.CustomerContact;
import com.telcox.customer.domain.CustomerDocument;
import com.telcox.customer.domain.CustomerSegment;
import com.telcox.customer.domain.DocumentType;
import com.telcox.customer.repository.CustomerAddressRepository;
import com.telcox.customer.repository.CustomerContactRepository;
import com.telcox.customer.repository.CustomerDocumentRepository;
import com.telcox.customer.repository.CustomerRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustomerProfileServiceTest {

    @Mock
    private CustomerRepository customerRepository;

    @Mock
    private CustomerAddressRepository addressRepository;

    @Mock
    private CustomerContactRepository contactRepository;

    @Mock
    private CustomerDocumentRepository documentRepository;

    private CustomerProfileService service;
    private UUID customerId;
    private Customer customer;

    @BeforeEach
    void setUp() {
        service = new CustomerProfileService(
                customerRepository,
                addressRepository,
                contactRepository,
                documentRepository
        );

        customerId = UUID.randomUUID();

        customer = Customer.registerIndividual(
                UUID.randomUUID(),
                "CUS-100001",
                "Ada",
                "Lovelace",
                "10000000146",
                LocalDate.of(1995, 1, 1),
                CustomerSegment.MASS
        );
    }

    @Test
    void shouldAddAddress() {
        when(customerRepository.findByIdAndDeletedAtIsNull(customerId))
                .thenReturn(Optional.of(customer));

        when(addressRepository.findByCustomerId(customerId))
                .thenReturn(List.of());

        when(addressRepository.save(any(CustomerAddress.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        AddressRequest request = new AddressRequest(
                AddressType.CONTACT,
                "TR",
                "Istanbul",
                "Kadikoy",
                "Street",
                "10",
                "34000",
                true
        );

        service.addAddress(customerId, request);

        verify(customerRepository)
                .findByIdAndDeletedAtIsNull(customerId);

        verify(addressRepository)
                .findByCustomerId(customerId);

        verify(addressRepository)
                .save(any(CustomerAddress.class));
    }

    @Test
    void shouldAddContact() {
        when(customerRepository.findByIdAndDeletedAtIsNull(customerId))
                .thenReturn(Optional.of(customer));

        when(contactRepository.findByCustomerId(customerId))
                .thenReturn(List.of());

        when(contactRepository.save(any(CustomerContact.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        ContactRequest request = new ContactRequest(
                ContactType.EMAIL,
                "ada@example.com",
                true
        );

        service.addContact(customerId, request);

        verify(customerRepository)
                .findByIdAndDeletedAtIsNull(customerId);

        verify(contactRepository)
                .findByCustomerId(customerId);

        verify(contactRepository)
                .save(any(CustomerContact.class));
    }

    @Test
    void shouldUploadDocument() {
        when(customerRepository.findByIdAndDeletedAtIsNull(customerId))
                .thenReturn(Optional.of(customer));

        when(documentRepository.save(any(CustomerDocument.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        DocumentUploadRequest request = new DocumentUploadRequest(
                DocumentType.ID_CARD,
                "DOC-1",
                "file://id-card",
                UUID.randomUUID()
        );

        service.uploadDocument(customerId, request);

        verify(customerRepository)
                .findByIdAndDeletedAtIsNull(customerId);

        verify(documentRepository)
                .save(any(CustomerDocument.class));
    }

    @Test
    void shouldRejectOperationForMissingCustomer() {
        UUID missingCustomerId = UUID.randomUUID();

        when(customerRepository.findByIdAndDeletedAtIsNull(missingCustomerId))
                .thenReturn(Optional.empty());

        assertThrows(
                BusinessException.class,
                () -> service.listAddresses(missingCustomerId)
        );

        verify(customerRepository)
                .findByIdAndDeletedAtIsNull(missingCustomerId);
    }
}
