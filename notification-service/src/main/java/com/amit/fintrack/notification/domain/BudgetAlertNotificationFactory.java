package com.amit.fintrack.notification.domain;

import com.amit.fintrack.notification.application.model.BudgetAlertEvent;
import com.amit.fintrack.notification.application.model.NewNotification;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.time.Clock;
import java.time.LocalDateTime;

@Component
@RequiredArgsConstructor
public class BudgetAlertNotificationFactory {

    private final Clock clock;

    public NewNotification create(BudgetAlertEvent event, NotificationType type) {
        return new NewNotification(
                event.userId(),
                buildTitle(type),
                event.message(),
                type,
                LocalDateTime.now(clock)
        );
    }

    private String buildTitle(NotificationType type) {
        return switch (type) {
            case BUDGET_WARNING -> "Budget warning";
            case BUDGET_EXCEEDED -> "Budget exceeded";
        };
    }
}
