package com.telcox.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;

import com.telcox.ticket.domain.SupportTicket;
import com.telcox.ticket.domain.TicketPriority;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class SlaAssignmentServiceTest {

    private final Clock clock = Clock.fixed(Instant.parse("2026-07-08T09:00:00Z"), ZoneId.of("UTC"));
    private final SlaAssignmentService service = new SlaAssignmentService(clock);

    @Test
    void assignsCriticalTicketsToEscalationTeamWithFourHourSla() {
        SupportTicket ticket = SupportTicket.open(UUID.randomUUID(), "BILLING", TicketPriority.CRITICAL,
                "System outage", "Customer cannot use any service", "corr-sla-1");

        SlaAssignment assignment = service.assign(ticket);

        assertThat(assignment.assignedTeam()).isEqualTo("NOC_ESCALATION");
        assertThat(assignment.slaDueAt()).isEqualTo(assignment.assignedAt().plusHours(4));
    }

    @Test
    void assignsBillingHighPriorityTicketsWithEightHourSla() {
        SupportTicket ticket = SupportTicket.open(UUID.randomUUID(), "BILLING", TicketPriority.HIGH,
                "Invoice issue", "Customer cannot see latest invoice", "corr-sla-2");

        SlaAssignment assignment = service.assign(ticket);

        assertThat(assignment.assignedTeam()).isEqualTo("BILLING_SUPPORT");
        assertThat(assignment.slaDueAt()).isEqualTo(assignment.assignedAt().plusHours(8));
    }

    @Test
    void fallsBackToCustomerCareForUnknownCategory() {
        SupportTicket ticket = SupportTicket.open(UUID.randomUUID(), "OTHER", TicketPriority.LOW,
                "General question", "Customer asks for information", "corr-sla-3");

        SlaAssignment assignment = service.assign(ticket);

        assertThat(assignment.assignedTeam()).isEqualTo("CUSTOMER_CARE");
        assertThat(assignment.slaDueAt()).isEqualTo(assignment.assignedAt().plusHours(72));
    }
}
