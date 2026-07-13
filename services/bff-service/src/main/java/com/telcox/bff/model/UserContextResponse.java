package com.telcox.bff.model;

import java.util.List;
import java.util.Set;

public record UserContextResponse(
        String userId,
        String username,
        String email,
        Set<String> roles,
        List<String> visibleSections
) {
}
