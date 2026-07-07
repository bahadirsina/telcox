package com.telcox.ticket.api;

import com.telcox.ticket.domain.SupportTicket;
import com.telcox.ticket.domain.TicketPriority;
import com.telcox.ticket.domain.TicketStatus;
import java.time.LocalDateTime;
import java.util.UUID;

public record TicketResponse(
        UUID id,
        String ticketNumber,
        UUID customerId,
        String category,
        TicketPriority priority,
        TicketStatus status,
        String subject,
        String description,
        String assignedTeam,
        UUID assignedAgentId,
        LocalDateTime assignedAt,
        LocalDateTime slaDueAt,
        String correlationId,
        LocalDateTime createdAt,
        LocalDateTime updatedAt
) {
    public static TicketResponse from(SupportTicket ticket) {
        return new TicketResponse(ticket.getId(), ticket.getTicketNumber(), ticket.getCustomerId(),
                ticket.getCategory(), ticket.getPriority(), ticket.getStatus(), ticket.getSubject(),
                ticket.getDescription(), ticket.getAssignedTeam(), ticket.getAssignedAgentId(),
                ticket.getAssignedAt(), ticket.getSlaDueAt(), ticket.getCorrelationId(),
                ticket.getCreatedAt(), ticket.getUpdatedAt());
    }
}
