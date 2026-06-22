package com.lms.batch.controller;

import com.lms.batch.dto.BatchFeatureFlagsDTO;
import com.lms.batch.service.BatchFeatureFlagsService;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feature-flags")
public class BatchFeatureFlagsController {

    private final BatchFeatureFlagsService flagsService;

    public BatchFeatureFlagsController(BatchFeatureFlagsService flagsService) {
        this.flagsService = flagsService;
    }

    // ===== ORG-SCOPED — used by OrganizationDetailsPage "Feature controls" tab =====
    // Toggling here affects ALL trainers/students/admins belonging to this org.

    @GetMapping("/org/{orgId}")
    public BatchFeatureFlagsDTO getOrgFlags(@PathVariable String orgId) {
        return flagsService.getOrgFlags(orgId);
    }

    @PutMapping("/org/{orgId}")
    public BatchFeatureFlagsDTO updateOrgFlags(@PathVariable String orgId,
                                                @RequestBody BatchFeatureFlagsDTO dto) {
        return flagsService.updateOrgFlags(orgId, dto);
    }

    // ===== INDIVIDUAL (email-scoped) — used by OnboardingManagement for org-less users =====
    // Toggling here affects ONLY this specific trainer/student (no organizationId).

    @GetMapping("/individual")
    public BatchFeatureFlagsDTO getIndividualFlags(@RequestParam String email) {
        return flagsService.getIndividualFlags(email);
    }

    @PutMapping("/individual")
    public BatchFeatureFlagsDTO updateIndividualFlags(@RequestParam String email,
                                                       @RequestBody BatchFeatureFlagsDTO dto) {
        return flagsService.updateIndividualFlags(email, dto);
    }
}