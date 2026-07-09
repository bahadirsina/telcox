package com.telcox.notification.api;

import com.telcox.notification.service.NotificationTemplateService;
import jakarta.validation.Valid;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/notification-templates")
public class NotificationTemplateController {

    private final NotificationTemplateService templateService;

    public NotificationTemplateController(NotificationTemplateService templateService) {
        this.templateService = templateService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public NotificationTemplateResponse upsert(@Valid @RequestBody NotificationTemplateRequest request) {
        return templateService.upsert(request);
    }

    @GetMapping
    public List<NotificationTemplateResponse> list(@RequestParam(required = false) String templateCode) {
        return templateService.list(templateCode);
    }
}
