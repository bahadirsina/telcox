package com.telcox.notification.repository;

import com.telcox.notification.domain.NotificationChannel;
import com.telcox.notification.domain.NotificationTemplate;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import org.springframework.data.jpa.repository.JpaRepository;

public interface NotificationTemplateRepository extends JpaRepository<NotificationTemplate, UUID> {

    Optional<NotificationTemplate> findByTemplateCodeAndChannelAndLocale(
            String templateCode, NotificationChannel channel, String locale);

    Optional<NotificationTemplate> findByTemplateCodeAndChannelAndLocaleAndActiveTrue(
            String templateCode, NotificationChannel channel, String locale);

    List<NotificationTemplate> findAllByTemplateCodeOrderByChannelAscLocaleAsc(String templateCode);
}
