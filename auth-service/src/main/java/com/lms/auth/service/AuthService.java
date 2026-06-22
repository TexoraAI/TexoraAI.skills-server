









// OPTIMIZATION:
// 1. Replaced ConcurrentHashMap<String,String> resetTokens with Redis TTL keys (auth:reset:{token})
//    — tokens now expire after 15 minutes automatically, survive restarts
// 2. GoogleIdTokenVerifier is now initialized once in @PostConstruct, not per-call
//    — previously recreated on every /check-google and /google call (expensive)
// 3. Added @Transactional to register() and authenticateGoogle()
//    — prevents partial state if save succeeds but Kafka/org-create fails
// 4. getOnboardingResponses() replaced findAll() with a filtered @Query + in-DB sort
//    — no longer loads entire users table into heap

package com.lms.auth.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.lms.auth.event.AuthEvent;
import com.lms.auth.producer.AuthEventProducer;
import jakarta.annotation.PostConstruct;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.ResponseEntity;
import java.util.Map;
import java.util.Collections;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.lms.auth.dto.AuthResponse;
import com.lms.auth.dto.ChangePasswordRequest;
import com.lms.auth.dto.OnboardingResponseDTO;
import com.lms.auth.dto.RegisterRequest;
import com.lms.auth.dto.UpdateProfileRequest;
import com.lms.auth.model.EmailVerificationToken;
import com.lms.auth.model.Organization;
import com.lms.auth.model.Role;
import com.lms.auth.model.User;
import com.lms.auth.repository.EmailVerificationTokenRepository;
import com.lms.auth.repository.OrganizationRepository;
import com.lms.auth.repository.UserRepository;
import com.lms.auth.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.UUID;
import java.util.List;

@Service
public class AuthService {

    private final AuthEventProducer authEventProducer;
    private final ObjectMapper objectMapper;
    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;
    private final OrganizationRepository organizationRepository;
    private final SesEmailService sesEmailService;
    // OPTIMIZATION: Injected RedisTemplate to replace ConcurrentHashMap for reset tokens.
    // Keys use auth:* prefix per project Redis rules.
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    // OPTIMIZATION: Google verifier initialized once at startup, not per request.
    private GoogleIdTokenVerifier googleIdTokenVerifier;

    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    public AuthService(UserRepository userRepository,
                       EmailVerificationTokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       JwtUtil jwtUtil,
                       AuthEventProducer authEventProducer,
                       ObjectMapper objectMapper,
                       OrganizationRepository organizationRepository,
                       RedisTemplate<String, Object> redisTemplate,
                       SesEmailService sesEmailService) {
        this.userRepository         = userRepository;
        this.tokenRepository        = tokenRepository;
        this.passwordEncoder        = passwordEncoder;
        this.emailService           = emailService;
        this.jwtUtil                = jwtUtil;
        this.authEventProducer      = authEventProducer;
        this.objectMapper           = objectMapper;
        this.organizationRepository = organizationRepository;
        this.redisTemplate          = redisTemplate;
        this.sesEmailService = sesEmailService;   
    }

    // OPTIMIZATION: Initialize Google verifier once at bean creation.
    // Previously new GoogleIdTokenVerifier was created on every /google and /check-google call.
    @PostConstruct
    public void initGoogleVerifier() {
        try {
            NetHttpTransport transport = GoogleNetHttpTransport.newTrustedTransport();
            this.googleIdTokenVerifier = new GoogleIdTokenVerifier.Builder(transport, JSON_FACTORY)
                    .setAudience(Collections.singletonList(googleClientId))
                    .build();
        } catch (Exception e) {
            throw new RuntimeException("Failed to initialize Google token verifier", e);
        }
    }

    // OPTIMIZATION: Added @Transactional — if Kafka send fails or org lookup throws,
    // the user save is rolled back, preventing orphaned auth users.
    @Transactional
    public void register(RegisterRequest request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            User existing = userRepository.findByEmail(request.getEmail()).get();
            if (existing.isEmailVerified() && !existing.isApproved()) {
                existing.setName(request.getName());
                existing.setRole(request.getRole() != null ? request.getRole() : existing.getRole());
                userRepository.save(existing);
                return;
            }
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(
            request.getPassword() != null
                ? passwordEncoder.encode(request.getPassword())
                : passwordEncoder.encode(UUID.randomUUID().toString())
        );
        user.setRole(request.getRole() != null ? request.getRole() : Role.STUDENT);
        user.setApproved(true);
        user.setEmailVerified(true);

        if (request.getOrganizationId() != null) {
            user.setOrganizationId(UUID.fromString(request.getOrganizationId()));
        }

        if (user.getOrganizationId() != null) {
            Organization org = organizationRepository
                .findById(user.getOrganizationId())
                .orElse(null);

            if (org != null) {
                if (user.getRole() == Role.STUDENT && org.getMaxStudents() != null) {
                    long count = userRepository.countByOrganizationIdAndRole(
                        org.getId(), Role.STUDENT);
                    if (count >= org.getMaxStudents()) {
                        throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Student limit reached. Max allowed: " + org.getMaxStudents());
                    }
                }
                if (user.getRole() == Role.TRAINER && org.getMaxTrainers() != null) {
                    long count = userRepository.countByOrganizationIdAndRole(
                        org.getId(), Role.TRAINER);
                    if (count >= org.getMaxTrainers()) {
                        throw new ResponseStatusException(
                            HttpStatus.FORBIDDEN,
                            "Trainer limit reached. Max allowed: " + org.getMaxTrainers());
                    }
                }
            }
        }

        User savedUser = userRepository.save(user);

        authEventProducer.sendEvent(new AuthEvent(
            "USER_CREATED",
            savedUser.getId(),
            savedUser.getEmail(),
            savedUser.getRole().name(),
            savedUser.getName(),
            savedUser.getOrganizationId() != null
                ? savedUser.getOrganizationId().toString()
                : null
        ));
     // ← ADD THIS ONE LINE
        sesEmailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());
    }

    // ─────────────────────── EMAIL LOGIN ────────────────────────────────────
    // SECURITY SENSITIVE — not cached, not changed structurally
    public AuthResponse authenticate(String email, String password) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED, "Invalid email or password"));
        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(HttpStatus.UNAUTHORIZED, "Invalid email or password");
        }
        if (!user.isEmailVerified()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN, "Please verify your email first");
        }
        if ((user.getRole() == Role.STUDENT || user.getRole() == Role.TRAINER
                || user.getRole() == Role.BUSINESS) && !user.isApproved()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Your application is not approved yet");
        }
        if (user.getOrganizationId() != null) {
            organizationRepository.findById(user.getOrganizationId())
                .ifPresent(org -> {
                    if ("inactive".equalsIgnoreCase(org.getStatus())) {
                        throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                            "Your organization has been deactivated. Please contact support.");
                    }
                });
        }
        if (user.isBlocked()) {
            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                "Your account has been blocked. Please contact support.");
        }
        String token = jwtUtil.generateToken(user);
        AuthResponse response = new AuthResponse(token, user.getEmail(), user.getRole().name());
        response.setOrganizationId(user.getOrganizationId());
        response.setProfileCompleted(user.isProfileCompleted());
        return response;
    }
    

    // ─────────────────────── GOOGLE LOGIN ───────────────────────────────────
    // OPTIMIZATION: Added @Transactional — multiple saves + org creation must be atomic.
    // OPTIMIZATION: Uses singleton googleIdTokenVerifier instead of creating new one per call.
    @Transactional
    public AuthResponse authenticateGoogle(String idToken, Role role,
            Map<String, List<String>> onboardingAnswers) {

        if (idToken == null || idToken.isBlank()) {
            throw new RuntimeException("Google ID token is missing");
        }

        try {
            // OPTIMIZATION: Using pre-initialized singleton verifier
            GoogleIdToken googleIdToken = googleIdTokenVerifier.verify(idToken);
            if (googleIdToken == null) throw new RuntimeException("Invalid Google token");

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            String name  = (String) payload.get("name");

            boolean isNewUser = !userRepository.existsByEmail(email);

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> createGoogleUser(email, name, role));

            user.setEmailVerified(true);
            user.setApproved(true);
            user.setGoogleUser(true);

            if (isNewUser && role != null) {
                user.setRole(role);
            }

            if (onboardingAnswers != null && !onboardingAnswers.isEmpty()) {
                if (!"COMPLETED".equals(user.getOnboardingStatus())) {
                    try {
                        user.setOnboardingAnswers(objectMapper.writeValueAsString(onboardingAnswers));
                        user.setOnboardingStatus("COMPLETED");
                    } catch (Exception e) {
                        System.err.println("Failed to save onboarding: " + e.getMessage());
                    }
                }
            } else {
                if (isNewUser) {
                    user.setOnboardingStatus("PENDING");
                }
            }

            userRepository.save(user);

            if (user.getOrganizationId() != null) {
                organizationRepository.findById(user.getOrganizationId())
                    .ifPresent(org -> {
                        if ("inactive".equalsIgnoreCase(org.getStatus())) {
                            throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                                "Your organization has been deactivated. Please contact support.");
                        }
                    });
            }

            if (user.isBlocked()) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                    "Your account has been blocked. Please contact support.");
            }

            if (user.getRole() == Role.TENANT_ADMIN
                    && "COMPLETED".equals(user.getOnboardingStatus())
                    && user.getOrganizationId() == null) {

                Organization org = new Organization();
                org.setName(user.getName() + "'s Organization");
                org.setEmail(user.getEmail());
                org.setManagerName(user.getName());
                org.setManagerEmail(user.getEmail());
                org.setOwnerId(user.getId());
                org.setPlan("trial");
                org.setStatus("active");

                Organization savedOrg = organizationRepository.save(org);
                user.setOrganizationId(savedOrg.getId());
                userRepository.save(user);

                authEventProducer.sendEvent(new AuthEvent(
                    "ORG_CREATED",
                    user.getId(),
                    savedOrg.getEmail(),
                    null,
                    savedOrg.getName(),
                    savedOrg.getId().toString()
                ));
            }

            if (isNewUser) {
                authEventProducer.sendEvent(new AuthEvent(
                        "USER_CREATED",
                        user.getId(),
                        user.getEmail(),
                        user.getRole().name(),
                        user.getName(),
                        user.getOrganizationId() != null
                                ? user.getOrganizationId().toString()
                                : null
                ));
                sesEmailService.sendWelcomeEmail(user.getEmail(), user.getName());
            }

            String jwt = jwtUtil.generateToken(user);
            AuthResponse response = new AuthResponse(jwt, user.getEmail(),
                    user.getRole().name(), user.getName());
            response.setNewUser(isNewUser);
            // profileCompleted = has the role-specific Details/Org form been
            // filled — NOT whether the signup quiz (onboardingStatus) is done.
            response.setProfileCompleted(user.isProfileCompleted());
            response.setOrganizationId(user.getOrganizationId());
            return response;

        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // ─────────────────────── FORGOT / RESET PASSWORD ────────────────────────
    // OPTIMIZATION: Replaced ConcurrentHashMap with Redis TTL.
    // Key: auth:reset:{token}, TTL: 15 minutes.
    // Token automatically expires — no manual cleanup needed.
    // On server restart, tokens are not lost (previously they were).
    public void forgotPassword(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
        String token = UUID.randomUUID().toString();

        // OPTIMIZATION: Store in Redis with 15-minute TTL instead of ConcurrentHashMap
        redisTemplate.opsForValue().set(
            "auth:reset:" + token,
            email,
            Duration.ofMinutes(15)
        );

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        emailService.sendResetPasswordMail(email, resetLink);
    }

    public void resetPassword(String token, String newPassword) {
        // OPTIMIZATION: Read token from Redis instead of ConcurrentHashMap
        Object emailObj = redisTemplate.opsForValue().get("auth:reset:" + token);
        if (emailObj == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid or expired token");
        }
        String email = emailObj.toString();
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        // OPTIMIZATION: Delete token from Redis after use (prevent reuse)
        redisTemplate.delete("auth:reset:" + token);
    }
    
    
    // ─────────────────────── MARK PROFILE COMPLETED ─────────────────────────
    // Call once, right after ANY role-specific Details/Org save succeeds on
    // the frontend — regardless of whether that save was handled by this
    // service (Organization entity) or the separate User Service. This is
    // the single source of truth the dashboard gate reads at login.
    public void markProfileCompleted(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
        user.setProfileCompleted(true);
        userRepository.save(user);
    }
    // ─────────────────────── EMAIL VERIFICATION ─────────────────────────────
    public void verifyEmail(String email, String token) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
        EmailVerificationToken savedToken = tokenRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.BAD_REQUEST,
                        "No verification token found. Please resend verification email."));
        if (!savedToken.getToken().equals(token))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Invalid verification token");
        if (savedToken.getExpiryTime().isBefore(LocalDateTime.now()))
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Verification link expired");
        user.setEmailVerified(true);
        userRepository.save(user);
        tokenRepository.delete(savedToken);
    }

    private void sendVerificationLink(User user) {
        String token = UUID.randomUUID().toString();
        EmailVerificationToken verifyToken = new EmailVerificationToken(
                token, LocalDateTime.now().plusHours(24), user);
        tokenRepository.save(verifyToken);
        String encodedEmail = URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);
        String verifyLink = frontendUrl + "/verify-email?token=" + token
                + "&email=" + encodedEmail;
        emailService.sendVerificationMail(user.getEmail(), verifyLink);
    }

    // ─────────────────────── CHANGE PASSWORD ────────────────────────────────
    public void changePassword(ChangePasswordRequest request, String email) {
        if (!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new RuntimeException("Passwords do not match");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    // ─────────────────────── CHECK GOOGLE USER ──────────────────────────────
    // OPTIMIZATION: Uses singleton googleIdTokenVerifier instead of creating new one per call.
    public ResponseEntity<Map<String, Object>> checkGoogleUser(String idToken) {
        try {
            // OPTIMIZATION: Using pre-initialized singleton verifier
            GoogleIdToken googleIdToken = googleIdTokenVerifier.verify(idToken);
            if (googleIdToken == null)
                return ResponseEntity.ok(Map.of("isNewUser", true));

            String email = googleIdToken.getPayload().getEmail();
            User user = userRepository.findByEmail(email).orElse(null);

            if (user == null) {
                return ResponseEntity.ok(Map.of("isNewUser", true));
            }

            if (user.isBlocked()) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(Map.of("message",
                        "Your account has been blocked. Please contact support."));
            }

            if (user.getOrganizationId() != null) {
                Organization org = organizationRepository
                    .findById(user.getOrganizationId()).orElse(null);
                if (org != null && "inactive".equalsIgnoreCase(org.getStatus())) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(Map.of("message",
                            "Your organization has been deactivated. Please contact support."));
                }
            }

            String jwt = jwtUtil.generateToken(user);
            Map<String, Object> resp = new java.util.HashMap<>();
            resp.put("isNewUser", false);
            resp.put("role", user.getRole().name());
            resp.put("name", user.getName());
            resp.put("email", user.getEmail());
            resp.put("token", jwt);
            resp.put("profileCompleted", user.isProfileCompleted());
            resp.put("organizationId", user.getOrganizationId());
            return ResponseEntity.ok(resp);

        } catch (Exception e) {
            return ResponseEntity.ok(Map.of("isNewUser", true));
        }
    }

    // ─────────────────────── DELETE USER ────────────────────────────────────
    public void deleteUser(Long userId) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found with id " + userId));
        userRepository.delete(user);
        authEventProducer.sendEvent(new AuthEvent(
            "USER_DELETED",
            user.getId(),
            user.getEmail(),
            user.getRole() != null ? user.getRole().name() : null,
            user.getName(),
            null
        ));
    }

    // ─────────────────────── ONBOARDING RESPONSES ───────────────────────────
    // OPTIMIZATION: Replaced userRepository.findAll() (full table scan) with
    // a filtered query using UserRepository.findOnboardingUsers(Pageable).
    // Sorting is now done at DB level (created_at DESC via @Query).
    // Pagination added — default page size 200 to avoid OOM at scale.
    public Map<String, Object> getOnboardingResponses() {
        // OPTIMIZATION: Use pageable query instead of findAll() + stream filter
        Pageable pageable = PageRequest.of(0, 200);
        List<User> usersWithAnswers = userRepository.findOnboardingUsers(pageable);

        List<OnboardingResponseDTO> dtoList = new java.util.ArrayList<>();
        for (User user : usersWithAnswers) {
            OnboardingResponseDTO dto = new OnboardingResponseDTO();
            dto.setId(user.getId());
            dto.setName(user.getName());
            dto.setEmail(user.getEmail());
            dto.setRole(user.getRole() != null ? user.getRole().name() : "STUDENT");
            dto.setOnboardingStatus(user.getOnboardingStatus() != null
                    ? user.getOnboardingStatus() : "PENDING");
            dto.setGoogleUser(Boolean.TRUE.equals(user.isGoogleUser()));
            dto.setCreatedAt(user.getCreatedAt() != null
                    ? user.getCreatedAt().toString() : null);
            dto.setBlocked(user.isBlocked());
            try {
                if (user.getOnboardingAnswers() != null
                        && !user.getOnboardingAnswers().trim().isEmpty()) {
                    Map<String, List<String>> answers = objectMapper.readValue(
                            user.getOnboardingAnswers(),
                            new TypeReference<Map<String, List<String>>>() {});
                    dto.setOnboardingAnswers(answers);
                } else {
                    dto.setOnboardingAnswers(new java.util.HashMap<>());
                }
            } catch (Exception e) {
                dto.setOnboardingAnswers(new java.util.HashMap<>());
            }
            dtoList.add(dto);
        }

        long completed = usersWithAnswers.stream()
                .filter(u -> "COMPLETED".equals(u.getOnboardingStatus())).count();
        long pending = usersWithAnswers.stream()
                .filter(u -> !"COMPLETED".equals(u.getOnboardingStatus())).count();
        Map<String, Long> byRole = new java.util.HashMap<>();
        for (User u : usersWithAnswers) {
            String roleName = u.getRole() != null ? u.getRole().name() : "STUDENT";
            byRole.put(roleName, byRole.getOrDefault(roleName, 0L) + 1);
        }

        Map<String, Object> summary = new java.util.HashMap<>();
        summary.put("total",     (long) usersWithAnswers.size());
        summary.put("completed", completed);
        summary.put("pending",   pending);
        summary.put("byRole",    byRole);

        Map<String, Object> result = new java.util.HashMap<>();
        result.put("users",   dtoList);
        result.put("summary", summary);
        return result;
    }

    // ─────────────────────── PRIVATE HELPERS ────────────────────────────────
    private User createGoogleUser(String email, String name, Role role) {
        User user = new User();
        user.setName(name != null ? name : email);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(role != null ? role : Role.STUDENT);
        user.setApproved(true);
        user.setEmailVerified(true);
        user.setGoogleUser(true);
        user.setOnboardingStatus("PENDING");
        return userRepository.save(user);
    }

    public void toggleUserBlock(Long userId, boolean blocked) {
        User user = userRepository.findById(userId)
            .orElseThrow(() -> new ResponseStatusException(
                HttpStatus.NOT_FOUND, "User not found"));
        user.setBlocked(blocked);
        userRepository.save(user);
    }
 // ─────────────────────── UPDATE BASIC PROFILE ───────────────────────────
 // ADD THIS METHOD to AuthService.java
 // (place it right after markProfileCompleted)
 //
 // Also add this import at the top of AuthService.java:
 //   import com.lms.auth.dto.UpdateProfileRequest;
 //
 // Also add these fields to your User entity (User.java) if not already there:
 //   @Column private String phone;
 //   @Column private String city;
 //   @Column(columnDefinition = "TEXT") private String bio;
 //   @Column private String college;
 //   @Column private String course;
 //   @Column private String expertise;
 //   @Column private String experience;
 //   @Column private String linkedinUrl;
 //   @Column private String companyName;
 //   @Column private String designation;
 //   @Column private String teamSize;
 //   @Column private String website;
 // (with getters/setters for each)

 // ─────────────────────── UPDATE PROFILE ────────────────────────────────
 // REPLACE the existing updateProfile() method (line ~617) with this.
 //
 // The old version called user.setPhone(), user.setCollege(), etc. —
 // none of those setters exist on User.java, causing a compile error.
 //
 // This version publishes a PROFILE_UPDATED Kafka event instead,
 // so the User Service handles all role-specific column storage.
 // User.java is NOT touched.
    public void updateProfile(String email, UpdateProfileRequest req) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        try {
            java.util.Map<String, Object> profileData = new java.util.LinkedHashMap<>();
            profileData.put("userId", user.getId());
            profileData.put("email",  user.getEmail());
            profileData.put("role",
                    req.getRole() != null ? req.getRole() : user.getRole().name());

            // ── Student fields ──────────────────────────────────────────────
            if (isSet(req.getMobileNumber()))  profileData.put("mobileNumber",  req.getMobileNumber().trim());
            if (isSet(req.getDateOfBirth()))   profileData.put("dateOfBirth",   req.getDateOfBirth().trim());
            if (isSet(req.getGender()))        profileData.put("gender",        req.getGender().trim());
            if (isSet(req.getCity()))          profileData.put("city",          req.getCity().trim());
            if (isSet(req.getState()))         profileData.put("state",         req.getState().trim());
            if (isSet(req.getCountry()))       profileData.put("country",       req.getCountry().trim());
            if (isSet(req.getQualification())) profileData.put("qualification", req.getQualification().trim());
            if (isSet(req.getCollegeName()))   profileData.put("collegeName",   req.getCollegeName().trim());
            if (isSet(req.getYearOfPassing())) profileData.put("yearOfPassing", req.getYearOfPassing().trim());
            if (isSet(req.getDomain()))        profileData.put("domain",        req.getDomain().trim());
            if (isSet(req.getExperience()))    profileData.put("experience",    req.getExperience().trim());

            // ── Trainer fields ──────────────────────────────────────────────
            if (isSet(req.getLinkedinUrl()))   profileData.put("linkedinUrl",   req.getLinkedinUrl().trim());
            if (isSet(req.getCourseTopic()))   profileData.put("courseTopic",   req.getCourseTopic().trim());
            if (isSet(req.getAudienceSize()))  profileData.put("audienceSize",  req.getAudienceSize().trim());
            if (isSet(req.getFullTimeRole()))  profileData.put("fullTimeRole",  req.getFullTimeRole().trim());

            // ✅ FIX — platforms is now List<String>, not String
            if (req.getPlatforms() != null && !req.getPlatforms().isEmpty()) {
                profileData.put("platforms", req.getPlatforms());
            }

            // ── Manager / Business fields ───────────────────────────────────
            if (isSet(req.getOrganizationName())) profileData.put("organizationName", req.getOrganizationName().trim());
            if (isSet(req.getWebsiteDomain()))    profileData.put("websiteDomain",    req.getWebsiteDomain().trim());
            if (isSet(req.getContactEmail()))     profileData.put("contactEmail",     req.getContactEmail().trim());
            if (isSet(req.getLocation()))         profileData.put("location",         req.getLocation().trim());
            if (isSet(req.getIndustry()))         profileData.put("industry",         req.getIndustry().trim());
            if (isSet(req.getDescription()))      profileData.put("description",      req.getDescription().trim());

            String payloadJson = objectMapper.writeValueAsString(profileData);
            authEventProducer.sendEvent(new AuthEvent(
                    "PROFILE_UPDATED",
                    user.getId(),
                    user.getEmail(),
                    user.getRole().name(),
                    user.getName(),
                    payloadJson
            ));

        } catch (Exception e) {
            System.err.println("[AuthService] PROFILE_UPDATED event failed for "
                    + email + ": " + e.getMessage());
        }

        user.setProfileCompleted(true);
        userRepository.save(user);
    }
 // Null-safe blank check — used above to skip empty fields
 private boolean isSet(String val) {
     return val != null && !val.isBlank();
 }
 //super admin sees admin onboadring responses this endpoint is used for that 
 public Map<String, Object> getAdminOnboardingByEmail(String email) {
	    User user = userRepository.findByEmail(email).orElse(null);
	    if (user == null) return new java.util.HashMap<>();
	    
	    Map<String, Object> result = new java.util.HashMap<>();
	    result.put("email", user.getEmail());
	    result.put("name", user.getName());
	    result.put("onboardingStatus", user.getOnboardingStatus());
	    
	    try {
	        if (user.getOnboardingAnswers() != null && !user.getOnboardingAnswers().trim().isEmpty()) {
	            Map<String, List<String>> answers = objectMapper.readValue(
	                user.getOnboardingAnswers(),
	                new TypeReference<Map<String, List<String>>>() {});
	            result.put("onboardingAnswers", answers);
	        } else {
	            result.put("onboardingAnswers", new java.util.HashMap<>());
	        }
	    } catch (Exception e) {
	        result.put("onboardingAnswers", new java.util.HashMap<>());
	    }
	    return result;
	}
 
 }