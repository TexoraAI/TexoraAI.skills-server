package com.lms.file.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.file.constants.FileFeatureKeys;
import com.lms.file.dto.FileFeatureFlagsDTO;
import com.lms.file.model.FileFeatureFlags;
import com.lms.file.repository.FileFeatureFlagsRepository;

import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
public class FileFeatureFlagsService {

    private final FileFeatureFlagsRepository repo;

    // Inject Spring-managed @Primary ObjectMapper — same as VideoFeatureFlagsService.
    // Plain mapper (no type headers), correct for reading/writing flagsJson.
    private final ObjectMapper objectMapper;

    public FileFeatureFlagsService(FileFeatureFlagsRepository repo,
                                   ObjectMapper objectMapper) {
        this.repo         = repo;
        this.objectMapper = objectMapper;
    }

    // ── Build default DTO with all features enabled ───────────────────────────
    private FileFeatureFlagsDTO defaultFlags() {
        FileFeatureFlagsDTO dto = new FileFeatureFlagsDTO();
        dto.setEnabled(true);
        Map<String, Boolean> features = new HashMap<>();
        for (String key : FileFeatureKeys.ALL_KEYS) {
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
    public FileFeatureFlagsDTO getFlags(String organizationId, String email) {
        String scopeKey = resolveScopeKey(organizationId, email);
        if (scopeKey == null) return defaultFlags();
        return repo.findByScopeKey(scopeKey)
                .map(this::deserialize)
                .orElseGet(this::defaultFlags);
    }

    // ── ORG-SCOPED (used by OrganizationDetailsPage "Feature controls" tab) ───
    // Cached per orgId. Cache key: "cache:feature-flags:file:org::{organizationId}"
    // TTL: 30 min default (from RedisConfig). Safe — only updated via updateOrgFlags.
    @Cacheable(value = "feature-flags:file:org", key = "#organizationId")
    public FileFeatureFlagsDTO getOrgFlags(String organizationId) {
        return repo.findByScopeKey(organizationId)
                .map(this::deserialize)
                .orElseGet(this::defaultFlags);
    }

    // Evict org cache when flags are updated.
    @CacheEvict(value = "feature-flags:file:org", key = "#organizationId")
    public FileFeatureFlagsDTO updateOrgFlags(String organizationId,
                                              FileFeatureFlagsDTO dto) {
        return save(organizationId, dto);
    }

    // ── INDIVIDUAL (email-scoped, org-less users) ─────────────────────────────
    // Cached per email (lowercased).
    @Cacheable(value = "feature-flags:file:user", key = "#email.toLowerCase()")
    public FileFeatureFlagsDTO getIndividualFlags(String email) {
        String scopeKey = email.trim().toLowerCase();
        return repo.findByScopeKey(scopeKey)
                .map(this::deserialize)
                .orElseGet(this::defaultFlags);
    }

    @CacheEvict(value = "feature-flags:file:user", key = "#email.toLowerCase()")
    public FileFeatureFlagsDTO updateIndividualFlags(String email,
                                                     FileFeatureFlagsDTO dto) {
        return save(email.trim().toLowerCase(), dto);
    }

    // ── Upsert helper ─────────────────────────────────────────────────────────
    private FileFeatureFlagsDTO save(String scopeKey, FileFeatureFlagsDTO dto) {
        FileFeatureFlags entity = repo.findByScopeKey(scopeKey)
                .orElseGet(() -> {
                    FileFeatureFlags e = new FileFeatureFlags();
                    e.setScopeKey(scopeKey);
                    return e;
                });
        entity.setFlagsJson(serialize(dto));
        repo.save(entity);
        return dto;
    }

    // ── Feature check (used by controller to guard endpoints) ────────────────
    // Returns true if the feature is enabled for this org or user.
    // If both organizationId and email are blank (e.g. non-org super admin), always allows.
//    public boolean isFeatureEnabled(String organizationId, String email,
//                                    String featureKey) {
//        if ((organizationId == null || organizationId.isBlank())
//                && (email == null || email.isBlank())) {
//            return true;
//        }
//        FileFeatureFlagsDTO dto = getFlags(organizationId, email);
//        if (!dto.isEnabled()) return false;
//        Map<String, Boolean> features = dto.getFeatures();
//        if (features == null) return true;
//        Boolean val = features.get(featureKey);
//        return val == null || val; // missing key → default enabled
//    }
    public boolean isFeatureEnabled(String organizationId, String email,
            String featureKey) {
boolean hasOrg   = organizationId != null && !organizationId.isBlank();
boolean hasEmail = email != null && !email.isBlank();

if (!hasOrg && !hasEmail) {
return true;
}

if (hasOrg) {
// STEP 1 — org admin's master switch always wins
FileFeatureFlagsDTO orgDto = getOrgFlags(organizationId);
if (!isEnabledInDto(orgDto, featureKey)) {
return false;
}

if (hasEmail) {
// STEP 2 — per-user-in-org override, brand-new scope key,
// defaults to enabled until an admin explicitly sets it
FileFeatureFlagsDTO adminUserDto = getAdminUserFlags(organizationId, email);
return isEnabledInDto(adminUserDto, featureKey);
}

return true;
}

// STEP 3 — org-less individual user, existing behavior unchanged
FileFeatureFlagsDTO dto = getIndividualFlags(email);
return isEnabledInDto(dto, featureKey);
}

//Shared by isFeatureEnabled — extracted from the old inline logic, no behavior change
private boolean isEnabledInDto(FileFeatureFlagsDTO dto, String featureKey) {
if (!dto.isEnabled()) return false;
Map<String, Boolean> features = dto.getFeatures();
if (features == null) return true;
Boolean val = features.get(featureKey);
return val == null || val; // missing key → default enabled
}

    // ── Enforce (throws 403 if feature is disabled) ────────────────────────────
    // Call this at the top of controller methods you want to gate.
    // Example:
    //   fileFeatureFlagsService.enforce(organizationId, email, FileFeatureKeys.UPLOAD_FILE);
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
    private FileFeatureFlagsDTO deserialize(FileFeatureFlags entity) {
        try {
            if (entity.getFlagsJson() == null) return defaultFlags();
            return objectMapper.readValue(entity.getFlagsJson(),
                    FileFeatureFlagsDTO.class);
        } catch (Exception e) {
            return defaultFlags();
        }
    }

    private String serialize(FileFeatureFlagsDTO dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize file feature flags", e);
        }
    }
 // ── ADMIN-SCOPED PER-USER-IN-ORG OVERRIDE ─────────────────────────────────
 // Composite scope key = organizationId + "::" + email. This is a brand-new
 // key space — it can never collide with an org-only or email-only row.
 private String resolveAdminUserScopeKey(String organizationId, String email) {
     if (organizationId == null || organizationId.isBlank()
             || email == null || email.isBlank()) {
         return null;
     }
     return organizationId + "::" + email.trim().toLowerCase();
 }

 @Cacheable(value = "feature-flags:file:admin-user",
            key = "#organizationId + '::' + #email.toLowerCase()")
 public FileFeatureFlagsDTO getAdminUserFlags(String organizationId, String email) {
     String scopeKey = resolveAdminUserScopeKey(organizationId, email);
     if (scopeKey == null) return defaultFlags();
     return repo.findByScopeKey(scopeKey)
             .map(this::deserialize)
             .orElseGet(this::defaultFlags);
 }

 @CacheEvict(value = "feature-flags:file:admin-user",
             key = "#organizationId + '::' + #email.toLowerCase()")
 public FileFeatureFlagsDTO updateAdminUserFlags(String organizationId, String email,
                                                 FileFeatureFlagsDTO dto) {
     String scopeKey = resolveAdminUserScopeKey(organizationId, email);
     return save(scopeKey, dto);
   }
}