package com.amit.fintrack.authservice.service;

import com.amit.fintrack.authservice.dto.AuthResponse;
import com.amit.fintrack.authservice.dto.LoginRequest;
import com.amit.fintrack.authservice.dto.RegisterRequest;
import com.amit.fintrack.authservice.entity.AppUser;
import com.amit.fintrack.authservice.entity.Role;
import com.amit.fintrack.authservice.exception.DuplicateEmailException;
import com.amit.fintrack.authservice.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.core.Authentication;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.lang.reflect.Proxy;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class AuthServiceTest {

    private static final UUID USER_ID = UUID.fromString("00000000-0000-0000-0000-000000000001");

    @Test
    void registerCreatesUserWithEncodedPasswordAndReturnsToken() {
        FakeUserRepository userRepository = new FakeUserRepository();
        AuthService authService = new AuthService(
                userRepository.repository(),
                new FixedPasswordEncoder(),
                authentication -> authentication,
                new FixedJwtService()
        );

        AuthResponse response = authService.register(
                new RegisterRequest("Amit", "amit@example.com", "secret123")
        );

        AppUser savedUser = userRepository.savedUser;
        assertEquals("Amit", savedUser.getFullName());
        assertEquals("amit@example.com", savedUser.getEmail());
        assertEquals("encoded-password", savedUser.getPassword());
        assertEquals(Role.USER, savedUser.getRole());

        assertEquals(USER_ID, response.userId());
        assertEquals("amit@example.com", response.email());
        assertEquals("jwt-token", response.accessToken());
    }

    @Test
    void registerRejectsDuplicateEmail() {
        FakeUserRepository userRepository = new FakeUserRepository();
        userRepository.emailExists = true;
        AuthService authService = new AuthService(
                userRepository.repository(),
                new FixedPasswordEncoder(),
                authentication -> authentication,
                new FixedJwtService()
        );

        assertThrows(
                DuplicateEmailException.class,
                () -> authService.register(new RegisterRequest("Amit", "amit@example.com", "secret123"))
        );

        assertEquals(0, userRepository.saveCount);
    }

    @Test
    void loginAuthenticatesAndReturnsToken() {
        FakeUserRepository userRepository = new FakeUserRepository();
        userRepository.user = AppUser.builder()
                .id(USER_ID)
                .email("amit@example.com")
                .password("encoded-password")
                .role(Role.USER)
                .build();
        RecordingAuthenticationManager authenticationManager = new RecordingAuthenticationManager();
        AuthService authService = new AuthService(
                userRepository.repository(),
                new FixedPasswordEncoder(),
                authenticationManager,
                new FixedJwtService()
        );

        AuthResponse response = authService.login(new LoginRequest("amit@example.com", "secret123"));

        assertEquals(1, authenticationManager.authenticateCount);
        assertEquals(USER_ID, response.userId());
        assertEquals("amit@example.com", response.email());
        assertEquals("jwt-token", response.accessToken());
    }

    @Test
    void loginThrowsWhenAuthenticatedUserIsMissing() {
        FakeUserRepository userRepository = new FakeUserRepository();
        AuthService authService = new AuthService(
                userRepository.repository(),
                new FixedPasswordEncoder(),
                authentication -> authentication,
                new FixedJwtService()
        );

        assertThrows(
                RuntimeException.class,
                () -> authService.login(new LoginRequest("missing@example.com", "secret123"))
        );
    }

    private static final class FakeUserRepository {
        private boolean emailExists;
        private AppUser user;
        private AppUser savedUser;
        private int saveCount;

        private UserRepository repository() {
            return (UserRepository) Proxy.newProxyInstance(
                    UserRepository.class.getClassLoader(),
                    new Class<?>[]{UserRepository.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "existsByEmail" -> emailExists;
                        case "findByEmail" -> Optional.ofNullable(user);
                        case "save" -> {
                            AppUser saved = (AppUser) args[0];
                            saved.setId(USER_ID);
                            savedUser = saved;
                            saveCount++;
                            yield saved;
                        }
                        case "toString" -> "FakeUserRepository";
                        default -> throw new UnsupportedOperationException(method.getName());
                    }
            );
        }
    }

    private static final class FixedPasswordEncoder implements PasswordEncoder {
        @Override
        public String encode(CharSequence rawPassword) {
            return "encoded-password";
        }

        @Override
        public boolean matches(CharSequence rawPassword, String encodedPassword) {
            return false;
        }
    }

    private static final class RecordingAuthenticationManager implements AuthenticationManager {
        private int authenticateCount;

        @Override
        public Authentication authenticate(Authentication authentication) {
            authenticateCount++;
            return authentication;
        }
    }

    private static final class FixedJwtService extends JwtService {
        @Override
        public String generateToken(AppUser user) {
            return "jwt-token";
        }
    }
}
