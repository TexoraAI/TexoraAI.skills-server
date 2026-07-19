package com.lms.assessment.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.assessment.constants.AssessmentFeatureKeys;
import com.lms.assessment.dto.AssessmentFeatureFlagsDTO;
import com.lms.assessment.model.AssessmentFeatureFlags;
import com.lms.assessment.repository.AssessmentFeatureFlagsRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
public class AssessmentFeatureFlagsService {

    private final AssessmentFeatureFlagsRepository repo;

    // Inject Spring-managed @Primary ObjectMapper — same as VideoFeatureFlagsService.
    // Plain mapper (no type headers), correct for reading/writing flagsJson.
    private final ObjectMapper objectMapper;

    public AssessmentFeatureFlagsService(AssessmentFeatureFlagsRepository repo,
                                         ObjectMapper objectMapper) {
        this.repo         = repo;
        this.objectMapper = objectMapper;
    }

    // ── Build default DTO with all features enabled ───────────────────────────
    private AssessmentFeatureFlagsDTO defaultFlags() {
        AssessmentFeatureFlagsDTO dto = new AssessmentFeatureFlagsDTO();
        dto.setEnabled(true);
        Map<String, Boolean> features = new HashMap<>();
        for (String key : AssessmentFeatureKeys.ALL_KEYS) {
            features.put(key, true);
        }
        dto.setFeatures(features);
        return dto;
    }

    // ── Resolve scope key: prefer orgId, fall back to email ──────────────────
    private String resolveScopeKey(String organizationId, String email) {
        if (organizationId != null && !organizationId.isBlank()) {
            return organizationId;
        }
        if (email != null && !email.isBlank()) {
            return email.trim().toLowerCase();
        }
        return null;
    }

    // ── Generic get (used internally by isFeatureEnabled / enforce) ───────────
    public AssessmentFeatureFlagsDTO getFlags(String organizationId, String email) {
        String scopeKey = resolveScopeKey(organizationId, email);
        if (scopeKey == null) return defaultFlags();
        return repo.findByScopeKey(scopeKey)
                .map(this::deserialize)
                .orElseGet(this::defaultFlags);
    }

    // ── ORG-SCOPED (used by OrganizationDetailsPage "Feature controls" tab) ───
 
    public AssessmentFeatureFlagsDTO getOrgFlags(String organizationId) {
        return repo.findByScopeKey(organizationId)
                .map(this::deserialize)
                .orElseGet(this::defaultFlags);
    }

    // Evict org cache when flags are updated.
    @CacheEvict(value = "feature-flags:assessment:org", key = "#organizationId")
    public AssessmentFeatureFlagsDTO updateOrgFlags(String organizationId,
                                                    AssessmentFeatureFlagsDTO dto) {
        return save(organizationId, dto);
    }

    // ── INDIVIDUAL (email-scoped, org-less users) ─────────────────────────────
    // Cached per email (lowercased).
   
    public AssessmentFeatureFlagsDTO getIndividualFlags(String email) {
        String scopeKey = email.trim().toLowerCase();
        return repo.findByScopeKey(scopeKey)
                .map(this::deserialize)
                .orElseGet(this::defaultFlags);
    }

   
    public AssessmentFeatureFlagsDTO updateIndividualFlags(String email,
                                                           AssessmentFeatureFlagsDTO dto) {
        return save(email.trim().toLowerCase(), dto);
    }

    // ── Upsert helper ─────────────────────────────────────────────────────────
    private AssessmentFeatureFlagsDTO save(String scopeKey, AssessmentFeatureFlagsDTO dto) {
        AssessmentFeatureFlags entity = repo.findByScopeKey(scopeKey)
                .orElseGet(() -> {
                    AssessmentFeatureFlags e = new AssessmentFeatureFlags();
                    e.setScopeKey(scopeKey);
                    return e;
                });
        entity.setFlagsJson(serialize(dto));
        repo.save(entity);
        return dto;
    }

    // ── Feature check (used by controllers to guard endpoints) ────────────────
    // Returns true if the feature is enabled for this org or user.
    // If both organizationId and email are blank (e.g. non-org super admin), always allows.
    public boolean isFeatureEnabled(String organizationId, String email,
                                    String featureKey) {
        if ((organizationId == null || organizationId.isBlank())
                && (email == null || email.isBlank())) {
            return true;
        }
        AssessmentFeatureFlagsDTO dto = getFlags(organizationId, email);
        if (!dto.isEnabled()) return false;
        Map<String, Boolean> features = dto.getFeatures();
        if (features == null) return true;
        Boolean val = features.get(featureKey);
        return val == null || val; // missing key → default enabled
    }

    // ── Enforce (throws 403 if feature is disabled) ────────────────────────────
    // Call this at the top of controller methods you want to gate.
    // Example:
    //   assessmentFeatureFlagsService.enforce(organizationId, email, AssessmentFeatureKeys.CREATE_QUIZ);
    public void enforce(String organizationId, String email, String featureKey) {
        // No org + no email → super admin or non-org user → always allow
        if ((organizationId == null || organizationId.isBlank())
                && (email == null || email.isBlank())) {
            return;
        }
        if (!isFeatureEnabled(organizationId, email, featureKey)) {
            String who = (organizationId != null && !organizationId.isBlank())
                    ? "your organization"
                    : "your account";
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Feature '" + featureKey + "' has been disabled for " + who + ".");
        }
    }

    // ── Serialization helpers ─────────────────────────────────────────────────
    private AssessmentFeatureFlagsDTO deserialize(AssessmentFeatureFlags entity) {
        try {
            if (entity.getFlagsJson() == null) return defaultFlags();
            return objectMapper.readValue(entity.getFlagsJson(),
                    AssessmentFeatureFlagsDTO.class);
        } catch (Exception e) {
            return defaultFlags();
        }
    }

    private String serialize(AssessmentFeatureFlagsDTO dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize assessment feature flags", e);
        }
    }
}