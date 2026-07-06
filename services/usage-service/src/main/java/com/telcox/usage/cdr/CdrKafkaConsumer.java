package com.telcox.usage.cdr;

import com.telcox.common.event.EventEnvelope;
import jakarta.validation.Valid;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.validation.annotation.Validated;

@Component
@Validated
public class CdrKafkaConsumer {
    private final CdrProcessingService processingService;

    public CdrKafkaConsumer(CdrProcessingService processingService) {
        this.processingService = processingService;
    }

    @KafkaListener(
            topics = "${telcox.usage.cdr-topic:telcox.cdr.cdr-recorded.v1}",
            groupId = "${spring.kafka.consumer.group-id:usage-service}")
    public void consume(@Valid EventEnvelope<@Valid CdrEvent> event) {
        processingService.process(event);
    }
}
