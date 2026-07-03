package com.telcox.productcatalog.cache;

import java.time.Duration;
import org.junit.jupiter.api.Test;
import org.springframework.cache.CacheManager;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;

class ProductCatalogCacheConfigTest {

    @Test
    void appliesServicePrefixTtlJsonSerializationAndNullProtection() {
        ProductCatalogCacheConfig config = new ProductCatalogCacheConfig();
        RedisCacheConfiguration cache = config.configuration(Duration.ofMinutes(10));

        assertThat(cache.getKeyPrefixFor(ProductCatalogCacheNames.TARIFF_BY_ID))
                .isEqualTo("product-catalog:cache:tariff-by-id:");
        assertThat(cache.getTtlFunction().getTimeToLive("tariff-1", null))
                .isEqualTo(Duration.ofMinutes(10));
        assertThat(cache.getAllowCacheNullValues()).isFalse();
        assertThat(cache.getValueSerializationPair()).isNotNull();
    }

    @Test
    void rejectsMissingOrNonPositiveTtl() {
        ProductCatalogCacheProperties properties = new ProductCatalogCacheProperties();

        assertThatThrownBy(() -> properties.setTariffTtl(Duration.ZERO))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> properties.setListTtl(null))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void registersOnlyKnownProductCatalogCaches() {
        ProductCatalogCacheProperties properties = new ProductCatalogCacheProperties();
        CacheManager manager = new ProductCatalogCacheConfig().productCatalogCacheManager(
                mock(RedisConnectionFactory.class), properties);
        ((RedisCacheManager) manager).afterPropertiesSet();

        assertThat(manager.getCacheNames()).containsExactlyInAnyOrder(
                ProductCatalogCacheNames.TARIFF_BY_ID,
                ProductCatalogCacheNames.TARIFF_BY_CODE,
                ProductCatalogCacheNames.ACTIVE_TARIFFS);
        assertThat(manager.getCache("unknown-cache")).isNull();
    }
}
