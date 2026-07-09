package com.telcox.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import com.telcox.notification.api.SendTemplatedNotificationRequest;
import com.telcox.notification.domain.NotificationChannel;
import com.telcox.notification.domain.NotificationTemplate;
import com.telcox.notification.repository.NotificationTemplateRepository;
import java.util.Map;
import java.util.Optional;
import org.junit.jupiter.api.Test;

class NotificationTemplateServiceTest {

    private final NotificationTemplateRepository repository = mock(NotificationTemplateRepository.class);
    private final NotificationTemplateService service =
            new NotificationTemplateService(repository, new NotificationTemplateRenderer());

    @Test
    void rendersActiveTemplateWithVariables() {
        NotificationTemplate template = new NotificationTemplate("ticket.opened", NotificationChannel.EMAIL,
                "tr-TR", "Talep {{ticketNumber}}", "Merhaba {{customerName}}", true);
        when(repository.findByTemplateCodeAndChannelAndLocaleAndActiveTrue(
                "ticket.opened", NotificationChannel.EMAIL, "tr-TR")).thenReturn(Optional.of(template));

        RenderedNotification rendered = service.render(new SendTemplatedNotificationRequest(
                null, "ticket.opened", NotificationChannel.EMAIL, "tr-TR", "customer@telcox.local",
                Map.of("ticketNumber", "TCK-1001", "customerName", "Ayse")));

        assertThat(rendered.subject()).isEqualTo("Talep TCK-1001");
        assertThat(rendered.content()).isEqualTo("Merhaba Ayse");
        assertThat(rendered.templateCode()).isEqualTo("ticket.opened");
    }

    @Test
    void failsWhenRequiredVariableIsMissing() {
        NotificationTemplate template = new NotificationTemplate("ticket.opened", NotificationChannel.SMS,
                "tr-TR", null, "Talep {{ticketNumber}}", true);
        when(repository.findByTemplateCodeAndChannelAndLocaleAndActiveTrue(
                "ticket.opened", NotificationChannel.SMS, "tr-TR")).thenReturn(Optional.of(template));

        assertThatThrownBy(() -> service.render(new SendTemplatedNotificationRequest(
                null, "ticket.opened", NotificationChannel.SMS, "tr-TR", "+905551112233", Map.of())))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("ticketNumber");
    }
}
