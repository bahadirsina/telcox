package com.telcox.subscription.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.UUID;

record InboundEvent(
        UUID eventId,
        String type,
        UUID aggregateId,
        String sourceService,
        String correlationId,
        JsonNode payload
) {
}
