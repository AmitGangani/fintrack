package com.amit.fintrack.authservice.web.dto;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String email,
        String accessToken,
        String message
) {
}