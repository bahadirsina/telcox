package com.telcox.customer.domain;

import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class CustomerTest {

    private Customer newCustomer() {
        return Customer.registerIndividual(
                UUID.randomUUID(), "CUS-100001", "Ada", "Lovelace",
                "10000000146", LocalDate.of(1995, 1, 1), CustomerSegment.MASS);
    }

    @Test
    void shouldRegisterIndividualAsProspect() {
        Customer customer = newCustomer();
        assertEquals(CustomerType.INDIVIDUAL, customer.getCustomerType());
        assertEquals(CustomerStatus.PROSPECT, customer.getStatus());
        assertFalse(customer.isDeleted());
    }

    @Test
    void shouldApproveKycOnlyFromProspect() {
        Customer customer = newCustomer();
        customer.approveKyc("KYC verified");
        assertEquals(CustomerStatus.ACTIVE, customer.getStatus());
        assertThrows(IllegalStateException.class, () -> customer.approveKyc("again"));
    }

    @Test
    void shouldRejectKycAndCloseCustomer() {
        Customer customer = newCustomer();
        customer.rejectKyc("Document mismatch");
        assertEquals(CustomerStatus.CLOSED, customer.getStatus());
    }

    @Test
    void shouldSoftDeleteOnlyOnce() {
        Customer customer = newCustomer();
        customer.softDelete();
        assertTrue(customer.isDeleted());
        assertNotNull(customer.getDeletedAt());
        assertThrows(IllegalStateException.class, customer::softDelete);
    }
}
