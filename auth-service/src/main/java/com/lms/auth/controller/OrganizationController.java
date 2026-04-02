package com.lms.auth.controller;

import com.lms.auth.dto.CreateOrganizationRequest;
import com.lms.auth.dto.OrganizationResponse;
import com.lms.auth.dto.PublicOrgResponse;
import com.lms.auth.service.OrganizationService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/organizations")
public class OrganizationController {

    private final OrganizationService organizationService;

    public OrganizationController(OrganizationService organizationService) {
        this.organizationService = organizationService;
    }

    // POST /api/organizations
    @PostMapping
    public ResponseEntity<OrganizationResponse> createOrganization(
            @RequestBody CreateOrganizationRequest request) {
        OrganizationResponse response = organizationService.createOrganization(request);
        return ResponseEntity.status(HttpStatus.CREATED).body(response);
    }

    // GET /api/organizations
    @GetMapping
    public ResponseEntity<List<OrganizationResponse>> getAllOrganizations() {
        return ResponseEntity.ok(organizationService.getAllOrganizations());
    }

    // GET /api/organizations/{id}
    @GetMapping("/{id}")
    public ResponseEntity<OrganizationResponse> getOrganizationById(
            @PathVariable UUID id) {
        return ResponseEntity.ok(organizationService.getOrganizationById(id));
    }

    // GET /api/organizations/public  — no auth, student signup dropdown
    @GetMapping("/public")
    public ResponseEntity<List<PublicOrgResponse>> getPublicOrgs() {
        return ResponseEntity.ok(organizationService.getPublicOrgs());
    }

    // PATCH /api/organizations/{id}/status?status=suspended
    @PatchMapping("/{id}/status")
    public ResponseEntity<OrganizationResponse> updateOrgStatus(
            @PathVariable UUID id,
            @RequestParam String status) {
        return ResponseEntity.ok(organizationService.updateOrgStatus(id, status));
    }
 // PUT /api/organizations/{id} — edit org details
    @PutMapping("/{id}")
    public ResponseEntity<OrganizationResponse> updateOrganization(
            @PathVariable UUID id,
            @RequestBody CreateOrganizationRequest request) {
        return ResponseEntity.ok(organizationService.updateOrganization(id, request));
    }

    // DELETE /api/organizations/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteOrganization(@PathVariable UUID id) {
        organizationService.deleteOrganization(id);
        return ResponseEntity.noContent().build();
    }
    
}