package com.lms.notification.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    // MUST be EXACT same secret as auth-service
    private static final String SECRET =
            "mysupersecretkeymysupersecretkey";

    private final SecretKey key =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    // ⭐ FIX: allow clock difference between services (VERY IMPORTANT)
    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(3600) // ← prevents random 401
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public String extractEmail(String token) {
        return extractAllClaims(token).getSubject();
    }

    public String extractRole(String token) {
        return extractAllClaims(token).get("role", String.class);
    }

    public String extractClaim(String token, String claimKey) {
        Object value = extractAllClaims(token).get(claimKey);
        return value == null ? null : value.toString();
    }

    public Long extractUserId(String token) {
        String userId = extractClaim(token, "userId");
        return userId == null ? null : Long.parseLong(userId);
    }

    // ⭐ BETTER VALIDATION
    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            System.out.println("JWT INVALID: " + e.getMessage());
            return false;
        }
    }
}