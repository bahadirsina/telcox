package com.telcox.ticket.service;

import java.time.LocalDateTime;
import java.util.UUID;

public record SlaAssignment(
        String assignedTeam,
        UUID assignedAgentId,
        LocalDateTime assignedAt,
        LocalDateTime slaDueAt
) {
}
