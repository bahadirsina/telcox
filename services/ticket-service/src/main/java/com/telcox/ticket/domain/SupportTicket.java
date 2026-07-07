package com.telcox.ticket.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.Id;
import jakarta.persistence.PreUpdate;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "support_ticket")
public class SupportTicket {

    @Id
    private UUID id;

    @Column(name = "ticket_number", nullable = false, unique = true, length = 40)
    private String ticketNumber;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    @Column(nullable = false, length = 80)
    private String category;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private TicketPriority priority;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 30)
    private TicketStatus status;

    @Column(nullable = false, length = 160)
    private String subject;

    @Column(nullable = false, columnDefinition = "text")
    private String description;

    @Column(name = "assigned_team", length = 80)
    private String assignedTeam;

    @Column(name = "assigned_agent_id")
    private UUID assignedAgentId;

    @Column(name = "assigned_at")
    private LocalDateTime assignedAt;

    @Column(name = "sla_due_at")
    private LocalDateTime slaDueAt;

    @Column(name = "correlation_id", nullable = false, length = 100)
    private String correlationId;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    protected SupportTicket() {
    }

    private SupportTicket(UUID customerId, String category, TicketPriority priority, String subject,
                          String description, String correlationId) {
        LocalDateTime now = LocalDateTime.now();
        this.id = UUID.randomUUID();
        this.ticketNumber = "TCK-" + now.getYear() + "-" + UUID.randomUUID().toString().substring(0, 8).toUpperCase();
        this.customerId = customerId;
        this.category = category;
        this.priority = priority;
        this.status = TicketStatus.OPEN;
        this.subject = subject;
        this.description = description;
        this.correlationId = correlationId;
        this.createdAt = now;
        this.updatedAt = now;
    }

    public static SupportTicket open(UUID customerId, String category, TicketPriority priority, String subject,
                                     String description, String correlationId) {
        return new SupportTicket(customerId, category, priority == null ? TicketPriority.MEDIUM : priority,
                subject, description, correlationId);
    }

    @PreUpdate
    void refreshUpdatedAt() {
        updatedAt = LocalDateTime.now();
    }

    public void assign(String assignedTeam, UUID assignedAgentId, LocalDateTime assignedAt, LocalDateTime slaDueAt) {
        this.assignedTeam = assignedTeam;
        this.assignedAgentId = assignedAgentId;
        this.assignedAt = assignedAt;
        this.slaDueAt = slaDueAt;
    }

    public UUID getId() { return id; }
    public String getTicketNumber() { return ticketNumber; }
    public UUID getCustomerId() { return customerId; }
    public String getCategory() { return category; }
    public TicketPriority getPriority() { return priority; }
    public TicketStatus getStatus() { return status; }
    public String getSubject() { return subject; }
    public String getDescription() { return description; }
    public String getAssignedTeam() { return assignedTeam; }
    public UUID getAssignedAgentId() { return assignedAgentId; }
    public LocalDateTime getAssignedAt() { return assignedAt; }
    public LocalDateTime getSlaDueAt() { return slaDueAt; }
    public String getCorrelationId() { return correlationId; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public LocalDateTime getUpdatedAt() { return updatedAt; }
}
