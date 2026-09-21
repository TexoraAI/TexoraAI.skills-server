package com.lms.user.kafka;
import java.util.List;
import java.time.LocalDate;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.user.event.AuthEvent;
import com.lms.user.model.StudentProfile;
import com.lms.user.model.TrainerProfile;
import com.lms.user.model.User;
import com.lms.user.repo.StudentProfileRepository;
import com.lms.user.repo.TrainerProfileRepository;
import com.lms.user.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.cache.CacheManager;

// WHY: Mirrors auth-service user records into user-service DB for profile enrichment and org queries
@Service
public class AuthEventConsumer {

    // OPTIMIZATION: Replaced System.err.println with SLF4J — errors now captured in log aggregation
    private static final Logger log = LoggerFactory.getLogger(AuthEventConsumer.class);

    private final UserRepository userRepository;
    private final StudentProfileRepository studentProfileRepo;
    private final TrainerProfileRepository trainerProfileRepo;
    private final ObjectMapper objectMapper = new ObjectMapper();
    private final CacheManager cacheManager;
    public AuthEventConsumer(UserRepository userRepository,
                              StudentProfileRepository studentProfileRepo,
                              TrainerProfileRepository trainerProfileRepo,CacheManager cacheManager) {
    	
        this.userRepository = userRepository;
        this.studentProfileRepo = studentProfileRepo;
        this.trainerProfileRepo = trainerProfileRepo;
        this.cacheManager=cacheManager;
    }

    // Small helper so every handler evicts the same way, without repeating the
    // null-check-then-evict boilerplate five times over.
    private void evictUserEmailCache(String email) {
        if (cacheManager.getCache("users:email") != null) {
            cacheManager.getCache("users:email").evict(email);
        }
    }

    // WHY: Kafka consumer — auth-service is source of truth for user creation events
    @KafkaListener(topics = "auth-events", groupId = "user-service-group")
    public void consume(String message) {
        try {
            AuthEvent event = objectMapper.readValue(message, AuthEvent.class);
            switch (event.getEventType()) {
                case "USER_CREATED"      -> handleCreate(event);
                case "USER_DELETED"      -> handleDelete(event);
                case "USER_ROLE_CHANGED" -> handleRoleChange(event);
                case "PROFILE_UPDATED"   -> handleProfileUpdate(event);
                case "USER_PLAN_UPDATED" -> handlePlanUpdate(event);
                case "ORG_UPDATED" -> handleOrgPlanSync(event);
                case "USER_RESUME_PLAN_UPDATED" -> handleResumePlanUpdate(event);
                default -> log.debug("Ignoring unknown auth event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            // OPTIMIZATION: Using log.error so monitoring systems can alert on Kafka processing failures
            log.error("AuthEventConsumer failed to process message: {} | error: {}", message, e.getMessage(), e);
        }
    }

    // WHY: Creates mirror user in user-service DB when student/trainer registers via auth-service
    // OPTIMIZATION: @Transactional ensures if save fails, Kafka consumer can retry without partial write
   
 // WHY: Creates mirror user in user-service DB when student/trainer registers via auth-service
 // OPTIMIZATION: @Transactional ensures if save fails, Kafka consumer can retry without partial write
 // FIX: now uses event.getUserId() as the explicit primary key instead of letting
 // this table auto-generate its own ID. Previously this created ID drift between
 // auth-service (source of truth, ID baked into every JWT) and user-service's
 // local mirror, which silently broke every feature here that looks users up by
 // numeric ID (resume AI usage, template access, etc.) instead of by email.
 @Transactional
 public void handleCreate(AuthEvent event) {
     if (event.getUserId() == null) {
         log.error("USER_CREATED event missing userId — cannot create mirror row → {}", event.getEmail());
         return;
     }

     // WHY: Idempotent check — Kafka at-least-once delivery means this can be called twice.
     // Now keyed by id (the canonical identity) instead of email.
     if (userRepository.existsById(event.getUserId())) {
         log.warn("USER-SERVICE: user already exists (id={}), skipping → {}", event.getUserId(), event.getEmail());
         return;
     }

     // WHY: Guard against re-drifting — if this email is already mirrored under
     // a DIFFERENT id than the event carries, something upstream is wrong.
     // Don't silently insert a duplicate row for the same person; surface it.
     userRepository.findByEmail(event.getEmail()).ifPresentOrElse(existing -> {
         log.error("USER-SERVICE: email {} already mirrored under id={} but USER_CREATED event carries id={} — ID DRIFT, needs manual reconciliation",
                 event.getEmail(), existing.getId(), event.getUserId());
     }, () -> {
         User user = new User();
         user.setId(event.getUserId());
         user.setEmail(event.getEmail());
         user.setDisplayName(event.getDisplayName());
         user.setRoles("ROLE_" + event.getRole());
         user.setPlan(event.getPlan() != null ? event.getPlan() : "free");
         if (event.getOrganizationId() != null && !event.getOrganizationId().isBlank()) {
             user.setOrganizationId(event.getOrganizationId());
         }

         userRepository.save(user);
         log.info("USER-SERVICE: User created → id={} email={} | org={}",
                 event.getUserId(), event.getEmail(), event.getOrganizationId());
     });
 }
    @Transactional
    public void handleDelete(AuthEvent event) {
        userRepository.findByEmail(event.getEmail()).ifPresent(user -> {
            studentProfileRepo.deleteByUserId(user.getId());
            trainerProfileRepo.deleteByUserId(user.getId());
            userRepository.delete(user);
        });

        // ADD THESE TWO
        if (cacheManager.getCache("trainer:profile") != null) {
            cacheManager.getCache("trainer:profile").evict(event.getEmail());
        }
        if (cacheManager.getCache("student:profile") != null) {
            cacheManager.getCache("student:profile").evict(event.getEmail());
        }
        evictUserEmailCache(event.getEmail());

        log.info("USER-SERVICE: User deleted → {}", event.getEmail());
    }
    // WHY: Propagates role changes from auth-service so user-service queries return correct roles
    @Transactional
    public void handleRoleChange(AuthEvent event) {
        userRepository.findByEmail(event.getEmail())
                .ifPresent(user -> {
                    user.setRoles("ROLE_" + event.getRole());
                    userRepository.save(user);
                });
        evictUserEmailCache(event.getEmail());
        log.info("USER-SERVICE: Role updated → {} | newRole={}", event.getEmail(), event.getRole());
    }

    
 // WHY: Mirrors org-level plan changes onto every user in that org
    @Transactional
    public void handleOrgPlanSync(AuthEvent event) {
        String orgId = event.getOrganizationId();
        if (orgId == null || orgId.isBlank() || event.getPlan() == null) {
            log.debug("ORG_UPDATED missing orgId or plan — skipping org-plan sync");
            return;
        }
        List<User> orgUsers = userRepository.findByOrganizationId(orgId);
        for (User u : orgUsers) {
            u.setOrgPlan(event.getPlan());
        }
        userRepository.saveAll(orgUsers);
        // Every user in this org may have a stale cached response — evict each one.
        for (User u : orgUsers) {
            evictUserEmailCache(u.getEmail());
        }
        log.info("USER-SERVICE: org plan synced -> orgId={} newOrgPlan={} usersUpdated={}",
                orgId, event.getPlan(), orgUsers.size());
    }

    // WHY: Individually purchased resume-plan override, independent of org/general plan
    @Transactional
    public void handleResumePlanUpdate(AuthEvent event) {
        if (event.getPlan() == null) {
            log.warn("USER_RESUME_PLAN_UPDATED received with no plan value → {}", event.getEmail());
            return;
        }
        userRepository.findByEmail(event.getEmail())
                .ifPresent(user -> {
                    user.setResumePlanOverride(event.getPlan());
                    user.setResumePlanOverrideExpiryDate(
                        event.getExpiresAt() != null ? LocalDate.parse(event.getExpiresAt()) : null);
                    userRepository.save(user);
                });
        evictUserEmailCache(event.getEmail());
        log.info("USER-SERVICE: resume plan override updated → {} | newPlan={} | expiresAt={}",
                event.getEmail(), event.getPlan(), event.getExpiresAt());
    }

    // WHY: Propagates plan/subscription tier changes from auth-service to user-service mirror
    @Transactional
    public void handlePlanUpdate(AuthEvent event) {
        if (event.getPlan() == null) {
            log.warn("USER_PLAN_UPDATED received with no plan value → {}", event.getEmail());
            return;
        }
        userRepository.findByEmail(event.getEmail())
                .ifPresent(user -> {
                    user.setPlan(event.getPlan());
                    userRepository.save(user);
                });
        evictUserEmailCache(event.getEmail());
        log.info("USER-SERVICE: Plan updated → {} | newPlan={}",
                event.getEmail(), event.getPlan());
    }
    
    
    
    @Transactional
    public void handleProfileUpdate(AuthEvent event) {
        try {
            java.util.Map<String, Object> data = objectMapper.readValue(
                event.getOrganizationId(), java.util.Map.class);

            User user = userRepository.findByEmail(event.getEmail()).orElse(null);
            if (user == null) {
                log.warn("PROFILE_UPDATED: user not found → {}", event.getEmail());
                return;
            }

            String role = (String) data.getOrDefault("role", "");

            if ("trainer".equalsIgnoreCase(role)) {
                TrainerProfile p = trainerProfileRepo.findByUser_Email(event.getEmail())
                    .orElseGet(() -> {
                        TrainerProfile np = new TrainerProfile();
                        np.setUser(user);
                        return np;
                    });

                if (data.get("linkedinUrl")  != null) p.setLinkedinUrl((String) data.get("linkedinUrl"));
                if (data.get("country")      != null) p.setCountry((String) data.get("country"));
                if (data.get("courseTopic")  != null) p.setCourseTopic((String) data.get("courseTopic"));
                if (data.get("audienceSize") != null) p.setAudienceSize((String) data.get("audienceSize"));
                if (data.get("fullTimeRole") != null) p.setFullTimeRole((String) data.get("fullTimeRole"));

                // ✅ FIX — platforms comes as List<String> from JSON array, not String
                if (data.get("platforms") != null) {
                    Object platRaw = data.get("platforms");
                    if (platRaw instanceof java.util.List) {
                        @SuppressWarnings("unchecked")
                        java.util.List<String> platList = (java.util.List<String>) platRaw;
                        p.setPlatforms(platList);
                    } else if (platRaw instanceof String) {
                        // fallback for any old string-format data
                        p.setPlatforms(java.util.Arrays.asList(((String) platRaw).split(",")));
                    }
                }

                trainerProfileRepo.save(p);

                if (cacheManager.getCache("trainer:profile") != null) {
                    cacheManager.getCache("trainer:profile").evict(event.getEmail());
                }

            } else if ("student".equalsIgnoreCase(role)) {
                StudentProfile p = studentProfileRepo.findByUser_Email(event.getEmail())
                    .orElseGet(() -> {
                        StudentProfile np = new StudentProfile();
                        np.setUser(user);
                        return np;
                    });

                if (data.get("mobileNumber")  != null) p.setMobileNumber((String) data.get("mobileNumber"));
                if (data.get("dateOfBirth")   != null) p.setDateOfBirth((String) data.get("dateOfBirth"));
                if (data.get("gender")        != null) p.setGender((String) data.get("gender"));
                if (data.get("city")          != null) p.setCity((String) data.get("city"));
                if (data.get("state")         != null) p.setState((String) data.get("state"));
                if (data.get("country")       != null) p.setCountry((String) data.get("country"));
                if (data.get("qualification") != null) p.setQualification((String) data.get("qualification"));
                if (data.get("collegeName")   != null) p.setCollegeName((String) data.get("collegeName"));
                if (data.get("yearOfPassing") != null) p.setYearOfPassing((String) data.get("yearOfPassing"));
                if (data.get("domain")        != null) p.setDomain((String) data.get("domain"));
                if (data.get("experience")    != null) p.setExperience((String) data.get("experience"));

                studentProfileRepo.save(p);

                if (cacheManager.getCache("student:profile") != null) {
                    cacheManager.getCache("student:profile").evict(event.getEmail());
                }
            }

            evictUserEmailCache(event.getEmail());

            log.info("USER-SERVICE: profile synced via PROFILE_UPDATED → {} ({})", event.getEmail(), role);

        } catch (Exception e) {
            log.error("PROFILE_UPDATED handling failed for {}: {}", event.getEmail(), e.getMessage(), e);
        }
    }
}