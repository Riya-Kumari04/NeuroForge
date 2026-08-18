package com.neuroforge.backend.security;

import com.neuroforge.backend.entity.User;
import io.jsonwebtoken.*;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.function.Function;

@Slf4j
@Component
public class JwtUtil {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.access-expiration}")
    private long accessExpiration;

    @Value("${app.jwt.refresh-expiration}")
    private long refreshExpiration;

    // ── Token generation ──────────────────────────────────────────────────────

    public String generateAccessToken(User user) {
        return buildToken(user, accessExpiration, "ACCESS");
    }

    public String generateRefreshToken(User user) {
        return buildToken(user, refreshExpiration, "REFRESH");
    }

    private String buildToken(User user, long expiry, String purpose) {
        return Jwts.builder()
                .subject(user.getEmail())
                .claim("userId", user.getId())
                .claim("name",   user.getName())
                .claim("role",   user.getRole())
                .claim("orgId",  user.getOrganizationId())
                .claim("purpose", purpose)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiry))
                .signWith(signingKey())
                .compact();
    }

    // ── Validation ────────────────────────────────────────────────────────────

    public boolean isTokenValid(String token) {
        try {
            claims(token);
            return !isExpired(token);
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("Invalid JWT: {}", e.getMessage());
            return false;
        }
    }

    // ── Extraction ────────────────────────────────────────────────────────────

    public String extractEmail(String token) {
        return claim(token, Claims::getSubject);
    }

    public String extractRole(String token) {
        return claim(token, c -> c.get("role", String.class));
    }

    public Long extractUserId(String token) {
        return claim(token, c -> c.get("userId", Long.class));
    }

    private <T> T claim(String token, Function<Claims, T> resolver) {
        return resolver.apply(claims(token));
    }

    private Claims claims(String token) {
        return Jwts.parser()
                .verifyWith(signingKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    private boolean isExpired(String token) {
        return claim(token, Claims::getExpiration).before(new Date());
    }

    private SecretKey signingKey() {
        return Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }
}
