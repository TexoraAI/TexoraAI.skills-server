package com.lms.progress.kafka;

import com.lms.progress.model.OrgPlanCache;
import com.lms.progress.model.UserPlanCache;
import com.lms.progress.repository.OrgPlanCacheRepository;
import com.lms.progress.repository.UserPlanCacheRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

// WHY: Mirrors org/user plan state from auth-service into progress-service for roadmap tier resolution
@Service
public class AuthEventConsumer {

    private static final Logger log = LoggerFactory.getLogger(AuthEventConsumer.class);

    private final OrgPlanCacheRepository orgPlanCacheRepo;
    private final UserPlanCacheRepository userPlanCacheRepo;

    public AuthEventConsumer(OrgPlanCacheRepository orgPlanCacheRepo,
                              UserPlanCacheRepository userPlanCacheRepo) {
        this.orgPlanCacheRepo = orgPlanCacheRepo;
        this.userPlanCacheRepo = userPlanCacheRepo;
    }

    @KafkaListener(topics = "auth-events", groupId = "progress-service-group")
    @Transactional
    public void consume(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");
            switch (eventType) {
                case "ORG_UPDATED" -> handleOrgUpdated(event);
                case "USER_PLAN_UPDATED" -> handleUserPlanUpdated(event);
                default -> log.debug("PROGRESS-SERVICE: ignoring auth event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("AuthEventConsumer failed to process message: {} | error: {}", event, e.getMessage(), e);
        }
    }

    private void handleOrgUpdated(Map<String, Object> event) {
        String organizationId = (String) event.get("organizationId");
        String plan = (String) event.get("plan");
        if (organizationId == null || organizationId.isBlank() || plan == null) {
            log.debug("ORG_UPDATED missing organizationId or plan — skipping org-plan cache sync");
            return;
        }

        OrgPlanCache cache = orgPlanCacheRepo.findById(organizationId)
                .orElseGet(OrgPlanCache::new);
        cache.setOrganizationId(organizationId);
        cache.setPlan(plan);
        cache.setUpdatedAt(LocalDateTime.now());
        orgPlanCacheRepo.save(cache);

        log.info("PROGRESS-SERVICE: org plan cache synced -> orgId={} plan={}", organizationId, plan);
    }

    private void handleUserPlanUpdated(Map<String, Object> event) {
        String email = (String) event.get("email");
        String plan = (String) event.get("plan");
        String organizationId = (String) event.get("organizationId");
        if (email == null || email.isBlank() || plan == null) {
            log.warn("USER_PLAN_UPDATED missing email or plan — skipping user-plan cache sync");
            return;
        }

        UserPlanCache cache = userPlanCacheRepo.findById(email)
                .orElseGet(UserPlanCache::new);
        cache.setEmail(email);
        cache.setPlan(plan);
        cache.setOrganizationId(organizationId);
        cache.setUpdatedAt(LocalDateTime.now());
        userPlanCacheRepo.save(cache);

        log.info("PROGRESS-SERVICE: user plan cache synced -> email={} plan={}", email, plan);
    }
}