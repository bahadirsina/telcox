package com.telcox.notification.service;

import com.telcox.notification.api.NotificationDeliveryResponse;
import com.telcox.notification.api.SendNotificationRequest;
import com.telcox.notification.channel.NotificationChannelSender;
import com.telcox.notification.channel.NotificationMessage;
import com.telcox.notification.domain.NotificationChannel;
import com.telcox.notification.domain.NotificationDelivery;
import com.telcox.notification.projection.CustomerPreferenceProjectionService;
import com.telcox.notification.repository.NotificationDeliveryRepository;
import java.time.Clock;
import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.function.Function;
import java.util.stream.Collectors;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationDeliveryService {

    private final NotificationDeliveryRepository repository;
    private final CustomerPreferenceProjectionService preferenceService;
    private final Map<NotificationChannel, NotificationChannelSender> senders;
    private final Clock clock;

    @Autowired
    public NotificationDeliveryService(NotificationDeliveryRepository repository,
                                       List<NotificationChannelSender> senders,
                                       CustomerPreferenceProjectionService preferenceService) {
        this(repository, senders, preferenceService, Clock.systemUTC());
    }

    NotificationDeliveryService(NotificationDeliveryRepository repository,
                                List<NotificationChannelSender> senders,
                                CustomerPreferenceProjectionService preferenceService,
                                Clock clock) {
        this.repository = repository;
        this.preferenceService = preferenceService;
        this.senders = senders.stream().collect(Collectors.toMap(
                NotificationChannelSender::channel,
                Function.identity(),
                (first, ignored) -> first,
                () -> new EnumMap<>(NotificationChannel.class)));
        this.clock = clock;
    }

    @Transactional
    public NotificationDeliveryResponse send(SendNotificationRequest request, String correlationId) {
        return send(request.customerId(), request.channel(), request.recipient(), request.subject(),
                request.content(), null, correlationId);
    }

    @Transactional
    public NotificationDeliveryResponse send(RenderedNotification notification, String correlationId) {
        return send(notification.customerId(), notification.channel(), notification.recipient(),
                notification.subject(), notification.content(), notification.templateCode(), correlationId);
    }

    private NotificationDeliveryResponse send(UUID customerId, NotificationChannel channel, String recipient,
                                              String subject, String content, String templateCode,
                                              String correlationId) {
        NotificationChannelSender sender = senders.get(channel);
        if (sender == null) {
            throw new IllegalArgumentException("Unsupported notification channel: " + channel);
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        NotificationDelivery delivery = repository.save(new NotificationDelivery(
                customerId, channel, recipient, subject, content, templateCode, normalizeCorrelationId(correlationId),
                now));
        if (!preferenceService.canSend(customerId, channel)) {
            delivery.markFailed("Customer opted out from " + channel + " notifications");
            return NotificationDeliveryResponse.from(repository.save(delivery));
        }

        try {
            sender.send(new NotificationMessage(recipient, subject, content));
            delivery.markSent(OffsetDateTime.now(clock));
        } catch (RuntimeException exception) {
            delivery.markFailed(limitMessage(exception.getMessage()));
        }
        return NotificationDeliveryResponse.from(repository.save(delivery));
    }

    @Transactional(readOnly = true)
    public List<NotificationDeliveryResponse> list(UUID customerId) {
        List<NotificationDelivery> deliveries = customerId == null
                ? repository.findAll()
                : repository.findAllByCustomerIdOrderByCreatedAtDesc(customerId);
        return deliveries.stream().map(NotificationDeliveryResponse::from).toList();
    }

    private String normalizeCorrelationId(String correlationId) {
        return correlationId == null || correlationId.isBlank() ? UUID.randomUUID().toString() : correlationId;
    }

    private String limitMessage(String message) {
        if (message == null) {
            return "Notification channel failed";
        }
        return message.length() <= 500 ? message : message.substring(0, 500);
    }
}
