package com.autowash.autowash_pro.config;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.RequiredArgsConstructor;


@Component
@RequiredArgsConstructor
public class JwtUtil {

    private final JwtProperties jwtProperties;

    private SecretKey getSigningKey() {
        byte[] keyBytes = Decoders.BASE64
            .decode(jwtProperties.getSecret());
        return Keys.hmacShaKeyFor(keyBytes);
    }

    public String generateAccessToken(String phone, String role) {
        return Jwts.builder()
            .subject(phone)
            .claim("role", role)
            .issuedAt(new Date())
            .expiration(new Date(
                System.currentTimeMillis()
                + jwtProperties.getExpiration()))
            .signWith(getSigningKey())
            .compact();
    }

    public String generateRefreshToken(String phone) {
        return Jwts.builder()
            .subject(phone)
            .issuedAt(new Date())
            .expiration(new Date(
                System.currentTimeMillis()
                + jwtProperties.getRefreshExpiration()))
            .signWith(getSigningKey())
            .compact();
    }

    public String extractPhone(String token) {
        return extractClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }

    public boolean isTokenValid(String token) {
        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    private Claims extractClaims(String token) {
        return Jwts.parser()
            .verifyWith(getSigningKey())
            .build()
            .parseSignedClaims(token)
            .getPayload();
    }
}