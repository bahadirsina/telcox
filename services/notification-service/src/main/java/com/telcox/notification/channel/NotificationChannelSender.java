package com.telcox.notification.channel;

import com.telcox.notification.domain.NotificationChannel;

public interface NotificationChannelSender {
    NotificationChannel channel();
    void send(NotificationMessage message);
}
