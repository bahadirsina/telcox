package com.telcox.common.event;

import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Timestamp;
import java.time.Instant;
import java.util.UUID;

/** docs/idempotent-consumer-standard.md standardinin ortak implementasyonu. */
@Component
public class ProcessedEventGuard {

    private final JdbcTemplate jdbcTemplate;

    public ProcessedEventGuard(JdbcTemplate jdbcTemplate) {
        this.jdbcTemplate = jdbcTemplate;
    }

    public boolean tryBeginProcessing(String tableName, UUID eventId, String eventType,
                                       String sourceService, UUID aggregateId) {
        try {
            jdbcTemplate.update(
                "INSERT INTO " + tableName +
                " (id, event_id, event_type, source_service, aggregate_id, status, created_at) " +
                "VALUES (?, ?, ?, ?, ?, 'PROCESSING', ?)",
                UUID.randomUUID(), eventId, eventType, sourceService, aggregateId,
                Timestamp.from(Instant.now()));
            return true;
        } catch (DuplicateKeyException e) {
            return false;
        }
    }

    public void markProcessed(String tableName, UUID eventId) {
        jdbcTemplate.update(
            "UPDATE " + tableName + " SET status='PROCESSED', processed_at=? WHERE event_id=?",
            Timestamp.from(Instant.now()), eventId);
    }
}