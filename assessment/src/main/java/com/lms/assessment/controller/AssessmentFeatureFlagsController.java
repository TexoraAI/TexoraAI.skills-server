package com.lms.assessment.controller;

import com.lms.assessment.dto.AssessmentFeatureFlagsDTO;
import com.lms.assessment.service.AssessmentFeatureFlagsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.lms.assessment.security.JwtUtil;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.security.access.prepost.PreAuthorize;
@RestController
@RequestMapping("/api/assessment-feature-flags")
public class AssessmentFeatureFlagsController {

	private final AssessmentFeatureFlagsService featureFlagsService;
    private final JwtUtil jwtUtil;

    public AssessmentFeatureFlagsController(AssessmentFeatureFlagsService featureFlagsService,
                                             JwtUtil jwtUtil) {
        this.featureFlagsService = featureFlagsService;
        this.jwtUtil = jwtUtil;
    }

    // ── ORG-SCOPED: GET /api/assessment-feature-flags/org/{organizationId} ────
    // Used by OrganizationDetailsPage -> FeatureControlsTab
    @GetMapping("/org/{organizationId}")
    public ResponseEntity<AssessmentFeatureFlagsDTO> getOrgFlags(
            @PathVariable String organizationId) {
        return ResponseEntity.ok(featureFlagsService.getOrgFlags(organizationId));
    }

    // ── ORG-SCOPED: PUT /api/assessment-feature-flags/org/{organizationId} ────
    @PutMapping("/org/{organizationId}")
    public ResponseEntity<AssessmentFeatureFlagsDTO> updateOrgFlags(
            @PathVariable String organizationId,
            @RequestBody AssessmentFeatureFlagsDTO dto) {
        return ResponseEntity.ok(featureFlagsService.updateOrgFlags(organizationId, dto));
    }

    // ── INDIVIDUAL: GET /api/assessment-feature-flags/individual?email=... ────
    // Used by onboarding dashboard for org-less users
    @GetMapping("/individual")
    public ResponseEntity<AssessmentFeatureFlagsDTO> getIndividualFlags(
            @RequestParam String email) {
        return ResponseEntity.ok(featureFlagsService.getIndividualFlags(email));
    }

    // ── INDIVIDUAL: PUT /api/assessment-feature-flags/individual?email=... ────
    @PutMapping("/individual")
    public ResponseEntity<AssessmentFeatureFlagsDTO> updateIndividualFlags(
            @RequestParam String email,
            @RequestBody AssessmentFeatureFlagsDTO dto) {
        return ResponseEntity.ok(featureFlagsService.updateIndividualFlags(email, dto));
    }
 // ── ADMIN: GET /api/assessment-feature-flags/admin/user/{email} ──────────
    // organizationId comes ONLY from the caller's own JWT — never from
    // path/body/params. This is the security boundary that keeps an admin
    // scoped to their own org's users.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/user/{email}")
    public ResponseEntity<AssessmentFeatureFlagsDTO> getAdminUserFlags(
            @PathVariable String email,
            HttpServletRequest request) {
        String organizationId = currentOrgId(request);
        return ResponseEntity.ok(featureFlagsService.getAdminUserFlags(organizationId, email));
    }

    // ── ADMIN: PUT /api/assessment-feature-flags/admin/user/{email} ──────────
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/user/{email}")
    public ResponseEntity<AssessmentFeatureFlagsDTO> updateAdminUserFlags(
            @PathVariable String email,
            @RequestBody AssessmentFeatureFlagsDTO dto,
            HttpServletRequest request) {
        String organizationId = currentOrgId(request);
        return ResponseEntity.ok(featureFlagsService.updateAdminUserFlags(organizationId, email, dto));
    }

    // ── Helper: pull organizationId out of the caller's own JWT ───────────────
    private String currentOrgId(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header == null || !header.startsWith("Bearer ")) {
            return null;
        }
        String token = header.substring(7);
        return jwtUtil.extractOrganizationId(token);
    }
}