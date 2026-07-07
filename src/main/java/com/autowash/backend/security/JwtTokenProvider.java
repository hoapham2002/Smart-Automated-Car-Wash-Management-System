package com.autowash.backend.security;

import com.autowash.backend.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;
import java.util.UUID;

/**
 * Issues and validates JWTs for both access and refresh tokens.
 * Config comes from application.yml (Sprint 0):
 *   jwt.secret / jwt.access-token-expiration / jwt.refresh-token-expiration
 */
@Slf4j
@Component
public class JwtTokenProvider {

    private final SecretKey signingKey;
    private final long accessTokenExpirationMs;
    private final long refreshTokenExpirationMs;

    public JwtTokenProvider(
            @Value("${jwt.secret}") String secret,
            @Value("${jwt.access-token-expiration}") long accessTokenExpirationMs,
            @Value("${jwt.refresh-token-expiration}") long refreshTokenExpirationMs) {
        // HS256 needs a key >= 256 bits; application.yml's JWT_SECRET must be
        // at least 32 chars (see .env.example from Sprint 0 setup instructions).
        this.signingKey = Keys.hmacShaKeyFor(secret.getBytes());
        this.accessTokenExpirationMs = accessTokenExpirationMs;
        this.refreshTokenExpirationMs = refreshTokenExpirationMs;
    }

    public String generateAccessToken(User user) {
        return buildToken(user, accessTokenExpirationMs, "access");
    }

    public String generateRefreshToken(User user) {
        return buildToken(user, refreshTokenExpirationMs, "refresh");
    }

    public long getAccessTokenExpirationMs() {
        return accessTokenExpirationMs;
    }

    public long getRefreshTokenExpirationMs() {
        return refreshTokenExpirationMs;
    }

    private String buildToken(User user, long expirationMs, String tokenType) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + expirationMs);

        return Jwts.builder()
                .subject(user.getId().toString())
                .claim("phone", user.getPhone())
                .claim("role", user.getRole().name())
                .claim("type", tokenType)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(signingKey)
                .compact();
    }

    /** @return true if the token is well-formed, signed correctly, and not expired. */
    public boolean validateToken(String token) {
        try {
            parseClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            log.debug("Invalid JWT: {}", ex.getMessage());
            return false;
        }
    }

    public UUID getUserId(String token) {
        return UUID.fromString(parseClaims(token).getSubject());
    }

    public String getRole(String token) {
        return parseClaims(token).get("role", String.class);
    }

    private Claims parseClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
}
