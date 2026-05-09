package com.amit.fintrack.gateway.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

@Service
public class GatewayJwtService {

    private final String secretKey;

    public GatewayJwtService(@Value("${jwt.secret}") String secretKey) {
        this.secretKey = secretKey;
    }

    public JwtClaims validate(String token) {
        Claims claims = extractAllClaims(token);

        Date expiration = claims.getExpiration();
        String email = claims.getSubject();
        String userId = claims.get("userId", String.class);
        String role = claims.get("role", String.class);

        if (expiration == null || expiration.before(new Date())) {
            throw new IllegalArgumentException("JWT is expired");
        }

        if (!StringUtils.hasText(email)
                || !StringUtils.hasText(userId)
                || !StringUtils.hasText(role)) {
            throw new IllegalArgumentException("JWT is missing required claims");
        }

        return new JwtClaims(
                UUID.fromString(userId),
                email,
                role
        );
    }

    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}