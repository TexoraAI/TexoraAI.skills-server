package com.lms.live_session.kafka;

import com.lms.live_session.entity.OrgPlanCache;
import com.lms.live_session.entity.UserPlanCache;
import com.lms.live_session.event.AuthEvent;
import com.lms.live_session.repository.OrgPlanCacheRepository;
import com.lms.live_session.repository.UserPlanCacheRepository;

import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

/**
 * Mirrors org/user plan changes from auth-service into local read caches, used
 * by LiveSessionTierResolver for plan-aware limit enforcement.
 *
 * Deserialization style matches BatchAssignmentConsumer: the listener takes a
 * typed event parameter directly, relying on the existing consumer factory's
 * JsonDeserializer (spring.json.use.type.headers=false, default.type=HashMap)
 * plus Spring's method-parameter type inference to bind the payload.
 */
@Service
public class AuthEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuthEventConsumer.class);

    private final OrgPlanCacheRepository orgPlanCacheRepository;
    private final UserPlanCacheRepository userPlanCacheRepository;

    public AuthEventConsumer(OrgPlanCacheRepository orgPlanCacheRepository,
                              UserPlanCacheRepository userPlanCacheRepository) {
        this.orgPlanCacheRepository = orgPlanCacheRepository;
        this.userPlanCacheRepository = userPlanCacheRepository;
    }

    @Transactional
    @KafkaListener(topics = "auth-events", groupId = "live-session-group")
    public void consume(java.util.Map<String, Object> messageMap) {
        try {
            com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
            AuthEvent event = mapper.convertValue(messageMap, AuthEvent.class);

            if (event == null || event.getEventType() == null) {
                log.debug("AuthEventConsumer received null/empty event, ignoring");
                return;
            }

            switch (event.getEventType()) {
                case "ORG_UPDATED" -> handleOrgUpdated(event);
                case "USER_PLAN_UPDATED" -> handleUserPlanUpdated(event);
                default -> log.debug("AuthEventConsumer ignoring event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("AuthEventConsumer failed to process message: {} | error: {}", messageMap, e.getMessage(), e);
        }
    }

    private void handleOrgUpdated(AuthEvent event) {
        String orgId = event.getOrganizationId();
        if (orgId == null || orgId.isBlank() || event.getPlan() == null) {
            log.debug("ORG_UPDATED missing organizationId or plan — skipping org-plan cache update");
            return;
        }

        OrgPlanCache cache = orgPlanCacheRepository.findById(orgId)
                .orElseGet(OrgPlanCache::new);
        cache.setOrganizationId(orgId);
        cache.setPlan(event.getPlan());
        cache.setUpdatedAt(LocalDateTime.now());
        orgPlanCacheRepository.save(cache);

        log.info("LIVE-SESSION-SERVICE: org plan cache updated -> orgId={} plan={}", orgId, event.getPlan());
    }

    private void handleUserPlanUpdated(AuthEvent event) {
        String email = event.getEmail();
        if (email == null || email.isBlank() || event.getPlan() == null) {
            log.debug("USER_PLAN_UPDATED missing email or plan — skipping user-plan cache update");
            return;
        }

        UserPlanCache cache = userPlanCacheRepository.findById(email)
                .orElseGet(UserPlanCache::new);
        cache.setEmail(email);
        cache.setPlan(event.getPlan());
        cache.setOrganizationId(event.getOrganizationId());
        cache.setUpdatedAt(LocalDateTime.now());
        userPlanCacheRepository.save(cache);

        log.info("LIVE-SESSION-SERVICE: user plan cache updated -> email={} plan={}", email, event.getPlan());
    }
}
