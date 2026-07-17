package com.telcox.billing.service;

import com.telcox.billing.domain.BillingAccount;
import com.telcox.billing.repository.BillingAccountRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class BillingAccountServiceTest {

    @Mock
    private BillingAccountRepository billingAccountRepository;

    private BillingAccountService billingAccountService;

    @BeforeEach
    void setUp() {
        billingAccountService = new BillingAccountService(billingAccountRepository);
    }

    @Test
    void shouldReturnExistingBillingAccountWithoutCreatingANewOne() {
        UUID customerId = UUID.randomUUID();
        BillingAccount existingAccount = new BillingAccount(customerId, "TRY", BigDecimal.ZERO);
        when(billingAccountRepository.findByCustomerId(customerId)).thenReturn(Optional.of(existingAccount));

        BillingAccount result = billingAccountService.getOrCreateForCustomer(customerId);

        assertSame(existingAccount, result);
        verify(billingAccountRepository, never()).save(org.mockito.ArgumentMatchers.any(BillingAccount.class));
    }

    @Test
    void shouldCreateBillingAccountWithDefaultValuesWhenAccountDoesNotExist() {
        UUID customerId = UUID.randomUUID();
        when(billingAccountRepository.findByCustomerId(customerId)).thenReturn(Optional.empty());
        when(billingAccountRepository.save(org.mockito.ArgumentMatchers.any(BillingAccount.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        BillingAccount result = billingAccountService.getOrCreateForCustomer(customerId);

        ArgumentCaptor<BillingAccount> captor = ArgumentCaptor.forClass(BillingAccount.class);
        verify(billingAccountRepository).save(captor.capture());
        BillingAccount savedAccount = captor.getValue();

        assertSame(savedAccount, result);
        assertEquals(customerId, savedAccount.getCustomerId());
        assertEquals("TRY", savedAccount.getCurrency());
        assertEquals(0, BigDecimal.ZERO.compareTo(savedAccount.getCreditLimit()));
    }
}
