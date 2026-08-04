



package com.lms.auth.controller;

import com.lms.auth.dto.AdminOrgUpdateRequest;
import com.lms.auth.dto.CreateOrganizationRequest;
import com.lms.auth.dto.OrganizationResponse;
import com.lms.auth.dto.PublicOrgResponse;
import com.lms.auth.service.OrganizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    // Super admin: create
    @PostMapping
    public ResponseEntity<OrganizationResponse> createOrganization(
            @RequestBody CreateOrganizationRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(organizationService.createOrganization(request));
    }

    // Super admin: get all
    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
        return ResponseEntity.ok(organizationService.getAllOrganizations());
    }

    // Super admin + Admin: get single
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getOrganizationById(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.getOrganizationById(id));
    }

    // Public: student signup dropdown
    @GetMapping("/public")
    public ResponseEntity<List<PublicOrgResponse>> getPublicOrgs() {
        return ResponseEntity.ok(organizationService.getPublicOrgs());
    }

    // Super admin: update status
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrganizationResponse> updateOrgStatus(
            @PathVariable UUID id, @RequestParam String status) {
        return ResponseEntity.ok(organizationService.updateOrgStatus(id, status));
    }

    // Super admin: full update
    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> updateOrganization(
            @PathVariable UUID id,
            @RequestBody CreateOrganizationRequest request) {
        return ResponseEntity.ok(organizationService.updateOrganization(id, request));
    }

    // ✅ Admin self-update: only their profile fields, locked fields untouched
    @PatchMapping("/{id}/profile")
    public ResponseEntity<OrganizationResponse> updateOrgProfile(
            @PathVariable UUID id,
            @RequestBody AdminOrgUpdateRequest request) {
        return ResponseEntity.ok(organizationService.updateOrgProfile(id, request));
    }

    // Super admin + Admin: get capacity + full profile
    @GetMapping("/{id}/capacity")
    public ResponseEntity<Map<String, Object>> getOrgCapacity(@PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.getOrgCapacity(id));
    }

    // Super admin: delete
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable UUID id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }
 // Super admin: view admin's org profile by their email (student/trainer excluded — that's User Service's job)
    @GetMapping("/by-admin-email")
    public ResponseEntity<Map<String, Object>> getOrgProfileByAdminEmail(
            @RequestParam String email) {
        return ResponseEntity.ok(organizationService.getOrgProfileByAdminEmail(email));
    }

    // Super admin: update admin's org profile by their email
    @PatchMapping("/by-admin-email/profile")
    public ResponseEntity<OrganizationResponse> updateOrgProfileByAdminEmail(
            @RequestParam String email,
            @RequestBody AdminOrgUpdateRequest request) {
        return ResponseEntity.ok(organizationService.updateOrgProfileByAdminEmail(email, request));
    }
}