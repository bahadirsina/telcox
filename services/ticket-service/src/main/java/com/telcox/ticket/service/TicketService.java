package com.telcox.ticket.service;

import com.telcox.common.exception.BusinessException;
import com.telcox.common.exception.ErrorCode;
import com.telcox.ticket.api.CreateTicketRequest;
import com.telcox.ticket.api.TicketResponse;
import com.telcox.ticket.domain.SupportTicket;
import com.telcox.ticket.domain.TicketOutboxEvent;
import com.telcox.ticket.repository.SupportTicketRepository;
import com.telcox.ticket.repository.TicketOutboxEventRepository;
import java.util.List;
import java.util.Map.Entry;
import java.util.Map;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    private final SupportTicketRepository repository;
    private final TicketOutboxEventRepository outboxEventRepository;
    private final SlaAssignmentService slaAssignmentService;

    public TicketService(SupportTicketRepository repository,
                         TicketOutboxEventRepository outboxEventRepository,
                         SlaAssignmentService slaAssignmentService) {
        this.repository = repository;
        this.outboxEventRepository = outboxEventRepository;
        this.slaAssignmentService = slaAssignmentService;
    }

    @Transactional
    public TicketResponse create(CreateTicketRequest request, String correlationId) {
        SupportTicket ticket = SupportTicket.open(request.customerId(), request.category(), request.priority(),
                request.subject(), request.description(), normalizeCorrelationId(correlationId));
        SlaAssignment assignment = slaAssignmentService.assign(ticket);
        ticket.assign(assignment.assignedTeam(), assignment.assignedAgentId(), assignment.assignedAt(),
                assignment.slaDueAt());
        SupportTicket savedTicket = repository.save(ticket);
        publishTicketOpened(savedTicket);
        return TicketResponse.from(savedTicket);
    }

    @Transactional(readOnly = true)
    public List<TicketResponse> list(UUID customerId) {
        List<SupportTicket> tickets = customerId == null
                ? repository.findAllByOrderByCreatedAtDesc()
                : repository.findByCustomerIdOrderByCreatedAtDesc(customerId);
        return tickets.stream().map(TicketResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public TicketResponse get(UUID ticketId) {
        if (ticketId == null) {
            throw new BusinessException(ErrorCode.VALIDATION_FAILED, "ticketId is required");
        }
        return repository.findById(ticketId)
                .map(TicketResponse::from)
                .orElseThrow(() -> new BusinessException(ErrorCode.NOT_FOUND, "Ticket not found: " + ticketId));
    }

    private String normalizeCorrelationId(String correlationId) {
        return correlationId == null || correlationId.isBlank() ? "ticket-" + UUID.randomUUID() : correlationId;
    }

    private void publishTicketOpened(SupportTicket ticket) {
        outboxEventRepository.save(new TicketOutboxEvent(ticket.getId(), "TICKET", "ticket-opened",
                Map.ofEntries(
                        entry("ticketId", ticket.getId()),
                        entry("ticketNumber", ticket.getTicketNumber()),
                        entry("customerId", ticket.getCustomerId()),
                        entry("category", ticket.getCategory()),
                        entry("priority", ticket.getPriority().name()),
                        entry("status", ticket.getStatus().name()),
                        entry("subject", ticket.getSubject()),
                        entry("assignedTeam", ticket.getAssignedTeam() == null ? "" : ticket.getAssignedTeam()),
                        entry("slaDueAt", ticket.getSlaDueAt() == null ? "" : ticket.getSlaDueAt().toString()),
                        entry("templateCode", "ticket.opened"),
                        entry("correlationId", ticket.getCorrelationId())
                ),
                ticket.getCorrelationId()));
    }

    private Entry<String, Object> entry(String key, Object value) {
        return Map.entry(key, value);
    }
}
