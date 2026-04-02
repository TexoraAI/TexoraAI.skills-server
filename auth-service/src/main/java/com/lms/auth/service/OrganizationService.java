package com.lms.auth.service;

import com.lms.auth.dto.CreateOrganizationRequest;
import com.lms.auth.dto.OrganizationResponse;
import com.lms.auth.dto.PublicOrgResponse;
import com.lms.auth.model.Organization;
import com.lms.auth.repository.OrganizationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;

    public OrganizationService(OrganizationRepository organizationRepository) {
        this.organizationRepository = organizationRepository;
    }

    // POST /api/organizations — super admin creates org
    @Transactional
    public OrganizationResponse createOrganization(CreateOrganizationRequest request) {

        Organization org = new Organization();
        org.setName(request.getName());
        org.setEmail(request.getEmail());
        org.setCity(request.getCity());
        org.setPhone(request.getPhone());
        org.setPlan(request.getPlan());
        org.setStatus(request.getStatus());
        org.setManagerName(request.getManagerName());
        org.setManagerEmail(request.getManagerEmail());

        Organization saved = organizationRepository.save(org);
        return mapToResponse(saved);
    }

    // GET /api/organizations — super admin gets all orgs
    public List<OrganizationResponse> getAllOrganizations() {
        return organizationRepository.findAll()
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // GET /api/organizations/public — no auth, student signup dropdown
    public List<PublicOrgResponse> getPublicOrgs() {
        return organizationRepository
                .findByStatusOrderByNameAsc("active")
                .stream()
                .map(org -> new PublicOrgResponse(org.getId(), org.getName()))
                .collect(Collectors.toList());
    }

    // PATCH /api/organizations/{id}/status — super admin suspend/activate
    @Transactional
    public OrganizationResponse updateOrgStatus(UUID orgId, String status) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found: " + orgId));
        org.setStatus(status);
        return mapToResponse(organizationRepository.save(org));
    }

    // GET /api/organizations/{id} — super admin get single org
    public OrganizationResponse getOrganizationById(UUID orgId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found: " + orgId));
        return mapToResponse(org);
    }
 // PUT — update org
    @Transactional
    public OrganizationResponse updateOrganization(UUID orgId, CreateOrganizationRequest request) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found: " + orgId));
        org.setName(request.getName());
        org.setEmail(request.getEmail());
        org.setCity(request.getCity());
        org.setPhone(request.getPhone());
        org.setPlan(request.getPlan());
        org.setStatus(request.getStatus());
        org.setManagerName(request.getManagerName());
        org.setManagerEmail(request.getManagerEmail());
        return mapToResponse(organizationRepository.save(org));
    }

    // DELETE
    @Transactional
    public void deleteOrganization(UUID orgId) {
        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found: " + orgId));
        organizationRepository.delete(org);
    }

    private OrganizationResponse mapToResponse(Organization org) {
        OrganizationResponse res = new OrganizationResponse();
        res.setId(org.getId());
        res.setName(org.getName());
        res.setEmail(org.getEmail());
        res.setCity(org.getCity());
        res.setPhone(org.getPhone());
        res.setPlan(org.getPlan());
        res.setStatus(org.getStatus());
        res.setManagerName(org.getManagerName());
        res.setManagerEmail(org.getManagerEmail());
        res.setCreatedAt(org.getCreatedAt());
        res.setUpdatedAt(org.getUpdatedAt());
        return res;
    }
}