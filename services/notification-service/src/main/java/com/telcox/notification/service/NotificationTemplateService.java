package com.telcox.notification.service;

import com.telcox.notification.api.NotificationTemplateRequest;
import com.telcox.notification.api.NotificationTemplateResponse;
import com.telcox.notification.api.SendTemplatedNotificationRequest;
import com.telcox.notification.domain.NotificationChannel;
import com.telcox.notification.domain.NotificationTemplate;
import com.telcox.notification.repository.NotificationTemplateRepository;
import java.util.List;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationTemplateService {

    static final String DEFAULT_LOCALE = "tr-TR";

    private final NotificationTemplateRepository repository;
    private final NotificationTemplateRenderer renderer;

    public NotificationTemplateService(NotificationTemplateRepository repository,
                                       NotificationTemplateRenderer renderer) {
        this.repository = repository;
        this.renderer = renderer;
    }

    @Transactional
    public NotificationTemplateResponse upsert(NotificationTemplateRequest request) {
        String locale = normalizeLocale(request.locale());
        boolean active = request.active() == null || request.active();
        NotificationTemplate template = repository
                .findByTemplateCodeAndChannelAndLocale(request.templateCode(), request.channel(), locale)
                .map(existing -> {
                    existing.update(request.subjectTemplate(), request.contentTemplate(), active);
                    return existing;
                })
                .orElseGet(() -> new NotificationTemplate(request.templateCode(), request.channel(), locale,
                        request.subjectTemplate(), request.contentTemplate(), active));
        return NotificationTemplateResponse.from(repository.save(template));
    }

    @Transactional(readOnly = true)
    public List<NotificationTemplateResponse> list(String templateCode) {
        List<NotificationTemplate> templates = templateCode == null || templateCode.isBlank()
                ? repository.findAll()
                : repository.findAllByTemplateCodeOrderByChannelAscLocaleAsc(templateCode);
        return templates.stream().map(NotificationTemplateResponse::from).toList();
    }

    @Transactional(readOnly = true)
    public RenderedNotification render(SendTemplatedNotificationRequest request) {
        String locale = normalizeLocale(request.locale());
        NotificationTemplate template = repository
                .findByTemplateCodeAndChannelAndLocaleAndActiveTrue(request.templateCode(), request.channel(), locale)
                .or(() -> repository.findByTemplateCodeAndChannelAndLocaleAndActiveTrue(
                        request.templateCode(), request.channel(), DEFAULT_LOCALE))
                .orElseThrow(() -> new IllegalArgumentException("Active notification template not found: "
                        + request.templateCode() + " / " + request.channel() + " / " + locale));

        return new RenderedNotification(request.customerId(), template.getTemplateCode(), request.channel(),
                request.recipient(), renderer.render(template.getSubjectTemplate(), request.variables()),
                renderer.render(template.getContentTemplate(), request.variables()));
    }

    private String normalizeLocale(String locale) {
        return locale == null || locale.isBlank() ? DEFAULT_LOCALE : locale;
    }
}
