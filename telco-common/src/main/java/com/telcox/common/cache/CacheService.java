package com.telcox.common.cache;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.util.Optional;
import java.util.function.Supplier;

@Component
public class CacheService {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;

    public CacheService(StringRedisTemplate redisTemplate, ObjectMapper objectMapper) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
    }

    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            String raw = redisTemplate.opsForValue().get(key);
            if (raw == null) return Optional.empty();
            return Optional.ofNullable(objectMapper.readValue(raw, type));
        } catch (Exception e) {
            return Optional.empty();
        }
    }

    public <T> void put(String key, T value, Duration ttl) {
        if (value == null) return;
        try {
            String json = objectMapper.writeValueAsString(value);
            redisTemplate.opsForValue().set(key, json, CachePolicy.withJitter(ttl));
        } catch (Exception ignored) {}
    }

    public void evict(String key) {
        try { redisTemplate.delete(key); } catch (Exception ignored) {}
    }

    public <T> T getOrLoad(String key, Class<T> type, Supplier<T> loader, Duration ttl) {
        Optional<T> cached = get(key, type);
        if (cached.isPresent()) return cached.get();
        T loaded = loader.get();
        if (loaded != null) put(key, loaded, ttl);
        return loaded;
    }
}
