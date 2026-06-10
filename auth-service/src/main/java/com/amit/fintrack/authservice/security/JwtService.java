package com.amit.fintrack.authservice.security;

import com.amit.fintrack.authservice.application.model.UserRecord;
import com.amit.fintrack.authservice.application.port.TokenIssuer;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.Map;

@Service
public class JwtService implements TokenIssuer {

    @Value("${jwt.secret}")
    private String secretKey;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    @Value("${jwt.issuer}")
    private String issuer;

    @Value("${jwt.audience}")
    private String audience;

    @Override
    public String issueToken(UserRecord user) {
        long now = System.currentTimeMillis();
        Map<String, Object> claims = Map.of(
                "userId", user.id().toString()
        );

        return Jwts.builder()
                .claims(claims)
                .issuer(issuer)
                .subject(user.email())
                .audience().add(audience).and()
                .issuedAt(new Date(now))
                .expiration(new Date(now + jwtExpirationMs))
                .signWith(getSigningKey())
                .compact();
    }

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64.decode(secretKey);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
