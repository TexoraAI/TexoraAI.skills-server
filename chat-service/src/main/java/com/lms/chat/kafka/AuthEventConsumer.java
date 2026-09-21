package com.lms.chat.kafka;

import com.lms.chat.entity.OrgPlanCache;
import com.lms.chat.entity.UserPlanCache;
import com.lms.chat.repository.OrgPlanCacheRepository;
import com.lms.chat.repository.UserPlanCacheRepository;
import jakarta.transaction.Transactional;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Map;

// WHY: chat-service's existing KafkaListenerConfig (StringDeserializer key/value +
// JsonMessageConverter, bean "kafkaListenerContainerFactory") converts the raw JSON
// payload into whatever type the @KafkaListener method declares. This mirrors
// BatchAssignmentConsumer's proven pattern exactly, and matches how course-service's
// AuthEventConsumer actually behaves in practice: consume as Map<String, Object> and
// pull fields out manually, rather than binding to a typed AuthEvent.
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
    @KafkaListener(topics = "auth-events", groupId = "chat-service-group")
    public void consume(Map<String, Object> event) {
        try {
            String eventType = (String) event.get("eventType");

            switch (eventType) {
                case "ORG_UPDATED"       -> handleOrgPlanUpdate(event);
                case "USER_PLAN_UPDATED" -> handleUserPlanUpdate(event);
                default -> log.debug("CHAT-SERVICE: ignoring auth event type: {}", eventType);
            }
        } catch (Exception e) {
            log.error("CHAT-SERVICE: AuthEventConsumer failed to process event: {} | error: {}",
                    event, e.getMessage(), e);
        }
    }

    private void handleOrgPlanUpdate(Map<String, Object> event) {
        String orgId = (String) event.get("organizationId");
        String plan  = (String) event.get("plan");

        if (orgId == null || orgId.isBlank() || plan == null) {
            log.debug("CHAT-SERVICE: ORG_UPDATED missing orgId or plan — skipping");
            return;
        }

        OrgPlanCache cache = orgPlanCacheRepo.findById(orgId).orElseGet(OrgPlanCache::new);
        cache.setOrganizationId(orgId);
        cache.setPlan(plan);
        cache.setUpdatedAt(LocalDateTime.now());
        orgPlanCacheRepo.save(cache);

        log.info("CHAT-SERVICE: org plan cache updated -> orgId={} plan={}", orgId, plan);
    }

    private void handleUserPlanUpdate(Map<String, Object> event) {
        String email = (String) event.get("email");
        String plan  = (String) event.get("plan");
        Object orgIdRaw = event.get("organizationId");
        String orgId = orgIdRaw != null ? orgIdRaw.toString() : null;

        if (email == null || plan == null) {
            log.debug("CHAT-SERVICE: USER_PLAN_UPDATED missing email or plan — skipping");
            return;
        }

        UserPlanCache cache = userPlanCacheRepo.findById(email).orElseGet(UserPlanCache::new);
        cache.setEmail(email);
        cache.setPlan(plan);
        cache.setOrganizationId(orgId);
        cache.setUpdatedAt(LocalDateTime.now());
        userPlanCacheRepo.save(cache);

        log.info("CHAT-SERVICE: user plan cache updated -> email={} plan={}", email, plan);
    }
}