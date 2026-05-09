package com.amit.fintrack.gateway.security;

import java.util.UUID;

public record JwtClaims(
        UUID userId,
        String email,
        String role
) {
}
