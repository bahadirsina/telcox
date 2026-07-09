package com.telcox.customer.api;

import jakarta.validation.constraints.NotBlank;

import java.util.UUID;

public record NoteRequest(
        UUID authorUserId,
        @NotBlank String noteText
) {
}
