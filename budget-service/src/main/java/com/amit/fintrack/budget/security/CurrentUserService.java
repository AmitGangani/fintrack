package com.amit.fintrack.budget.security;

import com.amit.fintrack.budget.security.JwtUserPrincipal;
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