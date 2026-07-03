package com.telcox.billing.projection;

import com.telcox.billing.projection.subscription.SubscriptionProjection;
import com.telcox.billing.projection.subscription.SubscriptionProjectionRepository;
import com.telcox.common.cache.CacheKeyPrefix;
import com.telcox.common.cache.CachePolicy;
import com.telcox.common.cache.CacheService;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class BillingProjectionQueryService {

    private final SubscriptionProjectionRepository subscriptionRepo;
    private final CacheService cacheService;

    public BillingProjectionQueryService(SubscriptionProjectionRepository subscriptionRepo,
                                          CacheService cacheService) {
        this.subscriptionRepo = subscriptionRepo;
        this.cacheService = cacheService;
    }

    public SubscriptionProjection getSubscription(UUID subscriptionId) {
        return cacheService.getOrLoad(
                CacheKeyPrefix.buildKey(CacheKeyPrefix.BILLING_SUBSCRIPTION_PROJECTION, subscriptionId),
                SubscriptionProjection.class,
                () -> subscriptionRepo.findBySubscriptionId(subscriptionId).orElse(null),
                CachePolicy.PROJECTION_TTL);
    }
}
