package com.lms.chat.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;

import java.security.Key;
import io.jsonwebtoken.security.Keys;

public class JwtUtil {

    private static final String SECRET =
            "mysupersecretkeymysupersecretkey";

    private static final Key KEY = Keys.hmacShaKeyFor(SECRET.getBytes());

    public static Claims extractClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(KEY)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }

    public static String extractEmail(String token) {
        return extractClaims(token).getSubject();
    }

    public static String extractRole(String token) {
        return (String) extractClaims(token).get("role");
    }
}
