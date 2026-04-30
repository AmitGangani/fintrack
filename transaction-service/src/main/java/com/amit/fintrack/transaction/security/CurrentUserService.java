package com.amit.fintrack.transaction.security;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CurrentUserService {

    public UUID getCurrentUserId() {
        Authentication authentication = SecurityContextHolder
                .getContext()
                .getAuthentication();

        JwtUserPrincipal principal = (JwtUserPrincipal) authentication.getPrincipal();

        return principal.userId();
    }
}