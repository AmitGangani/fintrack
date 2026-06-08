package com.amit.fintrack.authservice.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidatorResult;
import org.springframework.security.oauth2.jose.jws.MacAlgorithm;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;
import java.util.Set;
import java.util.UUID;

@Configuration
public class JwtSecurityConfig {

    private static final Set<String> ALLOWED_ROLES = Set.of("USER", "ADMIN");

    @Bean
    public JwtDecoder jwtDecoder(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.issuer}") String issuer,
            @Value("${jwt.audience}") String audience
    ) {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder
                .withSecretKey(jwtSecretKey(jwtSecret))
                .macAlgorithm(MacAlgorithm.HS256)
                .build();

        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(
                JwtValidators.createDefaultWithIssuer(requireText(issuer, "jwt.issuer")),
                audienceValidator(requireText(audience, "jwt.audience")),
                subjectValidator(),
                userIdValidator(),
                roleValidator()
        ));

        return jwtDecoder;
    }

    @Bean
    public JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtGrantedAuthoritiesConverter authoritiesConverter = new JwtGrantedAuthoritiesConverter();
        authoritiesConverter.setAuthoritiesClaimName("role");
        authoritiesConverter.setAuthorityPrefix("ROLE_");

        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter(authoritiesConverter);
        return converter;
    }

    private SecretKey jwtSecretKey(String jwtSecret) {
        String secret = requireText(jwtSecret, "jwt.secret");
        byte[] keyBytes;

        try {
            keyBytes = Base64.getDecoder().decode(secret);
        } catch (IllegalArgumentException ex) {
            throw new IllegalStateException("jwt.secret must be Base64 encoded", ex);
        }

        if (keyBytes.length < 32) {
            throw new IllegalStateException("jwt.secret must decode to at least 32 bytes for HS256");
        }

        return new SecretKeySpec(keyBytes, "HmacSHA256");
    }

    private OAuth2TokenValidator<Jwt> audienceValidator(String audience) {
        return jwt -> jwt.getAudience().contains(audience)
                ? OAuth2TokenValidatorResult.success()
                : invalid("JWT audience is invalid");
    }

    private OAuth2TokenValidator<Jwt> subjectValidator() {
        return jwt -> StringUtils.hasText(jwt.getSubject())
                ? OAuth2TokenValidatorResult.success()
                : invalid("JWT subject is required");
    }

    private OAuth2TokenValidator<Jwt> userIdValidator() {
        return jwt -> isUuid(jwt.getClaimAsString("userId"))
                ? OAuth2TokenValidatorResult.success()
                : invalid("JWT userId must be a UUID");
    }

    private OAuth2TokenValidator<Jwt> roleValidator() {
        return jwt -> ALLOWED_ROLES.contains(jwt.getClaimAsString("role"))
                ? OAuth2TokenValidatorResult.success()
                : invalid("JWT role is invalid");
    }

    private OAuth2TokenValidatorResult invalid(String message) {
        return OAuth2TokenValidatorResult.failure(
                new OAuth2Error("invalid_token", message, null)
        );
    }

    private String requireText(String value, String propertyName) {
        if (!StringUtils.hasText(value)) {
            throw new IllegalStateException(propertyName + " must be configured");
        }

        return value.trim();
    }

    private boolean isUuid(String value) {
        if (!StringUtils.hasText(value)) {
            return false;
        }

        try {
            UUID.fromString(value);
            return true;
        } catch (IllegalArgumentException ex) {
            return false;
        }
    }
}
