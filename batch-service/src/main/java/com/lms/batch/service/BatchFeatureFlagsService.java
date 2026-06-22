//
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
//    private final ObjectMapper objectMapper = new ObjectMapper();
//
//    public BatchFeatureFlagsService(BatchFeatureFlagsRepository repo) {
//        this.repo = repo;
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
//        // Both null = SuperAdmin global call, return null (don't throw)
//        return null;
//    }
//
//    public BatchFeatureFlagsDTO getFlags(String organizationId, String email) {
//        String scopeKey = resolveScopeKey(organizationId, email);
//        if (scopeKey == null) return defaultFlags(); // SuperAdmin
//        return repo.findByScopeKey(scopeKey)
//                .map(this::deserialize)
//                .orElseGet(this::defaultFlags);
//    }
//
//    public BatchFeatureFlagsDTO getOrgFlags(String organizationId) {
//        return repo.findByScopeKey(organizationId)
//                .map(this::deserialize)
//                .orElseGet(this::defaultFlags);
//    }
//
//    public BatchFeatureFlagsDTO getIndividualFlags(String email) {
//        String scopeKey = email.trim().toLowerCase();
//        return repo.findByScopeKey(scopeKey)
//                .map(this::deserialize)
//                .orElseGet(this::defaultFlags);
//    }
//
//    public BatchFeatureFlagsDTO updateOrgFlags(String organizationId, BatchFeatureFlagsDTO dto) {
//        return save(organizationId, dto);
//    }
//
//    public BatchFeatureFlagsDTO updateIndividualFlags(String email, BatchFeatureFlagsDTO dto) {
//        return save(email.trim().toLowerCase(), dto);
//    }
//
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
//        // SuperAdmin global call — always enabled
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
//        // SuperAdmin (both null) — never block
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
}