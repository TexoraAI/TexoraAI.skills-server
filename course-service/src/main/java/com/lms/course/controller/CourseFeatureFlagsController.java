package com.lms.course.controller;

import com.lms.course.dto.CourseFeatureFlagsDTO;
import com.lms.course.service.CourseFeatureFlagsService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/course-feature-flags")
public class CourseFeatureFlagsController {

    private final CourseFeatureFlagsService featureFlagsService;

    public CourseFeatureFlagsController(CourseFeatureFlagsService featureFlagsService) {
        this.featureFlagsService = featureFlagsService;
    }

    // ── ORG-SCOPED: GET /api/course-feature-flags/org/{organizationId} ────────
    // Used by OrganizationDetailsPage -> FeatureControlsTab -> courseService.getCourseFeatureFlags(orgId)
    @GetMapping("/org/{organizationId}")
    public ResponseEntity<CourseFeatureFlagsDTO> getOrgFlags(
            @PathVariable String organizationId) {
        return ResponseEntity.ok(featureFlagsService.getOrgFlags(organizationId));
    }

    // ── ORG-SCOPED: PUT /api/course-feature-flags/org/{organizationId} ────────
    // Used by courseService.updateCourseFeatureFlags(orgId, dto)
    @PutMapping("/org/{organizationId}")
    public ResponseEntity<CourseFeatureFlagsDTO> updateOrgFlags(
            @PathVariable String organizationId,
            @RequestBody CourseFeatureFlagsDTO dto) {
        return ResponseEntity.ok(featureFlagsService.updateOrgFlags(organizationId, dto));
    }

    // ── INDIVIDUAL: GET /api/course-feature-flags/individual?email=... ────────
    // Used by courseService.getIndividualCourseFeatureFlags(email)
    @GetMapping("/individual")
    public ResponseEntity<CourseFeatureFlagsDTO> getIndividualFlags(
            @RequestParam String email) {
        return ResponseEntity.ok(featureFlagsService.getIndividualFlags(email));
    }

    // ── INDIVIDUAL: PUT /api/course-feature-flags/individual?email=... ────────
    // Used by courseService.updateIndividualCourseFeatureFlags(email, dto)
    @PutMapping("/individual")
    public ResponseEntity<CourseFeatureFlagsDTO> updateIndividualFlags(
            @RequestParam String email,
            @RequestBody CourseFeatureFlagsDTO dto) {
        return ResponseEntity.ok(featureFlagsService.updateIndividualFlags(email, dto));
    }
}