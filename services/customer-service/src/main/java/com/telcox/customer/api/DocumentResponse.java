package com.telcox.customer.api;

import com.telcox.customer.domain.CustomerDocument;
import com.telcox.customer.domain.DocumentType;
import com.telcox.customer.domain.VerificationStatus;

import java.time.LocalDateTime;
import java.util.UUID;

public record DocumentResponse(
        UUID id,
        UUID customerId,
        DocumentType documentType,
        String documentNumber,
        String fileUrl,
        VerificationStatus verificationStatus,
        UUID uploadedByUserId,
        LocalDateTime uploadedAt,
        LocalDateTime verifiedAt
) {
    public static DocumentResponse from(CustomerDocument document) {
        return new DocumentResponse(
                document.getId(),
                document.getCustomerId(),
                document.getDocumentType(),
                document.getDocumentNumber(),
                document.getFileUrl(),
                document.getVerificationStatus(),
                document.getUploadedByUserId(),
                document.getUploadedAt(),
                document.getVerifiedAt()
        );
    }
}
