package com.lms.progress.security;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
@Component
public class JwtUtil {
    private final SecretKey key;
    public JwtUtil(@Value("${jwt.secret}") String secret) {
        this.key = Keys.hmacShaKeyFor(
                secret.getBytes(StandardCharsets.UTF_8)
        );
    }
    public String extractEmail(String token) {
        return getClaims(token).getSubject();
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
    public String extractRole(String token) {
        return getClaims(token).get("role", String.class);
    }
    public Long extractUserId(String token) {
        return getClaims(token).get("userId", Long.class);
    }
    public String extractOrganizationId(String token) {
        Object orgId = getClaims(token).get("organizationId");
        return orgId != null ? orgId.toString() : null;
    }

    /**
     * Nullable-safe extraction of the organizationId claim. organizationId is a
     * UUID string across the system (consistent with extractOrganizationId()
     * above), not numeric. Unlike extractOrganizationId(), this never throws:
     * if the claim is absent, the token is malformed, or parsing fails for any
     * reason, it returns null rather than propagating an exception. Used
     * throughout RoadmapService to support null-org trainers/students/roadmaps,
     * where "no organizationId claim" is a valid, expected state rather than an
     * error.
     */
    public String extractOrganizationIdOrNull(String token) {
        try {
            Claims claims = getClaims(token);
            Object raw = claims.get("organizationId");
            if (raw == null) {
                return null;
            }
            String asString = raw.toString().trim();
            return asString.isEmpty() ? null : asString;
        } catch (Exception ex) {
            return null;
        }
    }

    /**
     * True for the two org-scoped admin roles that manage a single organization's
     * batches/roadmaps: {@code "ADMIN"} and {@code "TENANT_ADMIN"}.
     */
    public boolean isOrgAdminRole(String role) {
        return "ADMIN".equals(role) || "TENANT_ADMIN".equals(role);
    }

    /**
     * True for {@code "SUPER_ADMIN"}, the cross-org role that also stands in as
     * org-admin for null-org (orgId == null) trainers/students.
     */
    public boolean isSuperAdmin(String role) {
        return "SUPER_ADMIN".equals(role);
    }
}