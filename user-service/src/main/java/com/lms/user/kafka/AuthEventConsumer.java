

package com.lms.user.kafka;

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
                default -> log.debug("Ignoring unknown auth event type: {}", event.getEventType());
            }
        } catch (Exception e) {
            // OPTIMIZATION: Using log.error so monitoring systems can alert on Kafka processing failures
            log.error("AuthEventConsumer failed to process message: {} | error: {}", message, e.getMessage(), e);
        }
    }

    // WHY: Creates mirror user in user-service DB when student/trainer registers via auth-service
    // OPTIMIZATION: @Transactional ensures if save fails, Kafka consumer can retry without partial write
    @Transactional
    public void handleCreate(AuthEvent event) {
        // WHY: Idempotent check — Kafka at-least-once delivery means this can be called twice
        if (userRepository.findByEmail(event.getEmail()).isPresent()) {
            log.warn("USER-SERVICE: user already exists, skipping → {}", event.getEmail());
            return;
        }

        User user = new User();
        user.setEmail(event.getEmail());
        user.setDisplayName(event.getDisplayName());
        user.setRoles("ROLE_" + event.getRole());

        if (event.getOrganizationId() != null && !event.getOrganizationId().isBlank()) {
            user.setOrganizationId(event.getOrganizationId());
        }

        userRepository.save(user);
        log.info("USER-SERVICE: User created → {} | org={}", event.getEmail(), event.getOrganizationId());
    }

  
    //@Transactional
//    public void handleDelete(AuthEvent event) {
//
//        userRepository.findByEmail(event.getEmail())
//            .ifPresent(user -> {
//
//                studentProfileRepo.deleteByUserId(user.getId());
//
//                trainerProfileRepo.deleteByUserId(user.getId());
//
//                userRepository.delete(user);
//            });
//
//        log.info("USER-SERVICE: User deleted → {}", event.getEmail());
//    }
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
        log.info("USER-SERVICE: Role updated → {} | newRole={}", event.getEmail(), event.getRole());
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

            log.info("USER-SERVICE: profile synced via PROFILE_UPDATED → {} ({})", event.getEmail(), role);

        } catch (Exception e) {
            log.error("PROFILE_UPDATED handling failed for {}: {}", event.getEmail(), e.getMessage(), e);
        }
    }
}