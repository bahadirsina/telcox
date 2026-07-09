package com.telcox.ticket.api;

import com.telcox.ticket.domain.TicketPriority;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import java.util.UUID;

public record CreateTicketRequest(
        @NotNull UUID customerId,
        @NotBlank @Size(max = 80) String category,
        TicketPriority priority,
        @NotBlank @Size(max = 160) String subject,
        @NotBlank String description
) {
}
