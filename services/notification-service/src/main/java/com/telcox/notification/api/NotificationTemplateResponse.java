package com.telcox.notification.api;

import com.telcox.notification.domain.NotificationChannel;
import com.telcox.notification.domain.NotificationTemplate;
import java.time.OffsetDateTime;
import java.util.UUID;

public record NotificationTemplateResponse(
        UUID id,
        String templateCode,
        NotificationChannel channel,
        String locale,
        String subjectTemplate,
        String contentTemplate,
        boolean active,
        OffsetDateTime createdAt,
        OffsetDateTime updatedAt
) {
    public static NotificationTemplateResponse from(NotificationTemplate template) {
        return new NotificationTemplateResponse(template.getId(), template.getTemplateCode(),
                template.getChannel(), template.getLocale(), template.getSubjectTemplate(),
                template.getContentTemplate(), template.isActive(), template.getCreatedAt(),
                template.getUpdatedAt());
    }
}
