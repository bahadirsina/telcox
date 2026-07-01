package com.telcox.productcatalog.cache;

import java.util.Map;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.cache.CacheManager;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.cache.RedisCacheConfiguration;
import org.springframework.data.redis.cache.RedisCacheManager;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.serializer.RedisSerializationContext;
import org.springframework.data.redis.serializer.RedisSerializer;
import org.springframework.data.redis.serializer.StringRedisSerializer;

@Configuration
@EnableConfigurationProperties(ProductCatalogCacheProperties.class)
public class ProductCatalogCacheConfig {
    static final String KEY_PREFIX = "product-catalog:cache:";

    @Bean
    CacheManager productCatalogCacheManager(
            RedisConnectionFactory connectionFactory,
            ProductCatalogCacheProperties properties
    ) {
        RedisCacheConfiguration tariff = configuration(properties.getTariffTtl());
        RedisCacheConfiguration list = configuration(properties.getListTtl());

        return RedisCacheManager.builder(connectionFactory)
                .cacheDefaults(tariff)
                .withInitialCacheConfigurations(Map.of(
                        ProductCatalogCacheNames.TARIFF_BY_ID, tariff,
                        ProductCatalogCacheNames.TARIFF_BY_CODE, tariff,
                        ProductCatalogCacheNames.ACTIVE_TARIFFS, list))
                .disableCreateOnMissingCache()
                .build();
    }

    RedisCacheConfiguration configuration(java.time.Duration ttl) {
        return RedisCacheConfiguration.defaultCacheConfig()
                .entryTtl(ttl)
                .disableCachingNullValues()
                .computePrefixWith(cacheName -> KEY_PREFIX + cacheName + ":")
                .serializeKeysWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(new StringRedisSerializer()))
                .serializeValuesWith(RedisSerializationContext.SerializationPair
                        .fromSerializer(RedisSerializer.json()));
    }
}
