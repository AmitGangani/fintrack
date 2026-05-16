package com.amit.fintrack.gateway.dto;

import java.time.LocalDateTime;

public record FallbackResponse(
        LocalDateTime timestamp,
        int status,
        String error,
        String message,
        String service,
        String path
) {
}