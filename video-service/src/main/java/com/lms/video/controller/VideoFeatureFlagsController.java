//package com.lms.video.controller;
//
//import com.lms.video.dto.VideoFeatureFlagsDTO;
//import com.lms.video.service.VideoFeatureFlagsService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//@RestController
//@RequestMapping("/api/video-feature-flags")
//public class VideoFeatureFlagsController {
//
//    private final VideoFeatureFlagsService featureFlagsService;
//
//    public VideoFeatureFlagsController(VideoFeatureFlagsService featureFlagsService) {
//        this.featureFlagsService = featureFlagsService;
//    }
//
//    // ── ORG-SCOPED: GET /api/video-feature-flags/org/{organizationId} ─────────
//    // Used by OrganizationDetailsPage -> FeatureControlsTab
//    @GetMapping("/org/{organizationId}")
//    public ResponseEntity<VideoFeatureFlagsDTO> getOrgFlags(
//            @PathVariable String organizationId) {
//        return ResponseEntity.ok(featureFlagsService.getOrgFlags(organizationId));
//    }
//
//    // ── ORG-SCOPED: PUT /api/video-feature-flags/org/{organizationId} ─────────
//    @PutMapping("/org/{organizationId}")
//    public ResponseEntity<VideoFeatureFlagsDTO> updateOrgFlags(
//            @PathVariable String organizationId,
//            @RequestBody VideoFeatureFlagsDTO dto) {
//        return ResponseEntity.ok(featureFlagsService.updateOrgFlags(organizationId, dto));
//    }
//
//    // ── INDIVIDUAL: GET /api/video-feature-flags/individual?email=... ─────────
//    // Used by onboarding dashboard for org-less users
//    @GetMapping("/individual")
//    public ResponseEntity<VideoFeatureFlagsDTO> getIndividualFlags(
//            @RequestParam String email) {
//        return ResponseEntity.ok(featureFlagsService.getIndividualFlags(email));
//    }
//
//    // ── INDIVIDUAL: PUT /api/video-feature-flags/individual?email=... ─────────
//    @PutMapping("/individual")
//    public ResponseEntity<VideoFeatureFlagsDTO> updateIndividualFlags(
//            @RequestParam String email,
//            @RequestBody VideoFeatureFlagsDTO dto) {
//        return ResponseEntity.ok(featureFlagsService.updateIndividualFlags(email, dto));
//    }
//}
package com.lms.video.controller;

import com.lms.video.dto.VideoFeatureFlagsDTO;
import com.lms.video.security.JwtUtil;
import com.lms.video.service.VideoFeatureFlagsService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/video-feature-flags")
public class VideoFeatureFlagsController {

    private final VideoFeatureFlagsService featureFlagsService;
    private final JwtUtil jwtUtil;

    public VideoFeatureFlagsController(VideoFeatureFlagsService featureFlagsService,
                                        JwtUtil jwtUtil) {
        this.featureFlagsService = featureFlagsService;
        this.jwtUtil = jwtUtil;
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

    // ── ADMIN: GET /api/video-feature-flags/admin/user/{email} ────────────────
    // organizationId comes ONLY from the caller's own JWT — never from
    // path/body/params. This is the security boundary that keeps an admin
    // scoped to their own org's users.
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/user/{email}")
    public ResponseEntity<VideoFeatureFlagsDTO> getAdminUserFlags(
            @PathVariable String email,
            HttpServletRequest request) {
        String organizationId = currentOrgId(request);
        return ResponseEntity.ok(featureFlagsService.getAdminUserFlags(organizationId, email));
    }

    // ── ADMIN: PUT /api/video-feature-flags/admin/user/{email} ────────────────
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/user/{email}")
    public ResponseEntity<VideoFeatureFlagsDTO> updateAdminUserFlags(
            @PathVariable String email,
            @RequestBody VideoFeatureFlagsDTO dto,
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