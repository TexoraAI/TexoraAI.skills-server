package com.lms.assessment.kafka;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.assessment.event.AuthEvent;
import com.lms.assessment.model.OrgPlanCache;
import com.lms.assessment.model.UserPlanCache;
import com.lms.assessment.repository.OrgPlanCacheRepository;
import com.lms.assessment.repository.UserPlanCacheRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Map;

// WHY: Mirrors org/user plan changes from auth-service into assessment-service's local
// plan cache, so tier resolution (quiz/assignment/coding/study-plan) doesn't need a
// cross-service call on every request.
@Service
public class AuthEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuthEventConsumer.class);

    private final OrgPlanCacheRepository orgPlanCacheRepo;
    private final UserPlanCacheRepository userPlanCacheRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public AuthEventConsumer(OrgPlanCacheRepository orgPlanCacheRepo,
                              UserPlanCacheRepository userPlanCacheRepo) {
        this.orgPlanCacheRepo = orgPlanCacheRepo;
        this.userPlanCacheRepo = userPlanCacheRepo;
    }

    // WHY: assessment-service's Kafka consumer config deserializes values as a generic
    // Map (see application.yml spring.json.value.default.type=java.util.HashMap), so we
    // accept a Map here and convert to AuthEvent, matching the existing consumer style
    // used by BatchAssignmentConsumer in this service.
    @KafkaListener(topics = "auth-events", groupId = "assessment-service-group")
    @Transactional
    public void consume(Map<String, Object> message) {
        try {
            AuthEvent event = objectMapper.convertValue(message, AuthEvent.class);
            switch (event.getEventType()) {
                case "ORG_UPDATED"        -> handleOrgUpdated(event);
                case "USER_PLAN_UPDATED"  -> handleUserPlanUpdated(event);
                default -> log.debug("ASSESSMENT-SERVICE: Ignoring unknown auth event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            log.error("AuthEventConsumer failed to process message: {} | error: {}", message, e.getMessage(), e);
        }
    }

    private void handleOrgUpdated(AuthEvent event) {
        String orgId = event.getOrganizationId();
        if (orgId == null || orgId.isBlank() || event.getPlan() == null) {
            log.debug("ORG_UPDATED missing orgId or plan — skipping org-plan cache update");
            return;
        }

        OrgPlanCache cache = orgPlanCacheRepo.findById(orgId)
                .orElseGet(() -> {
                    OrgPlanCache c = new OrgPlanCache();
                    c.setOrganizationId(orgId);
                    return c;
                });
        cache.setPlan(event.getPlan());
        cache.setUpdatedAt(LocalDateTime.now());
        orgPlanCacheRepo.save(cache);

        log.info("ASSESSMENT-SERVICE: org plan cache updated -> orgId={} plan={}", orgId, event.getPlan());
    }

    private void handleUserPlanUpdated(AuthEvent event) {
        String email = event.getEmail();
        if (email == null || email.isBlank() || event.getPlan() == null) {
            log.debug("USER_PLAN_UPDATED missing email or plan — skipping user-plan cache update");
            return;
        }

        UserPlanCache cache = userPlanCacheRepo.findById(email)
                .orElseGet(() -> {
                    UserPlanCache c = new UserPlanCache();
                    c.setEmail(email);
                    return c;
                });
        cache.setPlan(event.getPlan());
        cache.setOrganizationId(event.getOrganizationId());
        cache.setUpdatedAt(LocalDateTime.now());
        userPlanCacheRepo.save(cache);

        log.info("ASSESSMENT-SERVICE: user plan cache updated -> email={} plan={}", email, event.getPlan());
    }
}