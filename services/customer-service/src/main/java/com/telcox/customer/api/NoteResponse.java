package com.telcox.customer.api;

import com.telcox.customer.domain.CustomerNote;

import java.time.LocalDateTime;
import java.util.UUID;

public record NoteResponse(
        UUID id,
        UUID customerId,
        UUID authorUserId,
        String noteText,
        LocalDateTime createdAt
) {
    public static NoteResponse from(CustomerNote note) {
        return new NoteResponse(note.getId(), note.getCustomerId(), note.getAuthorUserId(), note.getNoteText(), note.getCreatedAt());
    }
}
