package com.telcox.productcatalog.cache;

import java.math.BigDecimal;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.junit.jupiter.api.Test;
import org.springframework.cache.concurrent.ConcurrentMapCacheManager;

import static org.assertj.core.api.Assertions.assertThat;

class ProductCatalogCacheServiceTest {

    @Test
    void loadsFromSourceOnceThenReturnsCachedTariff() {
        ProductCatalogCacheService service = service();
        UUID tariffId = UUID.randomUUID();
        TariffCacheValue value = tariff(tariffId);
        AtomicInteger loads = new AtomicInteger();

        assertThat(service.findTariffById(tariffId, () -> {
            loads.incrementAndGet();
            return Optional.of(value);
        })).contains(value);
        assertThat(service.findTariffById(tariffId, () -> {
            loads.incrementAndGet();
            return Optional.empty();
        })).contains(value);
        assertThat(loads).hasValue(1);
    }

    @Test
    void doesNotCacheMissingTariffAndEvictsBothLookupKeys() {
        ProductCatalogCacheService service = service();
        UUID tariffId = UUID.randomUUID();
        TariffCacheValue value = tariff(tariffId);

        assertThat(service.findTariffById(tariffId, Optional::empty)).isEmpty();
        service.findTariffById(tariffId, () -> Optional.of(value));
        service.findTariffByCode(value.tariffCode(), () -> Optional.of(value));
        service.evictTariff(tariffId, value.tariffCode());

        assertThat(service.findTariffById(tariffId, Optional::empty)).isEmpty();
        assertThat(service.findTariffByCode(value.tariffCode(), Optional::empty)).isEmpty();
    }

    private ProductCatalogCacheService service() {
        return new ProductCatalogCacheService(new ConcurrentMapCacheManager(
                ProductCatalogCacheNames.TARIFF_BY_ID,
                ProductCatalogCacheNames.TARIFF_BY_CODE,
                ProductCatalogCacheNames.ACTIVE_TARIFFS));
    }

    private TariffCacheValue tariff(UUID id) {
        return new TariffCacheValue(id, "T-5G", "5G Premium", new BigDecimal("499.90"), "TRY", "ACTIVE");
    }
}
