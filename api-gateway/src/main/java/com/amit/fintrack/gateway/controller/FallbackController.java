package com.amit.fintrack.gateway.controller;

import com.amit.fintrack.gateway.dto.FallbackResponse;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDateTime;

@RestController
@RequestMapping("/fallback")
public class FallbackController {

    @RequestMapping("/{serviceName}")
    public ResponseEntity<FallbackResponse> fallback(
            @PathVariable String serviceName,
            HttpServletRequest request
    ) {
        FallbackResponse response = new FallbackResponse(
                LocalDateTime.now(),
                HttpStatus.SERVICE_UNAVAILABLE.value(),
                HttpStatus.SERVICE_UNAVAILABLE.getReasonPhrase(),
                buildMessage(serviceName),
                serviceName,
                request.getRequestURI()
        );

        return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE).body(response);
    }

    private String buildMessage(String serviceName) {
        return switch (serviceName) {
            case "auth-service" -> "Auth Service is temporarily unavailable. Please try again later.";
            case "account-service" -> "Account Service is temporarily unavailable. Please try again later.";
            case "transaction-service" -> "Transaction Service is temporarily unavailable. Please try again later.";
            case "budget-service" -> "Budget Service is temporarily unavailable. Please try again later.";
            case "notification-service" -> "Notification Service is temporarily unavailable. Please try again later.";
            case "analytics-service" -> "Analytics Service is temporarily unavailable. Please try again later.";
            default -> "Requested service is temporarily unavailable. Please try again later.";
        };
    }
}
