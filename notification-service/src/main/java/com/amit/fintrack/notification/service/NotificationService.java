package com.amit.fintrack.notification.service;

import com.amit.fintrack.notification.dto.NotificationResponse;
import com.amit.fintrack.notification.entity.Notification;
import com.amit.fintrack.notification.entity.NotificationType;
import com.amit.fintrack.notification.entity.ProcessedKafkaEvent;
import com.amit.fintrack.notification.event.BudgetAlertEvent;
import com.amit.fintrack.notification.exception.NotificationNotFoundException;
import com.amit.fintrack.notification.repository.NotificationRepository;
import com.amit.fintrack.notification.repository.ProcessedKafkaEventRepository;
import com.amit.fintrack.notification.security.CurrentUserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final ProcessedKafkaEventRepository processedKafkaEventRepository;
    private final CurrentUserService currentUserService;

    public List<NotificationResponse> getMyNotifications() {
        UUID currentUserId = currentUserService.getCurrentUserId();

        return notificationRepository.findByUserIdOrderByCreatedAtDesc(currentUserId)
                .stream()
                .map(this::toResponse)
                .toList();
    }

    public NotificationResponse markAsRead(UUID notificationId) {
        UUID currentUserId = currentUserService.getCurrentUserId();

        Notification notification = notificationRepository.findByIdAndUserId(
                        notificationId,
                        currentUserId
                )
                .orElseThrow(() -> new NotificationNotFoundException("Notification not found"));

        notification.setRead(true);
        notification.setReadAt(LocalDateTime.now());

        return toResponse(notificationRepository.save(notification));
    }

    @Transactional
    public void handleBudgetAlertEvent(BudgetAlertEvent event) {
        if (processedKafkaEventRepository.existsById(event.eventId())) {
            log.info("Skipping duplicate notification event: {}", event.eventId());
            return;
        }

        NotificationType type = NotificationType.valueOf(event.alertType());

        Notification notification = Notification.builder()
                .userId(event.userId())
                .title(buildTitle(type))
                .message(event.message())
                .type(type)
                .read(false)
                .createdAt(LocalDateTime.now())
                .build();

        notificationRepository.save(notification);

        ProcessedKafkaEvent processedEvent = ProcessedKafkaEvent.builder()
                .eventId(event.eventId())
                .eventType(event.alertType())
                .processedAt(LocalDateTime.now())
                .build();

        processedKafkaEventRepository.save(processedEvent);

        log.info("Created notification for userId={}, type={}", event.userId(), type);
    }

    private String buildTitle(NotificationType type) {
        return switch (type) {
            case BUDGET_WARNING -> "Budget warning";
            case BUDGET_EXCEEDED -> "Budget exceeded";
        };
    }

    private NotificationResponse toResponse(Notification notification) {
        return new NotificationResponse(
                notification.getId(),
                notification.getTitle(),
                notification.getMessage(),
                notification.getType(),
                notification.isRead(),
                notification.getCreatedAt(),
                notification.getReadAt()
        );
    }
}