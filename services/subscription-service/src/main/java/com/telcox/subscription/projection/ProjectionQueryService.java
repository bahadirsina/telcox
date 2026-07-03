package com.telcox.subscription.projection;

import com.telcox.common.cache.CacheKeyPrefix;
import com.telcox.common.cache.CachePolicy;
import com.telcox.common.cache.CacheService;
import com.telcox.subscription.projection.order.OrderProjection;
import com.telcox.subscription.projection.order.OrderProjectionRepository;
import com.telcox.subscription.projection.payment.PaymentProjection;
import com.telcox.subscription.projection.payment.PaymentProjectionRepository;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class ProjectionQueryService {

    private final OrderProjectionRepository orderRepo;
    private final PaymentProjectionRepository paymentRepo;
    private final CacheService cacheService;

    public ProjectionQueryService(OrderProjectionRepository orderRepo,
                                   PaymentProjectionRepository paymentRepo, CacheService cacheService) {
        this.orderRepo = orderRepo;
        this.paymentRepo = paymentRepo;
        this.cacheService = cacheService;
    }

    public OrderProjection getOrder(UUID orderId) {
        return cacheService.getOrLoad(
                CacheKeyPrefix.buildKey(CacheKeyPrefix.SUB_ORDER_PROJECTION, orderId),
                OrderProjection.class,
                () -> orderRepo.findByOrderId(orderId).orElse(null),
                CachePolicy.PROJECTION_TTL);
    }

    public PaymentProjection getPayment(UUID paymentId) {
        return cacheService.getOrLoad(
                CacheKeyPrefix.buildKey(CacheKeyPrefix.SUB_PAYMENT_PROJECTION, paymentId),
                PaymentProjection.class,
                () -> paymentRepo.findByPaymentId(paymentId).orElse(null),
                CachePolicy.PROJECTION_TTL);
    }
}
