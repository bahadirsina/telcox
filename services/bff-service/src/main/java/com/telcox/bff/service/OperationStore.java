package com.telcox.bff.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.telcox.bff.config.BffProperties;
import com.telcox.bff.model.OperationRecord;
import com.telcox.common.cache.CacheKeyPrefix;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.util.Optional;
import java.util.UUID;

@Service
public class OperationStore {

    private final StringRedisTemplate redisTemplate;
    private final ObjectMapper objectMapper;
    private final BffProperties properties;

    public OperationStore(StringRedisTemplate redisTemplate, ObjectMapper objectMapper, BffProperties properties) {
        this.redisTemplate = redisTemplate;
        this.objectMapper = objectMapper;
        this.properties = properties;
    }

    public void save(OperationRecord operation) {
        try {
            redisTemplate.opsForValue().set(key(operation.operationId()),
                    objectMapper.writeValueAsString(operation),
                    properties.getOperationTtl());
        } catch (Exception ignored) {
        }
    }

    public Optional<OperationRecord> find(UUID operationId) {
        try {
            String raw = redisTemplate.opsForValue().get(key(operationId));
            if (raw == null) {
                return Optional.empty();
            }
            return Optional.ofNullable(objectMapper.readValue(raw, OperationRecord.class));
        } catch (Exception ignored) {
            return Optional.empty();
        }
    }

    private String key(UUID operationId) {
        return CacheKeyPrefix.buildKey(CacheKeyPrefix.BFF_OPERATION_STATUS, operationId);
    }
}
