package com.lms.video.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.video.event.AuthEvent;
import com.lms.video.model.OrgPlanCache;
import com.lms.video.model.UserPlanCache;
import com.lms.video.repository.OrgPlanCacheRepository;
import com.lms.video.repository.UserPlanCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

// WHY: video-service has no User entity — it only mirrors org/user plan into local
// caches so it can resolve video tiers without calling out to user-service per request.
@Component
public class AuthEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuthEventConsumer.class);

    private final OrgPlanCacheRepository orgPlanCacheRepository;
    private final UserPlanCacheRepository userPlanCacheRepository;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthEventConsumer(OrgPlanCacheRepository orgPlanCacheRepository,
                              UserPlanCacheRepository userPlanCacheRepository) {
        this.orgPlanCacheRepository = orgPlanCacheRepository;
        this.userPlanCacheRepository = userPlanCacheRepository;
    }

    // WHY: Kafka consumer — auth-service is source of truth for plan changes.
    // A bad/malformed message must never crash the consumer thread, so the whole
    // method is defensively wrapped in try/catch, matching the pattern used by
    // other services' Kafka consumers in this codebase.
    @KafkaListener(topics = "auth-events", groupId = "video-service-group")
    public void consume(java.util.Map<String, Object> messageMap) {
        try {
            AuthEvent event = objectMapper.convertValue(messageMap, AuthEvent.class);
            switch (event.getEventType()) {
                case "ORG_UPDATED" -> handleOrgPlanSync(event);
                case "USER_PLAN_UPDATED" -> handleUserPlanSync(event);
                default -> log.debug("VIDEO-SERVICE: ignoring unknown auth event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("AuthEventConsumer failed to process message: {} | error: {}", messageMap, e.getMessage(), e);
        }
    }

    @Transactional
    protected void handleOrgPlanSync(AuthEvent event) {
        if (event.getOrganizationId() == null || event.getPlan() == null) {
            log.debug("ORG_UPDATED missing orgId or plan — skipping org-plan sync");
            return;
        }

        OrgPlanCache cache = orgPlanCacheRepository.findById(event.getOrganizationId())
                .orElseGet(() -> {
                    OrgPlanCache c = new OrgPlanCache();
                    c.setOrganizationId(event.getOrganizationId());
                    return c;
                });
        cache.setPlan(event.getPlan());
        orgPlanCacheRepository.save(cache);

        log.info("VIDEO-SERVICE: org plan synced -> orgId={} plan={}", event.getOrganizationId(), event.getPlan());
    }

    @Transactional
    protected void handleUserPlanSync(AuthEvent event) {
        if (event.getEmail() == null || event.getPlan() == null) {
            log.debug("USER_PLAN_UPDATED missing email or plan — skipping user-plan sync");
            return;
        }

        UserPlanCache cache = userPlanCacheRepository.findById(event.getEmail())
                .orElseGet(() -> {
                    UserPlanCache c = new UserPlanCache();
                    c.setEmail(event.getEmail());
                    return c;
                });
        cache.setPlan(event.getPlan());
        // USER_PLAN_UPDATED is always the standalone/individual path — no org tied to it.
        cache.setOrganizationId(null);
        userPlanCacheRepository.save(cache);

        log.info("VIDEO-SERVICE: user plan synced -> email={} plan={}", event.getEmail(), event.getPlan());
    }
}