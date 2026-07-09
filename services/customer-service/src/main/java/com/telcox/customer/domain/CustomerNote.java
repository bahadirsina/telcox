package com.telcox.customer.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import java.time.LocalDateTime;
import java.util.UUID;

/**
 * CUST-03: Agent/temsilci notu. ER: db.sql -> CUSTOMER_SERVICE_NOTE
 */
@Entity
@Table(name = "customer_service_note")
public class CustomerNote {

    @Id
    private UUID id;

    @Column(name = "customer_id", nullable = false)
    private UUID customerId;

    /** logical ref -> identity-service.USER.id */
    @Column(name = "author_user_id")
    private UUID authorUserId;

    @Column(name = "note_text", columnDefinition = "text")
    private String noteText;

    @Column(name = "created_at", nullable = false)
    private LocalDateTime createdAt;

    protected CustomerNote() {
    }

    public CustomerNote(UUID customerId, UUID authorUserId, String noteText) {
        this.id = UUID.randomUUID();
        this.customerId = customerId;
        this.authorUserId = authorUserId;
        this.noteText = noteText;
        this.createdAt = LocalDateTime.now();
    }

    public UUID getId() {
        return id;
    }

    public UUID getCustomerId() {
        return customerId;
    }

    public UUID getAuthorUserId() {
        return authorUserId;
    }

    public String getNoteText() {
        return noteText;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }
}
