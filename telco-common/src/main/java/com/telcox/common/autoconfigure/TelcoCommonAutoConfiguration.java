package com.telcox.common.autoconfigure;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telcox.common.cache.CacheService;
import com.telcox.common.event.ProcessedEventGuard;
import org.springframework.boot.autoconfigure.AutoConfiguration;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.jdbc.core.JdbcTemplate;

@AutoConfiguration(afterName = {
        "org.springframework.boot.data.redis.autoconfigure.DataRedisAutoConfiguration",
        "org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration",
        "org.springframework.boot.jdbc.autoconfigure.JdbcTemplateAutoConfiguration"
})
public class TelcoCommonAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    CacheService cacheService(RedisConnectionFactory redisConnectionFactory, ObjectMapper objectMapper) {
        StringRedisTemplate redisTemplate = new StringRedisTemplate(redisConnectionFactory);
        redisTemplate.afterPropertiesSet();
        return new CacheService(redisTemplate, objectMapper);
    }

    @Bean
    @ConditionalOnBean(JdbcTemplate.class)
    @ConditionalOnMissingBean
    ProcessedEventGuard processedEventGuard(JdbcTemplate jdbcTemplate) {
        return new ProcessedEventGuard(jdbcTemplate);
    }
}
