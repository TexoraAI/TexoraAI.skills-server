


// OPTIMIZATION:
// 1. getAllOrganizations() — replaced per-org countByOrganizationIdAndRole (N+1) with
//    a single @Query that fetches all counts in one DB round-trip using GROUP BY.
// 2. getPublicOrgs() — annotated with @Cacheable for Redis (cache:orgs:public, TTL 10m).
// 3. getOrganizationById() — annotated with @Cacheable (cache:org:{id}, TTL 5m).
// 4. All write methods annotated with @CacheEvict targeting the relevant keys.

package com.lms.auth.service;

import com.lms.auth.dto.AdminOrgUpdateRequest;
import com.lms.auth.dto.CreateOrganizationRequest;
import com.lms.auth.dto.OrganizationResponse;
import com.lms.auth.dto.PublicOrgResponse;
import com.lms.auth.model.Organization;
import com.lms.auth.model.Role;
import com.lms.auth.repository.OrganizationRepository;
import com.lms.auth.repository.UserRepository;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cache.annotation.Caching;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import com.lms.auth.event.AuthEvent;
import com.lms.auth.producer.AuthEventProducer;
import com.lms.auth.model.User;

@Service
public class OrganizationService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final AuthEventProducer authEventProducer;

    public OrganizationService(OrganizationRepository organizationRepository,
            UserRepository userRepository,
            AuthEventProducer authEventProducer) {
        this.organizationRepository = organizationRepository;
        this.userRepository         = userRepository;
        this.authEventProducer      = authEventProducer;
    }

    // OPTIMIZATION: Evict public org list and any cached org entry on create.
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "orgs", key = "'public'"),
    })
    public OrganizationResponse createOrganization(CreateOrganizationRequest req) {
        Organization org = new Organization();
        org.setName(req.getName());
        org.setEmail(req.getEmail());
        org.setCity(req.getCity());
        org.setPhone(req.getPhone());
        org.setPlan(req.getPlan());
        org.setStatus(req.getStatus());
        org.setManagerName(req.getManagerName());
        org.setManagerEmail(req.getManagerEmail());
        org.setMaxStudents(req.getMaxStudents());
        org.setMaxTrainers(req.getMaxTrainers());
        org.setPlanExpiryDate(req.getPlanExpiryDate());
        org.setMaxDepartments(req.getMaxDepartments());
        org.setMaxBranchesPerDept(req.getMaxBranchesPerDept());
        org.setMaxBatchesPerBranch(req.getMaxBatchesPerBranch());
        Organization saved = organizationRepository.save(org);

        authEventProducer.sendEvent(new AuthEvent(
            "ORG_CREATED", null, saved.getEmail(), null,
            saved.getName(), saved.getId().toString(),
            saved.getMaxDepartments(),      // add to AuthEvent
            saved.getMaxBranchesPerDept(),
            saved.getMaxBatchesPerBranch()
        ));

        return mapToResponse(saved);
    }

    // OPTIMIZATION: Evict cached org by id and public list on full update.
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "org", key = "#orgId"),
        @CacheEvict(value = "orgs", key = "'public'")
    })
    public OrganizationResponse updateOrganization(UUID orgId, CreateOrganizationRequest req) {
        Organization org = find(orgId);
        org.setName(req.getName());
        org.setEmail(req.getEmail());
        org.setCity(req.getCity());
        org.setPhone(req.getPhone());
        org.setPlan(req.getPlan());
        org.setStatus(req.getStatus());
        org.setManagerName(req.getManagerName());
        org.setManagerEmail(req.getManagerEmail());
        org.setMaxStudents(req.getMaxStudents());
        org.setMaxTrainers(req.getMaxTrainers());
        org.setPlanExpiryDate(req.getPlanExpiryDate());
        org.setMaxDepartments(req.getMaxDepartments());
        org.setMaxBranchesPerDept(req.getMaxBranchesPerDept());
        org.setMaxBatchesPerBranch(req.getMaxBatchesPerBranch());

        Organization saved = organizationRepository.save(org);

        authEventProducer.sendEvent(new AuthEvent(
            "ORG_UPDATED", null, saved.getEmail(), null,
            saved.getName(), saved.getId().toString(),
            saved.getMaxDepartments(),      // add to AuthEvent
            saved.getMaxBranchesPerDept(),
            saved.getMaxBatchesPerBranch()
        ));

        return mapToResponse(saved);
    }

    // OPTIMIZATION: Evict cached org by id on profile update.
    @Transactional
    @CacheEvict(value = "org", key = "#orgId")
    public OrganizationResponse updateOrgProfile(UUID orgId, AdminOrgUpdateRequest req) {
        Organization org = find(orgId);
        if (req.getOrganizationName() != null) org.setOrganizationName(req.getOrganizationName());
        if (req.getDomain()           != null) org.setDomain(req.getDomain());
        if (req.getContactEmail()     != null) org.setContactEmail(req.getContactEmail());
        if (req.getLocation()         != null) org.setLocation(req.getLocation());
        if (req.getIndustry()         != null) org.setIndustry(req.getIndustry());
        if (req.getDescription()      != null) org.setDescription(req.getDescription());
        if (req.getMobileNumber()     != null) org.setMobileNumber(req.getMobileNumber());
        return mapToResponse(organizationRepository.save(org));
    }

    // OPTIMIZATION: Replaced N+1 pattern (one count query per org) with a single
    // DB call via findAllWithCounts() that uses GROUP BY to aggregate in one round-trip.
    public List<OrganizationResponse> getAllOrganizations() {
        List<Organization> orgs = organizationRepository.findAll();
        Map<UUID, long[]> counts = organizationRepository.findOrgUserCounts();

        return orgs.stream().map(org -> {
            OrganizationResponse res = mapToResponse(org);
            long[] c = counts.getOrDefault(org.getId(), new long[]{0L, 0L});
            res.setCurrentStudents(c[0]);
            res.setCurrentTrainers(c[1]);
            return res;
        }).collect(Collectors.toList());
    }

    // OPTIMIZATION: Cached for 5 minutes. Evicted on any update to this org.
    @Cacheable(value = "org", key = "#orgId")
    public OrganizationResponse getOrganizationById(UUID orgId) {
        return mapToResponseWithCounts(find(orgId));
    }

    // OPTIMIZATION: Cached for 10 minutes (student signup dropdown — rarely changes).
    // Cache name "orgs" with key "public" → Redis key: cache:orgs::public
    @Cacheable(value = "orgs", key = "'public'")
    public List<PublicOrgResponse> getPublicOrgs() {
        return organizationRepository
                .findByStatusOrderByNameAsc("active")
                .stream()
                .map(org -> new PublicOrgResponse(org.getId(), org.getName()))
                .collect(Collectors.toList());
    }

    // OPTIMIZATION: Evict cached org by id and public list on status change.
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "org", key = "#orgId"),
        @CacheEvict(value = "orgs", key = "'public'")
    })
    public OrganizationResponse updateOrgStatus(UUID orgId, String status) {
        Organization org = find(orgId);
        org.setStatus(status);
        return mapToResponse(organizationRepository.save(org));
    }

    // OPTIMIZATION: Evict org cache and public list on delete.
    @Transactional
    @Caching(evict = {
        @CacheEvict(value = "org", key = "#orgId"),
        @CacheEvict(value = "orgs", key = "'public'")
    })
    public void deleteOrganization(UUID orgId) {
        Organization org = find(orgId);
        List<User> orgUsers = userRepository.findByOrganizationId(orgId);

        for (User user : orgUsers) {
            userRepository.delete(user);
            authEventProducer.sendEvent(new AuthEvent(
                "USER_DELETED",
                user.getId(),
                user.getEmail(),
                user.getRole() != null ? user.getRole().name() : null,
                user.getName(),
                orgId.toString()
            ));
        }

        organizationRepository.delete(org);
    }

    // getOrgCapacity — NOT cached (live counts must always be accurate for limit checks)
    public Map<String, Object> getOrgCapacity(UUID orgId) {
        Organization org = find(orgId);
        long currentStudents = userRepository.countByOrganizationIdAndRole(orgId, Role.STUDENT);
        long currentTrainers = userRepository.countByOrganizationIdAndRole(orgId, Role.TRAINER);

        Map<String, Object> result = new HashMap<>();
        result.put("organizationName", org.getOrganizationName());
        result.put("domain",           org.getDomain());
        result.put("contactEmail",     org.getContactEmail());
        result.put("location",         org.getLocation());
        result.put("industry",         org.getIndustry());
        result.put("description",      org.getDescription());
        result.put("mobileNumber",     org.getMobileNumber());
        result.put("plan",             org.getPlan());
        result.put("status",           org.getStatus());
        result.put("planExpiryDate",   org.getPlanExpiryDate());
        result.put("maxStudents",      org.getMaxStudents());
        result.put("maxTrainers",      org.getMaxTrainers());
        result.put("currentStudents",  currentStudents);
        result.put("currentTrainers",  currentTrainers);
        result.put("remainingStudents",
            org.getMaxStudents() != null ? org.getMaxStudents() - currentStudents : null);
        result.put("remainingTrainers",
            org.getMaxTrainers() != null ? org.getMaxTrainers() - currentTrainers : null);
        result.put("maxDepartments",    org.getMaxDepartments());
        result.put("maxBranchesPerDept", org.getMaxBranchesPerDept());
        result.put("maxBatchesPerBranch", org.getMaxBatchesPerBranch());
        return result;
        
     // In getOrgCapacity() — add these 3 lines:
        
    }

    private Organization find(UUID orgId) {
        return organizationRepository.findById(orgId)
                .orElseThrow(() -> new RuntimeException("Organization not found: " + orgId));
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
        res.setMaxStudents(org.getMaxStudents());
        res.setMaxTrainers(org.getMaxTrainers());
        res.setPlanExpiryDate(org.getPlanExpiryDate());
        res.setCreatedAt(org.getCreatedAt());
        res.setUpdatedAt(org.getUpdatedAt());
        res.setOrganizationName(org.getOrganizationName());
        res.setDomain(org.getDomain());
        res.setContactEmail(org.getContactEmail());
        res.setLocation(org.getLocation());
        res.setIndustry(org.getIndustry());
        res.setDescription(org.getDescription());
        res.setMobileNumber(org.getMobileNumber());
        res.setMaxDepartments(org.getMaxDepartments());
        res.setMaxBranchesPerDept(org.getMaxBranchesPerDept());
        res.setMaxBatchesPerBranch(org.getMaxBatchesPerBranch());
        return res;
    }

    private OrganizationResponse mapToResponseWithCounts(Organization org) {
        OrganizationResponse res = mapToResponse(org);
        long students = userRepository.countByOrganizationIdAndRole(org.getId(), Role.STUDENT);
        long trainers = userRepository.countByOrganizationIdAndRole(org.getId(), Role.TRAINER);
        res.setCurrentStudents(students);
        res.setCurrentTrainers(trainers);
        return res;
    }
}
