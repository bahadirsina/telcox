package com.telcox.customer.service;

import com.telcox.common.exception.BusinessException;
import com.telcox.common.exception.ErrorCode;
import com.telcox.customer.api.AddressRequest;
import com.telcox.customer.api.AddressResponse;
import com.telcox.customer.api.ContactRequest;
import com.telcox.customer.api.ContactResponse;
import com.telcox.customer.api.DocumentResponse;
import com.telcox.customer.api.DocumentUploadRequest;
import com.telcox.customer.domain.CustomerAddress;
import com.telcox.customer.domain.CustomerContact;
import com.telcox.customer.domain.CustomerDocument;
import com.telcox.customer.repository.CustomerAddressRepository;
import com.telcox.customer.repository.CustomerContactRepository;
import com.telcox.customer.repository.CustomerDocumentRepository;
import com.telcox.customer.repository.CustomerRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
public class CustomerProfileService {

    private final CustomerRepository customerRepository;
    private final CustomerAddressRepository addressRepository;
    private final CustomerContactRepository contactRepository;
    private final CustomerDocumentRepository documentRepository;

    public CustomerProfileService(CustomerRepository customerRepository,
                                  CustomerAddressRepository addressRepository,
                                  CustomerContactRepository contactRepository,
                                  CustomerDocumentRepository documentRepository) {
        this.customerRepository = customerRepository;
        this.addressRepository = addressRepository;
        this.contactRepository = contactRepository;
        this.documentRepository = documentRepository;
    }

    // ---- Address ----

    @Transactional
    public AddressResponse addAddress(UUID customerId, AddressRequest request) {
        requireCustomerExists(customerId);
        if (request.isDefault()) {
            addressRepository.findByCustomerId(customerId).forEach(CustomerAddress::unmarkAsDefault);
        }
        CustomerAddress address = new CustomerAddress(
                customerId, request.addressType(), request.country(), request.city(), request.district(),
                request.street(), request.buildingNo(), request.postalCode(), request.isDefault()
        );
        return AddressResponse.from(addressRepository.save(address));
    }

    public List<AddressResponse> listAddresses(UUID customerId) {
        requireCustomerExists(customerId);
        return addressRepository.findByCustomerId(customerId).stream().map(AddressResponse::from).toList();
    }

    @Transactional
    public AddressResponse updateAddress(UUID customerId, UUID addressId, AddressRequest request) {
        CustomerAddress address = findAddress(customerId, addressId);
        if (request.isDefault() && !address.isDefault()) {
            addressRepository.findByCustomerId(customerId).forEach(CustomerAddress::unmarkAsDefault);
            address.markAsDefault();
        }
        address.update(request.country(), request.city(), request.district(), request.street(),
                request.buildingNo(), request.postalCode());
        return AddressResponse.from(address);
    }

    // ---- Contact ----

    @Transactional
    public ContactResponse addContact(UUID customerId, ContactRequest request) {
        requireCustomerExists(customerId);
        if (request.isPrimary()) {
            contactRepository.findByCustomerId(customerId).forEach(CustomerContact::unmarkAsPrimary);
        }
        CustomerContact contact = new CustomerContact(customerId, request.contactType(), request.contactValue(), request.isPrimary());
        return ContactResponse.from(contactRepository.save(contact));
    }

    public List<ContactResponse> listContacts(UUID customerId) {
        requireCustomerExists(customerId);
        return contactRepository.findByCustomerId(customerId).stream().map(ContactResponse::from).toList();
    }

    @Transactional
    public ContactResponse verifyContact(UUID customerId, UUID contactId) {
        CustomerContact contact = findContact(customerId, contactId);
        contact.markVerified();
        return ContactResponse.from(contact);
    }

    // ---- Document ----

    @Transactional
    public DocumentResponse uploadDocument(UUID customerId, DocumentUploadRequest request) {
        requireCustomerExists(customerId);
        CustomerDocument document = new CustomerDocument(
                customerId, request.documentType(), request.documentNumber(), request.fileUrl(), request.uploadedByUserId()
        );
        return DocumentResponse.from(documentRepository.save(document));
    }

    public List<DocumentResponse> listDocuments(UUID customerId) {
        requireCustomerExists(customerId);
        return documentRepository.findByCustomerId(customerId).stream().map(DocumentResponse::from).toList();
    }

    @Transactional
    public DocumentResponse verifyDocument(UUID customerId, UUID documentId) {
        CustomerDocument document = findDocument(customerId, documentId);
        document.verify();
        return DocumentResponse.from(document);
    }

    @Transactional
    public DocumentResponse rejectDocument(UUID customerId, UUID documentId) {
        CustomerDocument document = findDocument(customerId, documentId);
        document.reject();
        return DocumentResponse.from(document);
    }

    // ---- helpers ----

    private void requireCustomerExists(UUID customerId) {
        if (customerRepository.findByIdAndDeletedAtIsNull(customerId).isEmpty()) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Customer not found: " + customerId);
        }
    }

    private CustomerAddress findAddress(UUID customerId, UUID addressId) {
        CustomerAddress address = addressRepository.findById(addressId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Address not found: " + addressId));
        if (!address.getCustomerId().equals(customerId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Address not found for customer: " + customerId);
        }
        return address;
    }

    private CustomerContact findContact(UUID customerId, UUID contactId) {
        CustomerContact contact = contactRepository.findById(contactId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Contact not found: " + contactId));
        if (!contact.getCustomerId().equals(customerId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Contact not found for customer: " + customerId);
        }
        return contact;
    }

    private CustomerDocument findDocument(UUID customerId, UUID documentId) {
        CustomerDocument document = documentRepository.findById(documentId)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Document not found: " + documentId));
        if (!document.getCustomerId().equals(customerId)) {
            throw new BusinessException(ErrorCode.NOT_FOUND, "Document not found for customer: " + customerId);
        }
        return document;
    }
}
