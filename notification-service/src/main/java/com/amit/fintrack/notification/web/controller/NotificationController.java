package com.amit.fintrack.notification.web.controller;

import com.amit.fintrack.notification.application.NotificationService;
import com.amit.fintrack.notification.application.model.NotificationView;
import com.amit.fintrack.notification.web.dto.NotificationResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/notifications")
@RequiredArgsConstructor
public class NotificationController {

    private final NotificationService notificationService;

    @GetMapping
    public ResponseEntity<List<NotificationResponse>> getMyNotifications() {
        return ResponseEntity.ok(notificationService.getMyNotifications().stream().map(this::toResponse).toList());
    }

    @PutMapping("/{notificationId}/read")
    public ResponseEntity<NotificationResponse> markAsRead(
            @PathVariable UUID notificationId
    ) {
        return ResponseEntity.ok(toResponse(notificationService.markAsRead(notificationId)));
    }

    private NotificationResponse toResponse(NotificationView notification) {
        return new NotificationResponse(
                notification.id(),
                notification.title(),
                notification.message(),
                notification.type(),
                notification.read(),
                notification.createdAt(),
                notification.readAt()
        );
    }
}
