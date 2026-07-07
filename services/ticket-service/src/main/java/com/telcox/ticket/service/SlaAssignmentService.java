package com.telcox.ticket.service;

import com.telcox.ticket.domain.SupportTicket;
import com.telcox.ticket.domain.TicketPriority;
import java.time.Clock;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Locale;
import org.springframework.stereotype.Service;

@Service
public class SlaAssignmentService {

    private final Clock clock;

    public SlaAssignmentService() {
        this(Clock.systemDefaultZone());
    }

    SlaAssignmentService(Clock clock) {
        this.clock = clock;
    }

    public SlaAssignment assign(SupportTicket ticket) {
        LocalDateTime assignedAt = LocalDateTime.now(clock);
        return new SlaAssignment(resolveTeam(ticket), null, assignedAt, assignedAt.plus(resolveSla(ticket.getPriority())));
    }

    private String resolveTeam(SupportTicket ticket) {
        if (ticket.getPriority() == TicketPriority.CRITICAL) {
            return "NOC_ESCALATION";
        }

        return switch (normalizeCategory(ticket.getCategory())) {
            case "BILLING", "INVOICE" -> "BILLING_SUPPORT";
            case "PAYMENT" -> "PAYMENT_SUPPORT";
            case "ORDER", "ACTIVATION" -> "ORDER_SUPPORT";
            case "TECHNICAL", "NETWORK" -> "TECHNICAL_SUPPORT";
            default -> "CUSTOMER_CARE";
        };
    }

    private Duration resolveSla(TicketPriority priority) {
        return switch (priority) {
            case CRITICAL -> Duration.ofHours(4);
            case HIGH -> Duration.ofHours(8);
            case MEDIUM -> Duration.ofHours(24);
            case LOW -> Duration.ofHours(72);
        };
    }

    private String normalizeCategory(String category) {
        return category == null ? "" : category.trim().toUpperCase(Locale.ROOT);
    }
}
