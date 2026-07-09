package com.telcox.notification.channel;

public record NotificationMessage(String recipient, String subject, String content) {
}
