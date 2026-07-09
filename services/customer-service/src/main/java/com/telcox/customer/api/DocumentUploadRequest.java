package com.telcox.customer.api;

import com.telcox.customer.domain.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

import java.util.UUID;

public record DocumentUploadRequest(
        @NotNull DocumentType documentType,
        String documentNumber,
        @NotBlank String fileUrl,
        UUID uploadedByUserId
) {
}
