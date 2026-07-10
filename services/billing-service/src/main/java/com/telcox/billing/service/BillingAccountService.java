package com.telcox.billing.service;

import com.telcox.billing.domain.BillingAccount;
import com.telcox.billing.repository.BillingAccountRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.UUID;

/**
 * BILL-01 / FR-21: Musteri basina bir BillingAccount olmasini garanti eden yardimci servis.
 * Bill-run job, ilk kez faturalanan musteri icin burada hesap acar.
 */
@Service
public class BillingAccountService {

    private static final String DEFAULT_CURRENCY = "TRY";
    private static final BigDecimal DEFAULT_CREDIT_LIMIT = BigDecimal.valueOf(0);

    private final BillingAccountRepository billingAccountRepository;

    public BillingAccountService(BillingAccountRepository billingAccountRepository) {
        this.billingAccountRepository = billingAccountRepository;
    }

    @Transactional
    public BillingAccount getOrCreateForCustomer(UUID customerId) {
        return billingAccountRepository.findByCustomerId(customerId)
                .orElseGet(() -> billingAccountRepository.save(
                        new BillingAccount(customerId, DEFAULT_CURRENCY, DEFAULT_CREDIT_LIMIT)));
    }
}
