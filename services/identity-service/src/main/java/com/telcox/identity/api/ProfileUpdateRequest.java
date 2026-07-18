package com.telcox.identity.api;

import jakarta.validation.constraints.Size;

public record ProfileUpdateRequest(
        @Size(max = 255) String displayName,
        @Size(max = 30) String phoneNumber,
        @Size(max = 20) String locale
) {
}
