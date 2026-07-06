//package com.lms.file.security;
//
//import io.jsonwebtoken.Claims;
//import io.jsonwebtoken.Jwts;
//import io.jsonwebtoken.security.Keys;
//import org.springframework.stereotype.Component;
//
//import java.nio.charset.StandardCharsets;
//import java.security.Key;
//
//@Component
//public class JwtUtil {
//
//    // 🔑 MUST MATCH AUTH SERVICE SECRET
//    private static final String SECRET = "mysupersecretkeymysupersecretkey";
//
//    private final Key key =
//            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));
//
//    public String extractEmail(String token) {
//        return getClaims(token).getSubject();
//    }
//
//    public boolean validateToken(String token) {
//        try {
//            getClaims(token);
//            return true;
//        } catch (Exception e) {
//            return false;
//        }
//    }
//
//    private Claims getClaims(String token) {
//        return Jwts.parserBuilder()
//                .setSigningKey(key)
//                .build()
//                .parseClaimsJws(token)
//                .getBody();
//    }
//}
package com.lms.file.security;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.stereotype.Component;
import java.nio.charset.StandardCharsets;
import java.security.Key;

@Component
public class JwtUtil {

    // 🔑 MUST MATCH AUTH SERVICE SECRET
    private static final String SECRET = "mysupersecretkeymysupersecretkey";

    private final Key key =
            Keys.hmacShaKeyFor(SECRET.getBytes(StandardCharsets.UTF_8));

    public String extractEmail(String token) {
        return getClaims(token).getSubject();
    }

    // NEW — reads organizationId claim from JWT (already added by Auth Service).
    // Returns null for standalone users, exactly as required.
    public String extractOrganizationId(String token) {
        Object orgId = getClaims(token).get("organizationId");
        return orgId != null ? orgId.toString() : null;
    }

    public boolean validateToken(String token) {
        try {
            getClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    private Claims getClaims(String token) {
        return Jwts.parserBuilder()
                .setSigningKey(key)
                .build()
                .parseClaimsJws(token)
                .getBody();
    }
 // ADD this method
    public String extractRole(String token) {
        Object role = getClaims(token).get("role");
        return role != null ? role.toString() : null;
    }
}