package com.lms.attendance.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.attendance.constants.AttendanceFeatureKeys;
import com.lms.attendance.dto.AttendanceFeatureFlagsDTO;
import com.lms.attendance.entity.AttendanceFeatureFlags;
import com.lms.attendance.repository.AttendanceFeatureFlagsRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
public class AttendanceFeatureFlagsService {

    private final AttendanceFeatureFlagsRepository repo;
    private final ObjectMapper objectMapper;
    private static final String USER_SCOPE_DELIMITER = "::";
    public AttendanceFeatureFlagsService(AttendanceFeatureFlagsRepository repo,
                                          ObjectMapper objectMapper) {
        this.repo         = repo;
        this.objectMapper = objectMapper;
    }

    // ── Build default DTO with all features enabled ───────────────────────────
    private AttendanceFeatureFlagsDTO defaultFlags() {
        AttendanceFeatureFlagsDTO dto = new AttendanceFeatureFlagsDTO();
        dto.setEnabled(true);
        Map<String, Boolean> features = new HashMap<>();
        for (String key : AttendanceFeatureKeys.ALL_KEYS) {
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
    public AttendanceFeatureFlagsDTO getFlags(String organizationId, String email) {
        String scopeKey = resolveScopeKey(organizationId, email);
        if (scopeKey == null) return defaultFlags();
        return repo.findByScopeKey(scopeKey)
                .map(this::deserialize)
                .orElseGet(this::defaultFlags);
    }

    // ── ORG-SCOPED ──────────────────────────────────────────────────────────
    // Cache key: "cache:feature-flags:attendance:org::{organizationId}"
    @Cacheable(value = "feature-flags:attendance:org", key = "#organizationId")
    public AttendanceFeatureFlagsDTO getOrgFlags(String organizationId) {
        return repo.findByScopeKey(organizationId)
                .map(this::deserialize)
                .orElseGet(this::defaultFlags);
    }

    @CacheEvict(value = "feature-flags:attendance:org", key = "#organizationId")
    public AttendanceFeatureFlagsDTO updateOrgFlags(String organizationId,
                                                     AttendanceFeatureFlagsDTO dto) {
        return save(organizationId, dto);
    }

    // ── INDIVIDUAL (email-scoped, org-less users) ─────────────────────────────
    @Cacheable(value = "feature-flags:attendance:user", key = "#email.toLowerCase()")
    public AttendanceFeatureFlagsDTO getIndividualFlags(String email) {
        String scopeKey = email.trim().toLowerCase();
        return repo.findByScopeKey(scopeKey)
                .map(this::deserialize)
                .orElseGet(this::defaultFlags);
    }

    @CacheEvict(value = "feature-flags:attendance:user", key = "#email.toLowerCase()")
    public AttendanceFeatureFlagsDTO updateIndividualFlags(String email,
                                                            AttendanceFeatureFlagsDTO dto) {
        return save(email.trim().toLowerCase(), dto);
    }

    // ── Upsert helper ─────────────────────────────────────────────────────────
    private AttendanceFeatureFlagsDTO save(String scopeKey, AttendanceFeatureFlagsDTO dto) {
        AttendanceFeatureFlags entity = repo.findByScopeKey(scopeKey)
                .orElseGet(() -> {
                    AttendanceFeatureFlags e = new AttendanceFeatureFlags();
                    e.setScopeKey(scopeKey);
                    return e;
                });
        entity.setFlagsJson(serialize(dto));
        repo.save(entity);
        return dto;
    }

    // ── Feature check ───────────────────────────────────────────────────────
//    public boolean isFeatureEnabled(String organizationId, String email, String featureKey) {
//        if ((organizationId == null || organizationId.isBlank())
//                && (email == null || email.isBlank())) {
//            return true;
//        }
//        AttendanceFeatureFlagsDTO dto = getFlags(organizationId, email);
//        if (!dto.isEnabled()) return false;
//        Map<String, Boolean> features = dto.getFeatures();
//        if (features == null) return true;
//        Boolean val = features.get(featureKey);
//        return val == null || val; // missing key → default enabled
//    }
    public boolean isFeatureEnabled(String organizationId, String email, String featureKey) {
        boolean hasOrg   = organizationId != null && !organizationId.isBlank();
        boolean hasEmail = email != null && !email.isBlank();

        if (!hasOrg && !hasEmail) {
            return true;
        }

        if (hasOrg) {
            AttendanceFeatureFlagsDTO orgDto = getOrgFlags(organizationId);
            if (!orgDto.isEnabled()) return false;
            Map<String, Boolean> orgFeatures = orgDto.getFeatures();
            if (orgFeatures != null) {
                Boolean orgVal = orgFeatures.get(featureKey);
                if (orgVal != null && !orgVal) return false;
            }
        }

        if (hasOrg && hasEmail) {
            AttendanceFeatureFlagsDTO userDto = getAdminUserFlags(organizationId, email);
            if (!userDto.isEnabled()) return false;
            Map<String, Boolean> userFeatures = userDto.getFeatures();
            if (userFeatures != null) {
                Boolean userVal = userFeatures.get(featureKey);
                if (userVal != null && !userVal) return false;
            }
            return true;
        }

        if (!hasOrg) {
            AttendanceFeatureFlagsDTO dto = getIndividualFlags(email);
            if (!dto.isEnabled()) return false;
            Map<String, Boolean> features = dto.getFeatures();
            if (features == null) return true;
            Boolean val = features.get(featureKey);
            return val == null || val;
        }

        return true;
    }

    // ── Enforce (throws 403 if feature is disabled) ────────────────────────────
    // Example:
    //   attendanceFeatureFlagsService.enforce(organizationId, email, AttendanceFeatureKeys.MARK_ATTENDANCE);
    public void enforce(String organizationId, String email, String featureKey) {
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
    private AttendanceFeatureFlagsDTO deserialize(AttendanceFeatureFlags entity) {
        try {
            if (entity.getFlagsJson() == null) return defaultFlags();
            return objectMapper.readValue(entity.getFlagsJson(), AttendanceFeatureFlagsDTO.class);
        } catch (Exception e) {
            return defaultFlags();
        }
    }

    private String serialize(AttendanceFeatureFlagsDTO dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize attendance feature flags", e);
        }
    }
 // NEW: composite scope key for "this specific user, inside this org"
    private String resolveAdminUserScopeKey(String organizationId, String email) {
        if (organizationId == null || organizationId.isBlank()
                || email == null || email.isBlank()) {
            return null;
        }
        return organizationId + USER_SCOPE_DELIMITER + email.trim().toLowerCase();
    }
    @Cacheable(value = "feature-flags:attendance:admin-user",
            key = "#organizationId + '::' + #email.toLowerCase()")
    public AttendanceFeatureFlagsDTO getAdminUserFlags(String organizationId, String email) {
        String scopeKey = resolveAdminUserScopeKey(organizationId, email);
        if (scopeKey == null) return defaultFlags();
        return repo.findByScopeKey(scopeKey)
                .map(this::deserialize)
                .orElseGet(this::defaultFlags);
    }

    @CacheEvict(value = "feature-flags:attendance:admin-user",
            key = "#organizationId + '::' + #email.toLowerCase()")
    public AttendanceFeatureFlagsDTO updateAdminUserFlags(String organizationId,
                                                           String email,
                                                           AttendanceFeatureFlagsDTO dto) {
        String scopeKey = resolveAdminUserScopeKey(organizationId, email);
        if (scopeKey == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "organizationId and email are both required to set per-user feature flags.");
        }
        return save(scopeKey, dto);
    }
}