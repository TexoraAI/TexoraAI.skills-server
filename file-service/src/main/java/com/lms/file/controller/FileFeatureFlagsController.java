package com.lms.file.controller;

import com.lms.file.dto.FileFeatureFlagsDTO;
import com.lms.file.service.FileFeatureFlagsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/file-feature-flags")
public class FileFeatureFlagsController {

    private final FileFeatureFlagsService featureFlagsService;

    public FileFeatureFlagsController(FileFeatureFlagsService featureFlagsService) {
        this.featureFlagsService = featureFlagsService;
    }

    // ── ORG-SCOPED: GET /api/file-feature-flags/org/{organizationId} ─────────
    // Used by OrganizationDetailsPage -> FeatureControlsTab
    @GetMapping("/org/{organizationId}")
    public ResponseEntity<FileFeatureFlagsDTO> getOrgFlags(
            @PathVariable String organizationId) {
        return ResponseEntity.ok(featureFlagsService.getOrgFlags(organizationId));
    }

    // ── ORG-SCOPED: PUT /api/file-feature-flags/org/{organizationId} ─────────
    @PutMapping("/org/{organizationId}")
    public ResponseEntity<FileFeatureFlagsDTO> updateOrgFlags(
            @PathVariable String organizationId,
            @RequestBody FileFeatureFlagsDTO dto) {
        return ResponseEntity.ok(featureFlagsService.updateOrgFlags(organizationId, dto));
    }

    // ── INDIVIDUAL: GET /api/file-feature-flags/individual?email=... ─────────
    // Used by onboarding dashboard for org-less users
    @GetMapping("/individual")
    public ResponseEntity<FileFeatureFlagsDTO> getIndividualFlags(
            @RequestParam String email) {
        return ResponseEntity.ok(featureFlagsService.getIndividualFlags(email));
    }

    // ── INDIVIDUAL: PUT /api/file-feature-flags/individual?email=... ─────────
    @PutMapping("/individual")
    public ResponseEntity<FileFeatureFlagsDTO> updateIndividualFlags(
            @RequestParam String email,
            @RequestBody FileFeatureFlagsDTO dto) {
        return ResponseEntity.ok(featureFlagsService.updateIndividualFlags(email, dto));
    }
}