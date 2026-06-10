package com.hwinterton.inventory_api.security;

import java.nio.charset.StandardCharsets;
import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.hwinterton.inventory_api.model.Role;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * Utility class for creating, reading, and validating JWT authentication tokens.
 *
 * <p>Stores the username, role, and password-change status in the token so protected
 * requests can be identified without storing server-side sessions.</p>
 */
@Slf4j // Lombok: logging feature helper, call replaced need for standard dependencies fields for Slf4j logging
@Component
public class JwtUtil {

    // dependency fields
    private final String jwtSecret;
    private final long jwtExpiration;

    // constructor injection
    public JwtUtil(
            @Value("${jwt.secret}") String jwtSecret,
            @Value("${jwt.expiration}") long jwtExpiration) {
        this.jwtSecret = jwtSecret;
        this.jwtExpiration = jwtExpiration;
    }
    // Method: create hmac signing key from configured secret
    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes(StandardCharsets.UTF_8));
    }
    // Method: create signed token containing username, role, and mustChangePassword with issued and expiration times
    public String generateToken(String username, Role role, boolean mustChangePassword) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + jwtExpiration);

        String token = Jwts.builder()
                .subject(username)
                .claim("role", role.name())
                .claim("mustChangePassword", mustChangePassword)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(getSigningKey())
                .compact();

        log.info("Generated JWT for user: {} with role: {}", username, role);

        return token;
    }
    // Method: parse token claims after signature verification
    private Claims extractAllClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }
    // Method: return username stored in token subject claim
    public String extractUsername(String token) {
        return extractAllClaims(token).getSubject();
    }
    // Method: return mustChangePassword in token subject claim
    public boolean extractMustChangePassword(String token) {
        return extractAllClaims(token).get("mustChangePassword", Boolean.class);
    }
    // Method: return true when token correctly signed and not expired
    public boolean isTokenValid(String token) {
        try {
            Claims claims = extractAllClaims(token);
            return claims.getExpiration().after(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            log.warn("JWT validation failed: {}", e.getMessage());
            return false;
        }
    }
}
