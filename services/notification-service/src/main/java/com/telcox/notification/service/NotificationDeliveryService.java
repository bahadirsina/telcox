package com.telcox.notification.service;

import com.telcox.notification.api.NotificationDeliveryResponse;
import com.telcox.notification.api.SendNotificationRequest;
import com.telcox.notification.channel.NotificationChannelSender;
import com.telcox.notification.channel.NotificationMessage;
import com.telcox.notification.domain.NotificationChannel;
import com.telcox.notification.domain.NotificationDelivery;
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
    private final Map<NotificationChannel, NotificationChannelSender> senders;
    private final Clock clock;

    @Autowired
    public NotificationDeliveryService(NotificationDeliveryRepository repository,
                                       List<NotificationChannelSender> senders) {
        this(repository, senders, Clock.systemUTC());
    }

    NotificationDeliveryService(NotificationDeliveryRepository repository,
                                List<NotificationChannelSender> senders,
                                Clock clock) {
        this.repository = repository;
        this.senders = senders.stream().collect(Collectors.toMap(
                NotificationChannelSender::channel,
                Function.identity(),
                (first, ignored) -> first,
                () -> new EnumMap<>(NotificationChannel.class)));
        this.clock = clock;
    }

    @Transactional
    public NotificationDeliveryResponse send(SendNotificationRequest request, String correlationId) {
        NotificationChannelSender sender = senders.get(request.channel());
        if (sender == null) {
            throw new IllegalArgumentException("Unsupported notification channel: " + request.channel());
        }

        OffsetDateTime now = OffsetDateTime.now(clock);
        NotificationDelivery delivery = repository.save(new NotificationDelivery(
                request.customerId(), request.channel(), request.recipient(), request.subject(),
                request.content(), normalizeCorrelationId(correlationId), now));
        try {
            sender.send(new NotificationMessage(request.recipient(), request.subject(), request.content()));
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
