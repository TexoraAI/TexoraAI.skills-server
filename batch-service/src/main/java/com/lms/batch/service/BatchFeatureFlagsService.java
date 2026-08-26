//
//
//
//
//package com.lms.batch.service;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.lms.batch.constants.BatchFeatureKeys;
//import com.lms.batch.dto.BatchFeatureFlagsDTO;
//import com.lms.batch.entity.BatchFeatureFlags;
//import com.lms.batch.repository.BatchFeatureFlagsRepository;
//
//import org.springframework.cache.annotation.CacheEvict;
//import org.springframework.cache.annotation.Cacheable;
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.util.HashMap;
//import java.util.Map;
//
//@Service
//public class BatchFeatureFlagsService {
//
//    private final BatchFeatureFlagsRepository repo;
//    // OPTIMIZATION: Inject Spring-managed @Primary ObjectMapper instead of creating
//    // a new ObjectMapper() inline. Inline construction bypasses JavaTimeModule registration
//    // and any Spring customizations applied globally.
//    private final ObjectMapper objectMapper;
//
//    public BatchFeatureFlagsService(BatchFeatureFlagsRepository repo,
//                                     ObjectMapper objectMapper) {
//        this.repo         = repo;
//        this.objectMapper = objectMapper;
//    }
//
//    private BatchFeatureFlagsDTO defaultFlags() {
//        BatchFeatureFlagsDTO dto = new BatchFeatureFlagsDTO();
//        dto.setEnabled(true);
//        Map<String, Boolean> features = new HashMap<>();
//        for (String key : BatchFeatureKeys.ALL_KEYS) {
//            features.put(key, true);
//        }
//        dto.setFeatures(features);
//        return dto;
//    }
//
//    private String resolveScopeKey(String organizationId, String email) {
//        if (organizationId != null && !organizationId.isBlank()) {
//            return organizationId;
//        }
//        if (email != null && !email.isBlank()) {
//            return email.trim().toLowerCase();
//        }
//        return null;
//    }
//
//    public BatchFeatureFlagsDTO getFlags(String organizationId, String email) {
//        String scopeKey = resolveScopeKey(organizationId, email);
//        if (scopeKey == null) return defaultFlags();
//        return repo.findByScopeKey(scopeKey)
//                .map(this::deserialize)
//                .orElseGet(this::defaultFlags);
//    }
//
//    // OPTIMIZATION: Cache org-level feature flags.
//    // Flags are read on EVERY API call via enforce() — caching prevents DB hit per request.
//    @Cacheable(value = "feature-flags:org", key = "#organizationId")
//    public BatchFeatureFlagsDTO getOrgFlags(String organizationId) {
//        return repo.findByScopeKey(organizationId)
//                .map(this::deserialize)
//                .orElseGet(this::defaultFlags);
//    }
//
//    // OPTIMIZATION: Cache individual user feature flags.
//    @Cacheable(value = "feature-flags:user", key = "#email.toLowerCase()")
//    public BatchFeatureFlagsDTO getIndividualFlags(String email) {
//        String scopeKey = email.trim().toLowerCase();
//        return repo.findByScopeKey(scopeKey)
//                .map(this::deserialize)
//                .orElseGet(this::defaultFlags);
//    }
//
//    // OPTIMIZATION: Evict org feature flag cache on update.
//    @CacheEvict(value = "feature-flags:org", key = "#organizationId")
//    public BatchFeatureFlagsDTO updateOrgFlags(String organizationId, BatchFeatureFlagsDTO dto) {
//        return save(organizationId, dto);
//    }
//
//    // OPTIMIZATION: Evict individual feature flag cache on update.
//    @CacheEvict(value = "feature-flags:user", key = "#email.toLowerCase()")
//    public BatchFeatureFlagsDTO updateIndividualFlags(String email, BatchFeatureFlagsDTO dto) {
//        return save(email.trim().toLowerCase(), dto);
//    }
//    private String adminScopeKey(String organizationId, String email) {
//        return organizationId.trim() + ":" + email.trim().toLowerCase();
//    }
//
//    @Cacheable(value = "feature-flags:admin-user", key = "#organizationId + ':' + #email.toLowerCase()")
//    public BatchFeatureFlagsDTO getAdminUserFlags(String organizationId, String email) {
//        String scopeKey = adminScopeKey(organizationId, email);
//        return repo.findByScopeKey(scopeKey)
//                .map(this::deserialize)
//                .orElseGet(this::defaultFlags);
//    }
//
//    @CacheEvict(value = "feature-flags:admin-user", key = "#organizationId + ':' + #email.toLowerCase()")
//    public BatchFeatureFlagsDTO updateAdminUserFlags(String organizationId, String email, BatchFeatureFlagsDTO dto) {
//        return save(adminScopeKey(organizationId, email), dto);
//    }
//    private BatchFeatureFlagsDTO save(String scopeKey, BatchFeatureFlagsDTO dto) {
//        BatchFeatureFlags entity = repo.findByScopeKey(scopeKey)
//                .orElseGet(() -> {
//                    BatchFeatureFlags e = new BatchFeatureFlags();
//                    e.setScopeKey(scopeKey);
//                    return e;
//                });
//        entity.setFlagsJson(serialize(dto));
//        repo.save(entity);
//        return dto;
//    }
//
//    public boolean isFeatureEnabled(String organizationId, String email, String featureKey) {
//        if ((organizationId == null || organizationId.isBlank())
//                && (email == null || email.isBlank())) {
//            return true;
//        }
//
//        BatchFeatureFlagsDTO dto = getFlags(organizationId, email);
//        if (!dto.isEnabled()) return false;
//
//        Map<String, Boolean> features = dto.getFeatures();
//        if (features == null) return true;
//
//        Boolean val = features.get(featureKey);
//        return val == null || val;
//    }
//
//    public void enforce(String organizationId, String email, String featureKey) {
//        if ((organizationId == null || organizationId.isBlank())
//                && (email == null || email.isBlank())) {
//            return;
//        }
//
//        if (!isFeatureEnabled(organizationId, email, featureKey)) {
//            String who = (organizationId != null && !organizationId.isBlank())
//                    ? "your organization"
//                    : "your account";
//            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
//                    "Feature '" + featureKey + "' has been disabled for " + who + ".");
//        }
//    }
//
//    private BatchFeatureFlagsDTO deserialize(BatchFeatureFlags entity) {
//        try {
//            if (entity.getFlagsJson() == null) return defaultFlags();
//            return objectMapper.readValue(entity.getFlagsJson(), BatchFeatureFlagsDTO.class);
//        } catch (Exception e) {
//            return defaultFlags();
//        }
//    }
//
//    private String serialize(BatchFeatureFlagsDTO dto) {
//        try {
//            return objectMapper.writeValueAsString(dto);
//        } catch (Exception e) {
//            throw new RuntimeException("Failed to serialize feature flags", e);
//        }
//    }
//    
//}
package com.lms.batch.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.batch.constants.BatchFeatureKeys;
import com.lms.batch.dto.BatchFeatureFlagsDTO;
import com.lms.batch.entity.BatchFeatureFlags;
import com.lms.batch.repository.BatchFeatureFlagsRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
public class BatchFeatureFlagsService {

    private final BatchFeatureFlagsRepository repo;
    // OPTIMIZATION: Inject Spring-managed @Primary ObjectMapper instead of creating
    // a new ObjectMapper() inline. Inline construction bypasses JavaTimeModule registration
    // and any Spring customizations applied globally.
    private final ObjectMapper objectMapper;

    public BatchFeatureFlagsService(BatchFeatureFlagsRepository repo,
                                     ObjectMapper objectMapper) {
        this.repo         = repo;
        this.objectMapper = objectMapper;
    }

    private BatchFeatureFlagsDTO defaultFlags() {
        BatchFeatureFlagsDTO dto = new BatchFeatureFlagsDTO();
        dto.setEnabled(true);
        Map<String, Boolean> features = new HashMap<>();
        for (String key : BatchFeatureKeys.ALL_KEYS) {
            features.put(key, true);
        }
        dto.setFeatures(features);
        return dto;
    }

    private String resolveScopeKey(String organizationId, String email) {
        if (organizationId != null && !organizationId.isBlank()) {
            return organizationId;
        }
        if (email != null && !email.isBlank()) {
            return email.trim().toLowerCase();
        }
        return null;
    }

    public BatchFeatureFlagsDTO getFlags(String organizationId, String email) {
        String scopeKey = resolveScopeKey(organizationId, email);
        if (scopeKey == null) return defaultFlags();
        return repo.findByScopeKey(scopeKey)
                .map(this::deserialize)
                .orElseGet(this::defaultFlags);
    }

    // OPTIMIZATION: Cache org-level feature flags.
    // Flags are read on EVERY API call via enforce() — caching prevents DB hit per request.
    @Cacheable(value = "feature-flags:org", key = "#organizationId")
    public BatchFeatureFlagsDTO getOrgFlags(String organizationId) {
        return repo.findByScopeKey(organizationId)
                .map(this::deserialize)
                .orElseGet(this::defaultFlags);
    }

    // OPTIMIZATION: Cache individual user feature flags.
    @Cacheable(value = "feature-flags:user", key = "#email.toLowerCase()")
    public BatchFeatureFlagsDTO getIndividualFlags(String email) {
        String scopeKey = email.trim().toLowerCase();
        return repo.findByScopeKey(scopeKey)
                .map(this::deserialize)
                .orElseGet(this::defaultFlags);
    }

    // OPTIMIZATION: Evict org feature flag cache on update.
    @CacheEvict(value = "feature-flags:org", key = "#organizationId")
    public BatchFeatureFlagsDTO updateOrgFlags(String organizationId, BatchFeatureFlagsDTO dto) {
        return save(organizationId, dto);
    }

    // OPTIMIZATION: Evict individual feature flag cache on update.
    @CacheEvict(value = "feature-flags:user", key = "#email.toLowerCase()")
    public BatchFeatureFlagsDTO updateIndividualFlags(String email, BatchFeatureFlagsDTO dto) {
        return save(email.trim().toLowerCase(), dto);
    }

    private BatchFeatureFlagsDTO save(String scopeKey, BatchFeatureFlagsDTO dto) {
        BatchFeatureFlags entity = repo.findByScopeKey(scopeKey)
                .orElseGet(() -> {
                    BatchFeatureFlags e = new BatchFeatureFlags();
                    e.setScopeKey(scopeKey);
                    return e;
                });
        entity.setFlagsJson(serialize(dto));
        repo.save(entity);
        return dto;
    }

    public boolean isFeatureEnabled(String organizationId, String email, String featureKey) {
        if ((organizationId == null || organizationId.isBlank())
                && (email == null || email.isBlank())) {
            return true;
        }

        BatchFeatureFlagsDTO dto = getFlags(organizationId, email);
        if (!dto.isEnabled()) return false;

        Map<String, Boolean> features = dto.getFeatures();
        if (features == null) return true;

        Boolean val = features.get(featureKey);
        return val == null || val;
    }

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

    private BatchFeatureFlagsDTO deserialize(BatchFeatureFlags entity) {
        try {
            if (entity.getFlagsJson() == null) return defaultFlags();
            return objectMapper.readValue(entity.getFlagsJson(), BatchFeatureFlagsDTO.class);
        } catch (Exception e) {
            return defaultFlags();
        }
    }

    private String serialize(BatchFeatureFlagsDTO dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize feature flags", e);
        }
    }

    // ══════════════════════════════════════════════════════════════════════
    // ADMIN-SCOPED (org admin managing ONE user in their own org, via
    // Access Control page). Scope key is "orgId:email" — distinct from both
    // org flags ("orgId" alone) and individual flags ("email" alone), so
    // this never collides with either existing row shape.
    // ══════════════════════════════════════════════════════════════════════

    private String adminScopeKey(String organizationId, String email) {
        return organizationId.trim() + ":" + email.trim().toLowerCase();
    }

    // OPTIMIZATION: Cache admin-managed per-user flags, same pattern as
    // getIndividualFlags. Cache key includes orgId so it can't collide with
    // the individual-flags cache entry for the same email.
    @Cacheable(value = "feature-flags:admin-user", key = "#organizationId + ':' + #email.toLowerCase()")
    public BatchFeatureFlagsDTO getAdminUserFlags(String organizationId, String email) {
        String scopeKey = adminScopeKey(organizationId, email);
        return repo.findByScopeKey(scopeKey)
                .map(this::deserialize)
                .orElseGet(this::defaultFlags);
    }

    // OPTIMIZATION: Evict admin-user cache on update.
    @CacheEvict(value = "feature-flags:admin-user", key = "#organizationId + ':' + #email.toLowerCase()")
    public BatchFeatureFlagsDTO updateAdminUserFlags(String organizationId, String email, BatchFeatureFlagsDTO dto) {
        return save(adminScopeKey(organizationId, email), dto);
    }

    // Extracted from isFeatureEnabled so both the org/individual path and
    // the new admin-scoped path share the same enabled+features check.
    private boolean isEnabledInDto(BatchFeatureFlagsDTO dto, String featureKey) {
        if (dto == null || !dto.isEnabled()) return false;
        Map<String, Boolean> features = dto.getFeatures();
        if (features == null) return true;
        Boolean val = features.get(featureKey);
        return val == null || val;
    }

    // Admin per-user override takes priority over org/individual flags.
    // If an admin has explicitly set flags for this user (a row exists at
    // "orgId:email"), that decision is final — checked and returned here,
    // the org-wide flag is never consulted. Only when no admin-scoped row
    // exists does this fall through to the existing enforce() (org → individual),
    // which is what keeps super admin's org-wide toggles working for every
    // user Access Control hasn't individually touched yet.
    public void enforceForUser(String organizationId, String email, String featureKey) {
        if (organizationId != null && !organizationId.isBlank()
                && email != null && !email.isBlank()) {
            String scopeKey = adminScopeKey(organizationId, email);
            if (repo.findByScopeKey(scopeKey).isPresent()) {
                BatchFeatureFlagsDTO adminScoped = getAdminUserFlags(organizationId, email);
                if (!isEnabledInDto(adminScoped, featureKey)) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Feature '" + featureKey + "' has been disabled for your account.");
                }
                return;
            }
        }
        enforce(organizationId, email, featureKey);
    }
}