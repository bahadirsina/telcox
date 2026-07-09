package com.telcox.ticket.api;

import com.telcox.ticket.service.TicketService;
import jakarta.validation.Valid;
import java.util.List;
import java.util.UUID;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/tickets")
public class TicketController {

    private final TicketService ticketService;

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TicketResponse create(@Valid @RequestBody CreateTicketRequest request,
                                 @RequestHeader(value = "X-Correlation-Id", required = false) String correlationId) {
        return ticketService.create(request, correlationId);
    }

    @GetMapping
    public List<TicketResponse> list(@RequestParam(required = false) UUID customerId) {
        return ticketService.list(customerId);
    }

    @GetMapping("/{ticketId}")
    public TicketResponse get(@PathVariable UUID ticketId) {
        return ticketService.get(ticketId);
    }
}
