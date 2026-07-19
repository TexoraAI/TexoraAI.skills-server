package com.lms.assessment.controller;

import com.lms.assessment.dto.AssessmentFeatureFlagsDTO;
import com.lms.assessment.service.AssessmentFeatureFlagsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assessment-feature-flags")
public class AssessmentFeatureFlagsController {

    private final AssessmentFeatureFlagsService featureFlagsService;

    public AssessmentFeatureFlagsController(AssessmentFeatureFlagsService featureFlagsService) {
        this.featureFlagsService = featureFlagsService;
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
}