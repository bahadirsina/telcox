package com.telcox.ticket.repository;

import com.telcox.ticket.domain.TicketOutboxEvent;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface TicketOutboxEventRepository extends JpaRepository<TicketOutboxEvent, UUID> {
}
