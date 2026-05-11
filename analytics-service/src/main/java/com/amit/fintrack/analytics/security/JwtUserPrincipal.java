package com.amit.fintrack.analytics.security;

import java.util.UUID;

public record JwtUserPrincipal(
        UUID userId,
        String email,
        String role
) {
}