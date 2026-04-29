package com.amit.fintrack.account.security;

import java.util.UUID;

public record JwtUserPrincipal(
        UUID userId,
        String email,
        String role
) {
}