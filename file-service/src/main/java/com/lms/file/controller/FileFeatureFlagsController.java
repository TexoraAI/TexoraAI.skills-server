package com.lms.file.controller;

import com.lms.file.dto.FileFeatureFlagsDTO;
import com.lms.file.service.FileFeatureFlagsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import com.lms.file.security.SecurityUtils;
import org.springframework.security.access.prepost.PreAuthorize;
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
 // ── ADMIN: GET /api/file-feature-flags/admin/user/{email} ────────────────
 // organizationId is pulled ONLY from the caller's own JWT (SecurityUtils,
 // same pattern as FileController) — never from the path/body/params.
 // This is what keeps an admin scoped to their own org's users.
 @PreAuthorize("hasRole('ADMIN')")
 @GetMapping("/admin/user/{email}")
 public ResponseEntity<FileFeatureFlagsDTO> getAdminUserFlags(
         @PathVariable String email) {
     String organizationId = SecurityUtils.getCurrentOrganizationId();
     return ResponseEntity.ok(featureFlagsService.getAdminUserFlags(organizationId, email));
 }

 // ── ADMIN: PUT /api/file-feature-flags/admin/user/{email} ────────────────
 @PreAuthorize("hasRole('ADMIN')")
 @PutMapping("/admin/user/{email}")
 public ResponseEntity<FileFeatureFlagsDTO> updateAdminUserFlags(
         @PathVariable String email,
         @RequestBody FileFeatureFlagsDTO dto) {
     String organizationId = SecurityUtils.getCurrentOrganizationId();
     return ResponseEntity.ok(featureFlagsService.updateAdminUserFlags(organizationId, email, dto));
 }
}