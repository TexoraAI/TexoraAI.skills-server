package com.lms.course.kafka;

import com.lms.course.model.OrgPlanCache;
import com.lms.course.model.UserPlanCache;
import com.lms.course.repository.OrgPlanCacheRepository;
import com.lms.course.repository.UserPlanCacheRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

// WHY: course-service's Kafka consumer factory (application.yml) is globally
// configured with ErrorHandlingDeserializer -> JsonDeserializer,
// default.type=java.util.HashMap, use.type.headers=false — every listener on
// every topic receives a Map<String,Object>, regardless of what the producer
// sent. auth-service's AuthEventProducer sends plain JSON strings, but that
// doesn't matter: this service's consumer side always coerces to HashMap.
// This mirrors BatchAssignmentConsumer's proven pattern exactly.
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

    @Transactional
    @KafkaListener(topics = "auth-events", groupId = "course-service-group")
    public void consume(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");

            switch (eventType) {
                case "ORG_UPDATED"       -> handleOrgPlanUpdate(event);
                case "USER_PLAN_UPDATED" -> handleUserPlanUpdate(event);
                default -> log.debug("COURSE-SERVICE: ignoring auth event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("COURSE-SERVICE: AuthEventConsumer failed to process event: {} | error: {}",
                    event, e.getMessage(), e);
        }
    }

    private void handleOrgPlanUpdate(Map<String, Object> event) {
        String orgId = (String) event.get("organizationId");
        String plan  = (String) event.get("plan");

        if (orgId == null || orgId.isBlank() || plan == null) {
            log.debug("COURSE-SERVICE: ORG_UPDATED missing orgId or plan — skipping");
            return;
        }

        OrgPlanCache cache = orgPlanCacheRepo.findById(orgId).orElseGet(OrgPlanCache::new);
        cache.setOrganizationId(orgId);
        cache.setPlan(plan);
        cache.setUpdatedAt(LocalDateTime.now());
        orgPlanCacheRepo.save(cache);

        log.info("COURSE-SERVICE: org plan cache updated -> orgId={} plan={}", orgId, plan);
    }

    private void handleUserPlanUpdate(Map<String, Object> event) {
        String email = (String) event.get("email");
        String plan  = (String) event.get("plan");
        Object orgIdRaw = event.get("organizationId");
        String orgId = orgIdRaw != null ? orgIdRaw.toString() : null;

        if (email == null || plan == null) {
            log.debug("COURSE-SERVICE: USER_PLAN_UPDATED missing email or plan — skipping");
            return;
        }

        UserPlanCache cache = userPlanCacheRepo.findById(email).orElseGet(UserPlanCache::new);
        cache.setEmail(email);
        cache.setPlan(plan);
        cache.setOrganizationId(orgId);
        cache.setUpdatedAt(LocalDateTime.now());
        userPlanCacheRepo.save(cache);

        log.info("COURSE-SERVICE: user plan cache updated -> email={} plan={}", email, plan);
    }
}