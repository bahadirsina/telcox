package com.telcox.ticket.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.telcox.common.exception.BusinessException;
import com.telcox.ticket.api.CreateTicketRequest;
import com.telcox.ticket.domain.SupportTicket;
import com.telcox.ticket.domain.TicketOutboxEvent;
import com.telcox.ticket.domain.TicketPriority;
import com.telcox.ticket.domain.TicketStatus;
import com.telcox.ticket.repository.SupportTicketRepository;
import com.telcox.ticket.repository.TicketOutboxEventRepository;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.junit.jupiter.api.Test;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;

class TicketServiceTest {

    private static final UUID CUSTOMER_ID = UUID.fromString("d551a6ef-7b77-4bb9-b6f8-f331aebf0f0d");

    private final SupportTicketRepository repository = mock(SupportTicketRepository.class);
    private final TicketOutboxEventRepository outboxEventRepository = mock(TicketOutboxEventRepository.class);
    private final SlaAssignmentService slaAssignmentService = new SlaAssignmentService(
            Clock.fixed(Instant.parse("2026-07-08T09:00:00Z"), ZoneOffset.UTC));
    private final TicketService service = new TicketService(repository, outboxEventRepository, slaAssignmentService);

    @Test
    void createsOpenTicketWithCorrelationId() {
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));

        var response = service.create(new CreateTicketRequest(CUSTOMER_ID, "BILLING", TicketPriority.HIGH,
                "Invoice issue", "Customer cannot see latest invoice"), "corr-ticket-1");

        assertThat(response.customerId()).isEqualTo(CUSTOMER_ID);
        assertThat(response.status()).isEqualTo(TicketStatus.OPEN);
        assertThat(response.priority()).isEqualTo(TicketPriority.HIGH);
        assertThat(response.ticketNumber()).startsWith("TCK-");
        assertThat(response.correlationId()).isEqualTo("corr-ticket-1");
        assertThat(response.assignedTeam()).isEqualTo("BILLING_SUPPORT");
        assertThat(response.slaDueAt()).isEqualTo(java.time.LocalDateTime.parse("2026-07-08T17:00:00"));
        verify(repository).save(any(SupportTicket.class));
        verify(outboxEventRepository).save(any(TicketOutboxEvent.class));
    }

    @Test
    void listsTicketsByCustomer() {
        SupportTicket ticket = SupportTicket.open(CUSTOMER_ID, "TECHNICAL", TicketPriority.MEDIUM,
                "No signal", "Customer has no signal", "corr-ticket-2");
        when(repository.findByCustomerIdOrderByCreatedAtDesc(CUSTOMER_ID)).thenReturn(List.of(ticket));

        assertThat(service.list(CUSTOMER_ID)).hasSize(1);
    }

    @Test
    void getsTicketDetail() {
        SupportTicket ticket = SupportTicket.open(CUSTOMER_ID, "TECHNICAL", TicketPriority.LOW,
                "Slow internet", "Speed is lower than expected", "corr-ticket-3");
        when(repository.findById(ticket.getId())).thenReturn(Optional.of(ticket));

        assertThat(service.get(ticket.getId()).id()).isEqualTo(ticket.getId());
    }

    @Test
    void throwsBusinessExceptionWhenTicketIsMissing() {
        UUID ticketId = UUID.fromString("f46b3a2f-9d53-4a3e-b9e5-c19c020a12c9");
        when(repository.findById(ticketId)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> service.get(ticketId))
                .isInstanceOf(BusinessException.class)
                .hasMessageContaining("Ticket not found");
    }
}
