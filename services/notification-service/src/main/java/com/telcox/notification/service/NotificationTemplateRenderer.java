package com.telcox.notification.service;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.springframework.stereotype.Component;

@Component
public class NotificationTemplateRenderer {

    private static final Pattern VARIABLE_PATTERN = Pattern.compile("\\{\\{\\s*([A-Za-z0-9_.-]+)\\s*}}");

    public String render(String template, Map<String, String> variables) {
        if (template == null) {
            return null;
        }

        Matcher matcher = VARIABLE_PATTERN.matcher(template);
        StringBuilder rendered = new StringBuilder();
        while (matcher.find()) {
            String variableName = matcher.group(1);
            String value = variables == null ? null : variables.get(variableName);
            if (value == null) {
                throw new IllegalArgumentException("Missing notification template variable: " + variableName);
            }
            matcher.appendReplacement(rendered, Matcher.quoteReplacement(value));
        }
        matcher.appendTail(rendered);
        return rendered.toString();
    }
}
