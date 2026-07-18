package com.telcox.customer.integration;

import com.telcox.customer.domain.Customer;
import com.telcox.customer.domain.CustomerSegment;
import com.telcox.customer.repository.CustomerRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.data.jpa.test.autoconfigure.DataJpaTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.postgresql.PostgreSQLContainer;

import java.time.LocalDate;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@Testcontainers
@DataJpaTest
@ActiveProfiles("test")
class CustomerRepositoryIntegrationTest {

    @Container
    static final PostgreSQLContainer POSTGRES = new PostgreSQLContainer("postgres:16-alpine")
            .withDatabaseName("customer_test")
            .withUsername("test")
            .withPassword("test");

    @DynamicPropertySource
    static void datasourceProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);
    }

    @Autowired CustomerRepository customerRepository;

    @Test
    void shouldPersistFindAndSoftDeleteCustomer() {
        Customer customer = Customer.registerIndividual(UUID.randomUUID(), "CUS-TEST-001", "Ada", "Lovelace",
                "10000000146", LocalDate.of(1995, 1, 1), CustomerSegment.MASS);
        Customer saved = customerRepository.saveAndFlush(customer);

        assertTrue(customerRepository.findByIdAndDeletedAtIsNull(saved.getId()).isPresent());
        assertTrue(customerRepository.existsByNationalIdAndDeletedAtIsNull("10000000146"));

        saved.softDelete();
        customerRepository.saveAndFlush(saved);

        assertTrue(customerRepository.findByIdAndDeletedAtIsNull(saved.getId()).isEmpty());
        assertFalse(customerRepository.existsByNationalIdAndDeletedAtIsNull("10000000146"));
    }
}
