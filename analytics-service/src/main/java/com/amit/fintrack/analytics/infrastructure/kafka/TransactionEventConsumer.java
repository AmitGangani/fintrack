package com.amit.fintrack.analytics.infrastructure.kafka;

import com.amit.fintrack.analytics.application.AnalyticsEventService;
import com.amit.fintrack.analytics.application.model.TransactionAnalyticsEvent;
import com.amit.fintrack.analytics.application.model.TransactionEventType;
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
            analyticsEventService.handleTransactionEvent(toApplicationEvent(event));

        } catch (Exception exception) {
            log.error("Failed to process analytics event payload={}", payload, exception);
            throw new RuntimeException(exception);
        }
    }

    private TransactionAnalyticsEvent toApplicationEvent(TransactionLifecycleEvent event) {
        return new TransactionAnalyticsEvent(
                event.eventId(),
                TransactionEventType.valueOf(event.eventType().name()),
                event.userId(),
                event.oldType(),
                event.oldCategory(),
                event.oldAmount(),
                event.oldTransactionDate(),
                event.newType(),
                event.newCategory(),
                event.newAmount(),
                event.newTransactionDate()
        );
    }
}
