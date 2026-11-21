package com.vitalpet.msappointments.service;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;

@Service
public class JwtService {

    private final SecretKey signingKey;

    public JwtService(
            @Value("${JWT_SECRET}") String secret
    ) {
        byte[] keyBytes = Decoders.BASE64.decode(secret);
        this.signingKey = Keys.hmacShaKeyFor(keyBytes);
    }

    public Claims extractClaims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public boolean isTokenValid(String token) {
        if (token == null || token.isBlank()) {
            return false;
        }

        try {
            extractClaims(token);
            return true;
        } catch (JwtException | IllegalArgumentException ex) {
            return false;
        }
    }

    public String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public Long extractUserId(String token) {
        Object userId = extractClaims(token).get("userId");

        if (userId instanceof Number number) {
            return number.longValue();
        }

        throw new IllegalArgumentException(
                "El JWT no contiene un userId valido"
        );
    }

    public String extractRole(String token) {
        return extractClaims(token).get("role", String.class);
    }
}
