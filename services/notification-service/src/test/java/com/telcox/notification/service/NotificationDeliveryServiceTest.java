package com.telcox.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.telcox.notification.api.SendNotificationRequest;
import com.telcox.notification.channel.NotificationChannelSender;
import com.telcox.notification.domain.DeliveryStatus;
import com.telcox.notification.domain.NotificationChannel;
import com.telcox.notification.projection.CustomerPreferenceProjectionService;
import com.telcox.notification.repository.NotificationDeliveryRepository;
import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.List;
import java.util.UUID;
import org.junit.jupiter.api.Test;

class NotificationDeliveryServiceTest {

    private final NotificationDeliveryRepository repository = mock(NotificationDeliveryRepository.class);
    private final NotificationChannelSender sender = mock(NotificationChannelSender.class);
    private final CustomerPreferenceProjectionService preferenceService = mock(CustomerPreferenceProjectionService.class);
    private final Clock clock = Clock.fixed(Instant.parse("2026-07-06T10:00:00Z"), ZoneOffset.UTC);

    @Test
    void sendsAndMarksDeliveryAsSent() {
        when(sender.channel()).thenReturn(NotificationChannel.SMS);
        when(preferenceService.canSend(null, NotificationChannel.SMS)).thenReturn(true);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        NotificationDeliveryService service =
                new NotificationDeliveryService(repository, List.of(sender), preferenceService, clock);

        var response = service.send(new SendNotificationRequest(null, NotificationChannel.SMS,
                "+905551112233", null, "Test SMS"), "corr-1");

        assertThat(response.status()).isEqualTo(DeliveryStatus.SENT);
        assertThat(response.correlationId()).isEqualTo("corr-1");
        verify(sender).send(any());
    }

    @Test
    void keepsTemplateCodeWhenSendingRenderedNotification() {
        when(sender.channel()).thenReturn(NotificationChannel.EMAIL);
        when(preferenceService.canSend(null, NotificationChannel.EMAIL)).thenReturn(true);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        NotificationDeliveryService service =
                new NotificationDeliveryService(repository, List.of(sender), preferenceService, clock);

        var response = service.send(new RenderedNotification(null, "ticket.opened", NotificationChannel.EMAIL,
                "customer@example.com", "Ticket opened", "Body"), "corr-template");

        assertThat(response.status()).isEqualTo(DeliveryStatus.SENT);
        assertThat(response.templateCode()).isEqualTo("ticket.opened");
        verify(sender).send(any());
    }

    @Test
    void recordsFailureWithoutLosingDeliveryHistory() {
        when(sender.channel()).thenReturn(NotificationChannel.EMAIL);
        when(preferenceService.canSend(null, NotificationChannel.EMAIL)).thenReturn(true);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        doThrow(new IllegalStateException("SMTP unavailable")).when(sender).send(any());
        NotificationDeliveryService service =
                new NotificationDeliveryService(repository, List.of(sender), preferenceService, clock);

        var response = service.send(new SendNotificationRequest(null, NotificationChannel.EMAIL,
                "customer@example.com", "Subject", "Body"), null);

        assertThat(response.status()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(response.failureReason()).isEqualTo("SMTP unavailable");
    }

    @Test
    void blocksDeliveryWhenCustomerOptedOutFromChannel() {
        UUID customerId = UUID.fromString("1736719b-89fb-49a6-8470-8690fd65c761");
        when(sender.channel()).thenReturn(NotificationChannel.SMS);
        when(preferenceService.canSend(customerId, NotificationChannel.SMS)).thenReturn(false);
        when(repository.save(any())).thenAnswer(invocation -> invocation.getArgument(0));
        NotificationDeliveryService service =
                new NotificationDeliveryService(repository, List.of(sender), preferenceService, clock);

        var response = service.send(new SendNotificationRequest(customerId, NotificationChannel.SMS,
                "+905551112233", null, "Blocked SMS"), "corr-opt-out");

        assertThat(response.status()).isEqualTo(DeliveryStatus.FAILED);
        assertThat(response.failureReason()).contains("opted out");
        verify(sender, never()).send(any());
    }
}
