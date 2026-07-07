package com.telcox.ticket.service;

import com.telcox.common.exception.BusinessException;
import com.telcox.common.exception.ErrorCode;
import com.telcox.ticket.api.CreateTicketRequest;
import com.telcox.ticket.api.TicketResponse;
import com.telcox.ticket.domain.SupportTicket;
import com.telcox.ticket.repository.SupportTicketRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class TicketService {

    private final SupportTicketRepository repository;

    public TicketService(SupportTicketRepository repository) {
        this.repository = repository;
    }

    @Transactional
    public TicketResponse create(CreateTicketRequest request, String correlationId) {
        SupportTicket ticket = SupportTicket.open(request.customerId(), request.category(), request.priority(),
                request.subject(), request.description(), normalizeCorrelationId(correlationId));
        return TicketResponse.from(repository.save(ticket));
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
}
