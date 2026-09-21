package com.lms.file.kafka;

import com.lms.file.event.AuthEvent;
import com.lms.file.model.OrgPlanCache;
import com.lms.file.model.UserPlanCache;
import com.lms.file.repository.OrgPlanCacheRepository;
import com.lms.file.repository.UserPlanCacheRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Mirrors org- and user-level plan changes from auth-service into
 * file-service's local read caches, so file storage/size/count limits can be
 * resolved without a cross-service call. Uses the existing
 * kafkaListenerContainerFactory bean (JsonMessageConverter) already
 * configured for this service — no new consumer config is created here.
 */
@Component
public class AuthEventConsumer {

    private final OrgPlanCacheRepository orgPlanCacheRepository;
    private final UserPlanCacheRepository userPlanCacheRepository;

    public AuthEventConsumer(OrgPlanCacheRepository orgPlanCacheRepository,
                              UserPlanCacheRepository userPlanCacheRepository) {
        this.orgPlanCacheRepository = orgPlanCacheRepository;
        this.userPlanCacheRepository = userPlanCacheRepository;
    }

    @KafkaListener(topics = "auth-events", groupId = "file-service-group")
    @Transactional
    public void consume(AuthEvent event) {
        if (event == null || event.getEventType() == null) {
            return;
        }

        switch (event.getEventType()) {
            case "ORG_UPDATED" -> {
                if (event.getOrganizationId() == null || event.getPlan() == null) {
                    return;
                }
                OrgPlanCache cache = orgPlanCacheRepository
                        .findById(event.getOrganizationId())
                        .orElse(new OrgPlanCache());
                cache.setOrganizationId(event.getOrganizationId());
                cache.setPlan(event.getPlan());
                orgPlanCacheRepository.save(cache);
                System.out.println("📁 FILE-SERVICE: org plan synced -> orgId="
                        + event.getOrganizationId() + " plan=" + event.getPlan());
            }
            case "USER_PLAN_UPDATED" -> {
                if (event.getEmail() == null || event.getPlan() == null) {
                    return;
                }
                UserPlanCache cache = userPlanCacheRepository
                        .findById(event.getEmail())
                        .orElse(new UserPlanCache());
                cache.setEmail(event.getEmail());
                cache.setPlan(event.getPlan());
                cache.setOrganizationId(null);
                userPlanCacheRepository.save(cache);
                System.out.println("📁 FILE-SERVICE: user plan synced -> email="
                        + event.getEmail() + " plan=" + event.getPlan());
            }
            default -> {
                // ignore all other event types
            }
        }
    }
}
