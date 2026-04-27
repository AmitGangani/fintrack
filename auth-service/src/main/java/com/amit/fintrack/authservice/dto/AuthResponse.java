package com.amit.fintrack.authservice.dto;

import java.util.UUID;

public record AuthResponse(
        UUID userId,
        String email,
        String message
) {
}