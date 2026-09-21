package com.lms.auth.security;

import com.lms.auth.model.Role;
import com.lms.auth.model.User;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtUtil {

    private static final String SECRET = "mysupersecretkeymysupersecretkey";
    private static final long EXPIRATION_MS = 60 * 60 * 1000; // 1 hour

    private final SecretKey key =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    
    public String generateToken(User user) {
        var builder = Jwts.builder()
                .setSubject(user.getEmail())
                .claim("role", user.getRole().name())
                .claim("userId", user.getId())
                .setIssuedAt(new Date())
                .setExpiration(new Date(System.currentTimeMillis() + EXPIRATION_MS));

        if (user.getOrganizationId() != null) {
            builder.claim("organizationId", user.getOrganizationId().toString());
        }

        return builder.signWith(key, SignatureAlgorithm.HS256).compact();
    }
    // ✅ ADD THIS METHOD
    public String extractEmail(String token) {
        Claims claims = Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
        return claims.getSubject(); // email is stored as subject
    }
    // ❌ You can REMOVE this if not used anymore
    public String generateToken(String email, Role role) {
        throw new UnsupportedOperationException(
            "Use generateToken(User user) instead"
        );
    }
    
 // ✅ ADD — lets controllers pull claims (userId, role, organizationId)
 // straight off the token without a separate filter/context class.
 public Claims parseClaims(String token) {
     return Jwts.parserBuilder()
             .setSigningKey(key)
             .build()
             .parseClaimsJws(token)
             .getBody(); // throws JwtException/ExpiredJwtException if invalid — caller must catch
 }
    
}
