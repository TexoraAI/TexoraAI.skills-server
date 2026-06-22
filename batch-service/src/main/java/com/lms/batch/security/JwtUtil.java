//package com.lms.batch.security;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.stereotype.Component;
//
//import javax.crypto.SecretKey;
//import java.nio.charset.StandardCharsets;
//
//@Component
//public class JwtUtil {
//
//    // MUST be EXACT same secret as auth-service
//    private static final String SECRET =
//            "mysupersecretkeymysupersecretkey";
//
//    private final SecretKey key =
//            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
//
//    // ⭐ FIX: allow clock difference between services (VERY IMPORTANT)
//    public Claims extractAllClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(key)
//                .setAllowedClockSkewSeconds(3600) // ← prevents random 401
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//
//    public String extractEmail(String token) {
//        return extractAllClaims(token).getSubject();
//    }
//
//    public String extractRole(String token) {
//        return extractAllClaims(token).get("role", String.class);
//    }
//
//    public String extractClaim(String token, String claimKey) {
//        Object value = extractAllClaims(token).get(claimKey);
//        return value == null ? null : value.toString();
//    }
//
//    public Long extractUserId(String token) {
//        String userId = extractClaim(token, "userId");
//        return userId == null ? null : Long.parseLong(userId);
//    }
//
//    // ⭐ BETTER VALIDATION
//    public boolean validateToken(String token) {
//        try {
//            extractAllClaims(token);
//            return true;
//        } catch (Exception e) {
//            System.out.println("JWT INVALID: " + e.getMessage());
//            return false;
//        }
//    }
//  
//    public String extractOrganizationId(String token) {
//        Object orgId = extractAllClaims(token).get("organizationId");
//        return orgId != null ? orgId.toString() : null;
//    }
// // ADD this method to your existing JwtUtil class
//
//   
//}
package com.lms.batch.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;

@Component
public class JwtUtil {

    // OPTIMIZATION: Removed hardcoded SECRET constant. Now injected from
    // ${JWT_SECRET} via application.yml — matches auth-service's pattern exactly.
    // Hardcoding this in source meant anyone with repo access had the signing key,
    // and rotating it required a code change + redeploy instead of an env var change.
    @Value("${jwt.secret}")
    private String secret;

    private SecretKey key;

    @jakarta.annotation.PostConstruct
    public void init() {
        this.key = Keys.hmacShaKeyFor(secret.getBytes(StandardCharsets.UTF_8));
    }

    public Claims extractAllClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .setAllowedClockSkewSeconds(3600)
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

    public boolean validateToken(String token) {
        try {
            extractAllClaims(token);
            return true;
        } catch (Exception e) {
            System.out.println("JWT INVALID: " + e.getMessage());
            return false;
        }
    }

    public String extractOrganizationId(String token) {
        Object orgId = extractAllClaims(token).get("organizationId");
        return orgId != null ? orgId.toString() : null;
    }
}