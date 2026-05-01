package com.amit.fintrack.budget.security;

import java.util.UUID;

public record JwtUserPrincipal(
        UUID userId,
        String email,
        String role
) {
}