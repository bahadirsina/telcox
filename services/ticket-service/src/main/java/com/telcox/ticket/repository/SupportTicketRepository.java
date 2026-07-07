package com.telcox.ticket.repository;

import com.telcox.ticket.domain.SupportTicket;
import java.util.List;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface SupportTicketRepository extends JpaRepository<SupportTicket, UUID> {

    List<SupportTicket> findAllByOrderByCreatedAtDesc();

    List<SupportTicket> findByCustomerIdOrderByCreatedAtDesc(UUID customerId);
}
