package com.telcox.order.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
class InboundEventReader {

    private final ObjectMapper objectMapper;

    InboundEventReader(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    InboundEvent read(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            JsonNode payload = root.path("payload");
            if (payload.isMissingNode() || payload.isNull()) {
                payload = root;
            }
            return new InboundEvent(
                    uuid(root.path("eventId").asText(null), UUID.randomUUID()),
                    text(root, "type", "eventType", "unknown-event"),
                    uuid(root.path("aggregateId").asText(null), null),
                    text(root, "sourceService", "source", "unknown-service"),
                    text(root, "correlationId", "correlation_id", "corr-" + UUID.randomUUID()),
                    payload
            );
        } catch (Exception ex) {
            throw new IllegalArgumentException("Inbound event payload could not be parsed", ex);
        }
    }

    UUID payloadUuid(InboundEvent event, String fieldName) {
        String value = event.payload().path(fieldName).asText(null);
        return uuid(value, event.aggregateId());
    }

    private static String text(JsonNode root, String preferredField, String fallbackField, String defaultValue) {
        String preferred = root.path(preferredField).asText(null);
        if (preferred != null && !preferred.isBlank()) {
            return preferred;
        }
        String fallback = root.path(fallbackField).asText(null);
        return fallback == null || fallback.isBlank() ? defaultValue : fallback;
    }

    private static UUID uuid(String value, UUID defaultValue) {
        if (value == null || value.isBlank()) {
            return defaultValue;
        }
        return UUID.fromString(value);
    }
}
