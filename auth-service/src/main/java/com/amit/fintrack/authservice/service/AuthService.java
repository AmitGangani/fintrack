package com.amit.fintrack.authservice.service;

import com.amit.fintrack.authservice.dto.AuthResponse;
import com.amit.fintrack.authservice.dto.LoginRequest;
import com.amit.fintrack.authservice.dto.RegisterRequest;
import com.amit.fintrack.authservice.entity.AppUser;
import com.amit.fintrack.authservice.entity.Role;
import com.amit.fintrack.authservice.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthenticationManager authenticationManager;
    private final JwtService jwtService;

    public AuthResponse register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.email())) {
            throw new RuntimeException("Email already registered");
        }

        AppUser user = AppUser.builder()
                .fullName(request.fullName())
                .email(request.email())
                .password(passwordEncoder.encode(request.password()))
                .role(Role.USER)
                .createdAt(LocalDateTime.now())
                .build();

        AppUser savedUser = userRepository.save(user);

        String token = jwtService.generateToken(savedUser);

        return new AuthResponse(
                savedUser.getId(),
                savedUser.getEmail(),
                token,
                "User registered successfully"
        );
    }

    public AuthResponse login(LoginRequest request) {

        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.email(),
                        request.password()
                )
        );

        AppUser user = userRepository.findByEmail(request.email())
                .orElseThrow(() -> new RuntimeException("User not found"));

        String token = jwtService.generateToken(user);

        return new AuthResponse(
                user.getId(),
                user.getEmail(),
                token,
                "Login successful"
        );
    }
}