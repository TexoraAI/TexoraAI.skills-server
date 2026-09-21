package com.lms.auth.controller;

import com.lms.auth.dto.IndividualUpgradePreviewResponse;
import com.lms.auth.dto.ResumePlanPreviewResponse;
import com.lms.auth.dto.UpgradePreviewResponse;
import com.lms.auth.security.JwtUtil;
import com.lms.auth.service.PlanUpgradeService;
import io.jsonwebtoken.Claims;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Objects;
import java.util.UUID;

@RestController
public class PlanUpgradeController {

    private final PlanUpgradeService planUpgradeService;
    private final JwtUtil jwtUtil;

    public PlanUpgradeController(PlanUpgradeService planUpgradeService, JwtUtil jwtUtil) {
        this.planUpgradeService = planUpgradeService;
        this.jwtUtil = jwtUtil;
    }

    // ── Helper: pull claims off the Authorization header. Returns null if
    // there's no/invalid token — callers decide what to do with that. This
    // is intentionally NOT a shared class yet, per your note — just a
    // private method on this controller for now. ──
    private Claims currentClaims(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) return null;
        try {
            return jwtUtil.parseClaims(header.substring(7));
        } catch (Exception e) {
            return null;
        }
    }

    private Long claimUserId(Claims claims) {
        Object raw = claims.get("userId");
        return raw != null ? Long.valueOf(raw.toString()) : null;
    }

    private String claimRole(Claims claims) {
        return claims.get("role", String.class);
    }

    private String claimOrganizationId(Claims claims) {
        return claims.get("organizationId", String.class);
    }

    // Part A — org-level plan upgrade preview. Only SUPER_ADMIN, or the
    // ADMIN/TENANT_ADMIN who actually belongs to this org, may preview it.
    @GetMapping("/api/organizations/{orgId}/upgrade/preview")
    public ResponseEntity<UpgradePreviewResponse> previewOrgUpgrade(
            @PathVariable UUID orgId,
            @RequestParam String targetPlan,
            @RequestParam int durationMonths,
            HttpServletRequest request) {

        Claims claims = currentClaims(request);
        if (claims == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String role = claimRole(claims);
        String tokenOrgId = claimOrganizationId(claims);

        boolean isSuperAdmin = "SUPER_ADMIN".equalsIgnoreCase(role);
        boolean isOwnOrgAdmin = ("ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role))
                && tokenOrgId != null
                && tokenOrgId.equals(orgId.toString());

        if (!isSuperAdmin && !isOwnOrgAdmin) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(planUpgradeService.previewUpgrade(orgId, targetPlan, durationMonths));
    }

    // Part B — individual (standalone) plan upgrade preview. A user can only
    // preview their OWN upgrade price; TENANT_ADMIN/SUPER_ADMIN may preview
    // on behalf of anyone.
    @GetMapping("/api/users/{userId}/upgrade/preview")
    public ResponseEntity<IndividualUpgradePreviewResponse> previewIndividualUpgrade(
            @PathVariable Long userId,
            @RequestParam String targetPlan,
            @RequestParam int durationMonths,
            HttpServletRequest request) {

        Claims claims = currentClaims(request);
        if (claims == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String role = claimRole(claims);
        Long tokenUserId = claimUserId(claims);

        boolean isPrivileged = "SUPER_ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role);
        boolean isSelf = Objects.equals(tokenUserId, userId);

        if (!isPrivileged && !isSelf) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(
                planUpgradeService.previewIndividualUpgrade(userId, targetPlan, durationMonths));
    }

    // Part D — resume-plan purchase preview. Same self-or-privileged rule as B.
    @GetMapping("/api/users/{userId}/resume-plan/upgrade/preview")
    public ResponseEntity<ResumePlanPreviewResponse> previewResumeUpgrade(
            @PathVariable Long userId,
            @RequestParam String targetPlan,
            @RequestParam int durationMonths,
            HttpServletRequest request) {

        Claims claims = currentClaims(request);
        if (claims == null) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
        }
        String role = claimRole(claims);
        Long tokenUserId = claimUserId(claims);

        boolean isPrivileged = "SUPER_ADMIN".equalsIgnoreCase(role) || "TENANT_ADMIN".equalsIgnoreCase(role);
        boolean isSelf = Objects.equals(tokenUserId, userId);

        if (!isPrivileged && !isSelf) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
        }
        return ResponseEntity.ok(
                planUpgradeService.previewResumeUpgrade(userId, targetPlan, durationMonths));
    }
}