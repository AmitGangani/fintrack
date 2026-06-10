package com.amit.fintrack.notification.application;

import com.amit.fintrack.notification.application.model.BudgetAlertEvent;
import com.amit.fintrack.notification.application.model.NewNotification;
import com.amit.fintrack.notification.application.port.NotificationStore;
import com.amit.fintrack.notification.application.port.ProcessedEventStore;
import com.amit.fintrack.notification.domain.BudgetAlertNotificationFactory;
import com.amit.fintrack.notification.domain.NotificationType;
import com.amit.fintrack.notification.domain.NotificationTypeResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class BudgetAlertNotificationHandler {

    private final NotificationStore notificationStore;
    private final BudgetAlertNotificationFactory budgetAlertNotificationFactory;
    private final NotificationTypeResolver notificationTypeResolver;
    private final ProcessedEventStore processedEventStore;

    @Transactional
    public void handle(BudgetAlertEvent event) {
        Optional<NotificationType> type = notificationTypeResolver.resolve(event.alertType());
        if (type.isEmpty()) {
            log.warn("Skipping unsupported notification alert type: {}", event.alertType());
            return;
        }

        if (!processedEventStore.reserve(event.eventId(), event.alertType())) {
            return;
        }

        NewNotification notification = budgetAlertNotificationFactory.create(event, type.get());
        notificationStore.save(notification);

        log.info("Created notification for userId={}, type={}", event.userId(), type.get());
    }
}
