package com.telcox.customer.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    @Test
    void registerIndividual_shouldCreateProspectCustomer() {
        UUID identityUserId = UUID.randomUUID();

        Customer customer = Customer.registerIndividual(
                identityUserId,
                "CUS-123456",
                "Ayse",
                "Yilmaz",
                "12345678901",
                LocalDate.of(2000, 1, 15),
                CustomerSegment.MASS
        );

        assertNotNull(customer.getId());
        assertEquals(identityUserId, customer.getIdentityUserId());
        assertEquals("CUS-123456", customer.getCustomerNumber());
        assertEquals(CustomerType.INDIVIDUAL, customer.getCustomerType());
        assertEquals(CustomerStatus.PROSPECT, customer.getStatus());
        assertFalse(customer.isDeleted());
    }

    @Test
    void registerIndividual_shouldUseMassSegment_whenSegmentIsNull() {
        Customer customer = createCustomer(null);

        assertEquals(CustomerSegment.MASS, customer.getSegment());
    }

    @Test
    void registerIndividual_shouldRejectBlankFirstName() {
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> Customer.registerIndividual(
                        UUID.randomUUID(), "CUS-123456", " ", "Yilmaz",
                        "12345678901", LocalDate.of(2000, 1, 15), CustomerSegment.MASS)
        );

        assertEquals("firstName must not be blank", exception.getMessage());
    }

    @Test
    void approveKyc_shouldActivateProspectCustomer() {
        Customer customer = createCustomer(CustomerSegment.MASS);

        customer.approveKyc("Belgeler dogrulandi");

        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
        assertEquals("Belgeler dogrulandi", customer.getStatusReason());
    }

    @Test
    void approveKyc_shouldFail_whenCustomerIsNotProspect() {
        Customer customer = createCustomer(CustomerSegment.MASS);
        customer.approveKyc("Ilk onay");

        assertThrows(IllegalStateException.class, () -> customer.approveKyc("Tekrar onay"));
    }

    @Test
    void rejectKyc_shouldCloseProspectCustomer() {
        Customer customer = createCustomer(CustomerSegment.MASS);

        customer.rejectKyc("Kimlik bilgileri eslesmedi");

        assertEquals(CustomerStatus.CLOSED, customer.getStatus());
        assertEquals("Kimlik bilgileri eslesmedi", customer.getStatusReason());
    }

    @Test
    void suspendAndReactivate_shouldFollowStateTransitions() {
        Customer customer = createCustomer(CustomerSegment.MASS);
        customer.approveKyc("Onaylandi");

        customer.suspend("Odeme gecikmesi");
        assertEquals(CustomerStatus.SUSPENDED, customer.getStatus());

        customer.reactivate("Odeme tamamlandi");
        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
    }

    @Test
    void softDelete_shouldMarkCustomerDeleted() {
        Customer customer = createCustomer(CustomerSegment.MASS);

        customer.softDelete();

        assertTrue(customer.isDeleted());
        assertNotNull(customer.getDeletedAt());
        assertThrows(IllegalStateException.class, customer::softDelete);
    }

    private Customer createCustomer(CustomerSegment segment) {
        return Customer.registerIndividual(
                UUID.randomUUID(),
                "CUS-123456",
                "Ayse",
                "Yilmaz",
                "12345678901",
                LocalDate.of(2000, 1, 15),
                segment
        );
    }
}
