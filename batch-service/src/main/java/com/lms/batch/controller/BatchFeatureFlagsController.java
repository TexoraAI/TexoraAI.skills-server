package com.lms.batch.controller;

import com.lms.batch.dto.BatchFeatureFlagsDTO;
import com.lms.batch.service.BatchFeatureFlagsService;
import com.lms.batch.client.UserClient;
import com.lms.batch.dto.UserDTO;
import com.lms.batch.security.JwtUtil;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/feature-flags")
public class BatchFeatureFlagsController {

	private final BatchFeatureFlagsService flagsService;
    private final JwtUtil jwtUtil;
    private final UserClient userClient;

    public BatchFeatureFlagsController(BatchFeatureFlagsService flagsService,
                                        JwtUtil jwtUtil,
                                        UserClient userClient) {
        this.flagsService = flagsService;
        this.jwtUtil = jwtUtil;
        this.userClient = userClient;
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
 // ===== ADMIN-SCOPED — org admin managing ONE user within their own org =====
    @GetMapping("/admin/user/{email}")
    public BatchFeatureFlagsDTO getAdminUserFlags(@PathVariable String email,
                                                   @RequestHeader("Authorization") String authHeader) {
        String adminOrgId = requireAdminOrgId(authHeader);
        enforceSameOrg(adminOrgId, email);
        return flagsService.getAdminUserFlags(adminOrgId, email);
    }

    @PutMapping("/admin/user/{email}")
    public BatchFeatureFlagsDTO updateAdminUserFlags(@PathVariable String email,
                                                      @RequestBody BatchFeatureFlagsDTO dto,
                                                      @RequestHeader("Authorization") String authHeader) {
        String adminOrgId = requireAdminOrgId(authHeader);
        enforceSameOrg(adminOrgId, email);
        return flagsService.updateAdminUserFlags(adminOrgId, email, dto);
    }

    private String requireAdminOrgId(String authHeader) {
        String token = authHeader.replace("Bearer ", "").trim();
        if (!jwtUtil.validateToken(token)) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.UNAUTHORIZED, "Invalid or expired token.");
        }
        String orgId = jwtUtil.extractOrganizationId(token);
        if (orgId == null || orgId.isBlank()) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN,
                "Admin token has no organizationId — cannot scope this action.");
        }
        return orgId;
    }

    private void enforceSameOrg(String adminOrgId, String targetEmail) {
        UserDTO target = userClient.getUserByEmail(targetEmail);
        if (target == null || target.getOrganizationId() == null
                || !adminOrgId.equals(target.getOrganizationId())) {
            throw new org.springframework.web.server.ResponseStatusException(
                org.springframework.http.HttpStatus.FORBIDDEN,
                "User does not belong to your organization.");
        }
    }
}