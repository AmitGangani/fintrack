package com.amit.fintrack.authservice.web.controller;

import com.amit.fintrack.authservice.application.AuthService;
import com.amit.fintrack.authservice.application.model.AuthResult;
import com.amit.fintrack.authservice.application.model.LoginCommand;
import com.amit.fintrack.authservice.application.model.RegisterCommand;
import com.amit.fintrack.authservice.web.dto.AuthResponse;
import com.amit.fintrack.authservice.web.dto.LoginRequest;
import com.amit.fintrack.authservice.web.dto.RegisterRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ResponseEntity<AuthResponse> register(@Valid @RequestBody RegisterRequest request) {
        AuthResponse response = toResponse(authService.register(toCommand(request)));
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest request) {
        AuthResponse response = toResponse(authService.login(toCommand(request)));
        return ResponseEntity.ok(response);
    }

    @GetMapping("/me")
    public ResponseEntity<String> me(Authentication authentication) {
        return ResponseEntity.ok(authentication.getName());
    }

    private RegisterCommand toCommand(RegisterRequest request) {
        return new RegisterCommand(request.fullName(), request.email(), request.password());
    }

    private LoginCommand toCommand(LoginRequest request) {
        return new LoginCommand(request.email(), request.password());
    }

    private AuthResponse toResponse(AuthResult result) {
        return new AuthResponse(result.userId(), result.email(), result.accessToken(), result.message());
    }
}
