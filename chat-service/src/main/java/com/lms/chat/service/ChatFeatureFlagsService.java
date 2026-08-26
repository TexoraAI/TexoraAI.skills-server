package com.lms.chat.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.chat.constants.ChatFeatureKeys;
import com.lms.chat.dto.ChatFeatureFlagsDTO;
import com.lms.chat.entity.ChatFeatureFlags;
import com.lms.chat.repository.ChatFeatureFlagsRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.HashMap;
import java.util.Map;

@Service
public class ChatFeatureFlagsService {

    private final ChatFeatureFlagsRepository repo;
    private final ObjectMapper objectMapper; // Spring-managed, same as VideoFeatureFlagsService

    public ChatFeatureFlagsService(ChatFeatureFlagsRepository repo,
                                    ObjectMapper objectMapper) {
        this.repo         = repo;
        this.objectMapper = objectMapper;
    }

    // ── Build default DTO with all features enabled ───────────────────────────
    private ChatFeatureFlagsDTO defaultFlags() {
        ChatFeatureFlagsDTO dto = new ChatFeatureFlagsDTO();
        dto.setEnabled(true);
        Map<String, Boolean> features = new HashMap<>();
        for (String key : ChatFeatureKeys.ALL_KEYS) {
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
    public ChatFeatureFlagsDTO getFlags(String organizationId, String email) {
        String scopeKey = resolveScopeKey(organizationId, email);
        if (scopeKey == null) return defaultFlags();
        return repo.findByScopeKey(scopeKey)
                .map(this::deserialize)
                .orElseGet(this::defaultFlags);
    }

    // ── ORG-SCOPED ─────────────────────────────────────────────────────────────
    public ChatFeatureFlagsDTO getOrgFlags(String organizationId) {
        return repo.findByScopeKey(organizationId)
                .map(this::deserialize)
                .orElseGet(this::defaultFlags);
    }

    public ChatFeatureFlagsDTO updateOrgFlags(String organizationId, ChatFeatureFlagsDTO dto) {
        return save(organizationId, dto);
    }

    // ── INDIVIDUAL (email-scoped, org-less users) ─────────────────────────────
    public ChatFeatureFlagsDTO getIndividualFlags(String email) {
        String scopeKey = email.trim().toLowerCase();
        return repo.findByScopeKey(scopeKey)
                .map(this::deserialize)
                .orElseGet(this::defaultFlags);
    }

    public ChatFeatureFlagsDTO updateIndividualFlags(String email, ChatFeatureFlagsDTO dto) {
        return save(email.trim().toLowerCase(), dto);
    }

    // ── Upsert helper ─────────────────────────────────────────────────────────
    private ChatFeatureFlagsDTO save(String scopeKey, ChatFeatureFlagsDTO dto) {
        ChatFeatureFlags entity = repo.findByScopeKey(scopeKey)
                .orElseGet(() -> {
                    ChatFeatureFlags e = new ChatFeatureFlags();
                    e.setScopeKey(scopeKey);
                    return e;
                });
        entity.setFlagsJson(serialize(dto));
        repo.save(entity);
        return dto;
    }

    // ── Feature check ───────────────────────────────────────────────────────────
    // If both organizationId and email are blank (e.g. Super Admin), always allow.
//    public boolean isFeatureEnabled(String organizationId, String email, String featureKey) {
//        if ((organizationId == null || organizationId.isBlank())
//                && (email == null || email.isBlank())) {
//            return true;
//        }
//        ChatFeatureFlagsDTO dto = getFlags(organizationId, email);
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
            // STEP 1 — org admin's master switch always wins
            ChatFeatureFlagsDTO orgDto = getOrgFlags(organizationId);
            if (!isEnabledInDto(orgDto, featureKey)) {
                return false;
            }

            if (hasEmail) {
                // STEP 2 — per-user-in-org override; defaults enabled until set
                ChatFeatureFlagsDTO adminUserDto = getAdminUserFlags(organizationId, email);
                return isEnabledInDto(adminUserDto, featureKey);
            }

            return true;
        }

        // STEP 3 — org-less individual user, existing behavior unchanged
        ChatFeatureFlagsDTO dto = getIndividualFlags(email);
        return isEnabledInDto(dto, featureKey);
    }

    private boolean isEnabledInDto(ChatFeatureFlagsDTO dto, String featureKey) {
        if (!dto.isEnabled()) return false;
        Map<String, Boolean> features = dto.getFeatures();
        if (features == null) return true;
        Boolean val = features.get(featureKey);
        return val == null || val;
    }

    // ── Enforce (throws 403 if feature is disabled) ─────────────────────────────
    // Call at the top of protected controller methods only.
    // Super Admin endpoints must NEVER call this.
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

    // ── Serialization helpers ───────────────────────────────────────────────────
    private ChatFeatureFlagsDTO deserialize(ChatFeatureFlags entity) {
        try {
            if (entity.getFlagsJson() == null) return defaultFlags();
            return objectMapper.readValue(entity.getFlagsJson(), ChatFeatureFlagsDTO.class);
        } catch (Exception e) {
            return defaultFlags();
        }
    }

    private String serialize(ChatFeatureFlagsDTO dto) {
        try {
            return objectMapper.writeValueAsString(dto);
        } catch (Exception e) {
            throw new RuntimeException("Failed to serialize chat feature flags", e);
        }
    }
 // ── ADMIN-SCOPED PER-USER-IN-ORG OVERRIDE ─────────────────────────────────
 // Composite scope key = organizationId + "::" + email. Brand-new key space —
 // can never collide with an org-only or email-only row.
 private String resolveAdminUserScopeKey(String organizationId, String email) {
     if (organizationId == null || organizationId.isBlank()
             || email == null || email.isBlank()) {
         return null;
     }
     return organizationId + "::" + email.trim().toLowerCase();
 }

 public ChatFeatureFlagsDTO getAdminUserFlags(String organizationId, String email) {
     String scopeKey = resolveAdminUserScopeKey(organizationId, email);
     if (scopeKey == null) return defaultFlags();
     return repo.findByScopeKey(scopeKey)
             .map(this::deserialize)
             .orElseGet(this::defaultFlags);
 }

 public ChatFeatureFlagsDTO updateAdminUserFlags(String organizationId, String email,
                                                 ChatFeatureFlagsDTO dto) {
     String scopeKey = resolveAdminUserScopeKey(organizationId, email);
     return save(scopeKey, dto);
 }
}