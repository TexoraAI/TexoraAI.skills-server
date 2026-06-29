package com.lms.video.controller;

import com.lms.video.dto.VideoFeatureFlagsDTO;
import com.lms.video.service.VideoFeatureFlagsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video-feature-flags")
public class VideoFeatureFlagsController {

    private final VideoFeatureFlagsService featureFlagsService;

    public VideoFeatureFlagsController(VideoFeatureFlagsService featureFlagsService) {
        this.featureFlagsService = featureFlagsService;
    }

    // ── ORG-SCOPED: GET /api/video-feature-flags/org/{organizationId} ─────────
    // Used by OrganizationDetailsPage -> FeatureControlsTab
    @GetMapping("/org/{organizationId}")
    public ResponseEntity<VideoFeatureFlagsDTO> getOrgFlags(
            @PathVariable String organizationId) {
        return ResponseEntity.ok(featureFlagsService.getOrgFlags(organizationId));
    }

    // ── ORG-SCOPED: PUT /api/video-feature-flags/org/{organizationId} ─────────
    @PutMapping("/org/{organizationId}")
    public ResponseEntity<VideoFeatureFlagsDTO> updateOrgFlags(
            @PathVariable String organizationId,
            @RequestBody VideoFeatureFlagsDTO dto) {
        return ResponseEntity.ok(featureFlagsService.updateOrgFlags(organizationId, dto));
    }

    // ── INDIVIDUAL: GET /api/video-feature-flags/individual?email=... ─────────
    // Used by onboarding dashboard for org-less users
    @GetMapping("/individual")
    public ResponseEntity<VideoFeatureFlagsDTO> getIndividualFlags(
            @RequestParam String email) {
        return ResponseEntity.ok(featureFlagsService.getIndividualFlags(email));
    }

    // ── INDIVIDUAL: PUT /api/video-feature-flags/individual?email=... ─────────
    @PutMapping("/individual")
    public ResponseEntity<VideoFeatureFlagsDTO> updateIndividualFlags(
            @RequestParam String email,
            @RequestBody VideoFeatureFlagsDTO dto) {
        return ResponseEntity.ok(featureFlagsService.updateIndividualFlags(email, dto));
    }
}