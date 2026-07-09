package com.springboard.auth_service.config;


import com.springboard.auth_service.entity.User;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.function.Function;

@Service
@Getter
@RequiredArgsConstructor
public class JwtService {

    @Value("${app.jwt.secret}")
    private String secret;

    @Value("${app.jwt.expiration}")
    private long ACCESS_TOKEN_EXPIRATION;
    private final long REFRESH_TOKEN_EXPIRATION = 1000L * 60 * 60 * 24 * 7; //7 days

    public String generateToken(User user) {
        return createToken(
                user,
                ACCESS_TOKEN_EXPIRATION,
                "ACCESS"
        );
    }

    public String generateRefreshToken(User user) {
        return createToken(
                user,
                REFRESH_TOKEN_EXPIRATION,
                "REFRESH"
        );
    }

    public String generateSetPasswordToken(User user) {

        return createToken(
                user,
                15 * 60 * 1000,
                "SET_PASSWORD"
        );
    }

    private String createToken(User user, long expiration, String purpose) {

        return Jwts.builder()
                .subject(user.getEmail())
                .claim("id", user.getId())
                .claim("name", user.getName())
                .claim("role", user.getRole())
                .claim("purpose", purpose)
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + expiration))
                .signWith(getSigningKey(), SignatureAlgorithm.HS256)
                .compact();
    }


    public String extractUsername(String token) {

        return extractClaim(token, Claims::getSubject);
    }


    public <T> T extractClaim(String token,
                              Function<Claims, T> resolver) {

        Claims claims = extractAllClaims(token);

        return resolver.apply(claims);
    }


    public boolean isTokenValid(String token, User user) {

        String email = extractUsername(token);

        return email.equals(user.getEmail()) &&
                !isTokenExpired(token);
    }

    public boolean isTokenValid(String token, String expectedPurpose) {

        try {

            Claims claims = extractAllClaims(token);

            return !claims.getExpiration().before(new Date())
                    && expectedPurpose.equals(
                    claims.get("purpose", String.class)
            );

        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }


    private boolean isTokenExpired(String token) {

        return extractExpiration(token)
                .before(new Date());
    }

    private Date extractExpiration(String token) {

        return extractClaim(token, Claims::getExpiration);
    }


    private Claims extractAllClaims(String token) {

        return Jwts.parser()

                .verifyWith(getSigningKey())

                .build()

                .parseSignedClaims(token)

                .getPayload();
    }


    private SecretKey getSigningKey() {

        return Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }

    public String extractPurpose(String token) {

        return extractClaim(
                token,
                claims -> claims.get("purpose", String.class)
        );
    }

    public boolean isSetPasswordToken(String token) {

        return "SET_PASSWORD".equals(
                extractPurpose(token)
        );
    }

    public boolean isAccessToken(String token) {

        return "ACCESS".equals(
                extractPurpose(token)
        );
    }

    public boolean isRefreshToken(String token) {

        return "REFRESH".equals(
                extractPurpose(token)
        );
    }

}