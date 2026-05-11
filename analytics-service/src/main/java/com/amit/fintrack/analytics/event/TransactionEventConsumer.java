package com.amit.fintrack.analytics.event;

import com.amit.fintrack.analytics.service.AnalyticsEventService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import tools.jackson.databind.ObjectMapper;

@Component
@RequiredArgsConstructor
@Slf4j
public class TransactionEventConsumer {

    private final ObjectMapper objectMapper;
    private final AnalyticsEventService analyticsEventService;

    @KafkaListener(
            topics = KafkaTopics.TRANSACTION_EVENTS,
            groupId = "analytics-service-group"
    )
    public void consume(String payload) {
        try {
            TransactionLifecycleEvent event = objectMapper.readValue(
                    payload,
                    TransactionLifecycleEvent.class
            );

            log.info("Analytics Service received transaction event: {}", event);
            analyticsEventService.handleTransactionEvent(event);

        } catch (Exception exception) {
            log.error("Failed to process analytics event payload={}", payload, exception);
            throw new RuntimeException(exception);
        }
    }
}