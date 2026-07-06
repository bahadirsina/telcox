package com.telcox.notification.channel;

import com.telcox.notification.domain.NotificationChannel;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Component;

@Component
public class EmailNotificationSender implements NotificationChannelSender {

    private final JavaMailSender mailSender;

    public EmailNotificationSender(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public NotificationChannel channel() {
        return NotificationChannel.EMAIL;
    }

    @Override
    public void send(NotificationMessage message) {
        SimpleMailMessage mail = new SimpleMailMessage();
        mail.setTo(message.recipient());
        mail.setSubject(message.subject());
        mail.setText(message.content());
        mailSender.send(mail);
    }
}
