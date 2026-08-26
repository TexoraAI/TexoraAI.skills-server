package com.lms.attendance.controller;

import com.lms.attendance.dto.AttendanceFeatureFlagsDTO;
import com.lms.attendance.service.AttendanceFeatureFlagsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.context.SecurityContextHolder;
@RestController
@RequestMapping("/api/attendance-feature-flags")
public class AttendanceFeatureFlagsController {

    private final AttendanceFeatureFlagsService featureFlagsService;

    public AttendanceFeatureFlagsController(AttendanceFeatureFlagsService featureFlagsService) {
        this.featureFlagsService = featureFlagsService;
    }

    // ── ORG-SCOPED: GET /api/attendance-feature-flags/org/{organizationId} ────
    @GetMapping("/org/{organizationId}")
    public ResponseEntity<AttendanceFeatureFlagsDTO> getOrgFlags(
            @PathVariable String organizationId) {
        return ResponseEntity.ok(featureFlagsService.getOrgFlags(organizationId));
    }

    // ── ORG-SCOPED: PUT /api/attendance-feature-flags/org/{organizationId} ────
    @PutMapping("/org/{organizationId}")
    public ResponseEntity<AttendanceFeatureFlagsDTO> updateOrgFlags(
            @PathVariable String organizationId,
            @RequestBody AttendanceFeatureFlagsDTO dto) {
        return ResponseEntity.ok(featureFlagsService.updateOrgFlags(organizationId, dto));
    }

    // ── INDIVIDUAL: GET /api/attendance-feature-flags/individual?email=... ────
    @GetMapping("/individual")
    public ResponseEntity<AttendanceFeatureFlagsDTO> getIndividualFlags(
            @RequestParam String email) {
        return ResponseEntity.ok(featureFlagsService.getIndividualFlags(email));
    }

    // ── INDIVIDUAL: PUT /api/attendance-feature-flags/individual?email=... ────
    @PutMapping("/individual")
    public ResponseEntity<AttendanceFeatureFlagsDTO> updateIndividualFlags(
            @RequestParam String email,
            @RequestBody AttendanceFeatureFlagsDTO dto) {
        return ResponseEntity.ok(featureFlagsService.updateIndividualFlags(email, dto));
    }
 // GET /api/attendance-feature-flags/admin/user/{email}
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/admin/user/{email}")
    public ResponseEntity<AttendanceFeatureFlagsDTO> getAdminUserFlags(
            @PathVariable String email) {
        String organizationId = resolveCallerOrgId();
        return ResponseEntity.ok(featureFlagsService.getAdminUserFlags(organizationId, email));
    }

    // PUT /api/attendance-feature-flags/admin/user/{email}
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/admin/user/{email}")
    public ResponseEntity<AttendanceFeatureFlagsDTO> updateAdminUserFlags(
            @PathVariable String email,
            @RequestBody AttendanceFeatureFlagsDTO dto) {
        String organizationId = resolveCallerOrgId();
        return ResponseEntity.ok(
                featureFlagsService.updateAdminUserFlags(organizationId, email, dto));
    }

    private String resolveCallerOrgId() {
        var authentication = SecurityContextHolder.getContext().getAuthentication();
        Object details = authentication != null ? authentication.getDetails() : null;
        return details instanceof String ? (String) details : null;
    }
}