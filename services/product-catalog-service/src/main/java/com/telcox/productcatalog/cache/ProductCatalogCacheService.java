package com.telcox.productcatalog.cache;

import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.function.Supplier;
import org.springframework.cache.Cache;
import org.springframework.cache.CacheManager;
import org.springframework.stereotype.Service;

@Service
public class ProductCatalogCacheService {
    private final CacheManager cacheManager;

    public ProductCatalogCacheService(CacheManager cacheManager) {
        this.cacheManager = cacheManager;
    }

    public Optional<TariffCacheValue> findTariffById(
            UUID tariffId,
            Supplier<Optional<TariffCacheValue>> sourceLoader
    ) {
        return getOrLoad(ProductCatalogCacheNames.TARIFF_BY_ID, tariffId, sourceLoader);
    }

    public Optional<TariffCacheValue> findTariffByCode(
            String tariffCode,
            Supplier<Optional<TariffCacheValue>> sourceLoader
    ) {
        if (tariffCode == null || tariffCode.isBlank()) {
            throw new IllegalArgumentException("tariffCode must not be blank");
        }
        return getOrLoad(ProductCatalogCacheNames.TARIFF_BY_CODE, tariffCode, sourceLoader);
    }

    public void evictTariff(UUID tariffId, String tariffCode) {
        cache(ProductCatalogCacheNames.TARIFF_BY_ID).evict(tariffId);
        if (tariffCode != null && !tariffCode.isBlank()) {
            cache(ProductCatalogCacheNames.TARIFF_BY_CODE).evict(tariffCode);
        }
        cache(ProductCatalogCacheNames.ACTIVE_TARIFFS).clear();
    }

    private Optional<TariffCacheValue> getOrLoad(
            String cacheName,
            Object key,
            Supplier<Optional<TariffCacheValue>> sourceLoader
    ) {
        Objects.requireNonNull(key, "cache key must not be null");
        Objects.requireNonNull(sourceLoader, "sourceLoader must not be null");
        Cache cache = cache(cacheName);
        TariffCacheValue cached = cache.get(key, TariffCacheValue.class);
        if (cached != null) {
            return Optional.of(cached);
        }
        Optional<TariffCacheValue> loaded = Objects.requireNonNull(
                sourceLoader.get(), "sourceLoader result must not be null");
        loaded.ifPresent(value -> cache.put(key, value));
        return loaded;
    }

    private Cache cache(String name) {
        return Objects.requireNonNull(cacheManager.getCache(name), "cache is not configured: " + name);
    }
}
