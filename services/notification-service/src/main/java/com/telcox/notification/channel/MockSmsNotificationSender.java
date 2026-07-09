package com.telcox.notification.channel;

import com.telcox.notification.domain.NotificationChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

@Component
public class MockSmsNotificationSender implements NotificationChannelSender {

    private static final Logger log = LoggerFactory.getLogger(MockSmsNotificationSender.class);

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.SMS;
    }

    @Override
    public void send(NotificationMessage message) {
        log.info("Mock SMS sent to={} content={}", message.recipient(), message.content());
    }
}
