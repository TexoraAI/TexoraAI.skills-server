
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
import com.lms.auth.dto.AdminUpdateUserRequest;
import com.lms.auth.dto.AdminUserViewDTO;

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
    private final RedisTemplate<String, Object> redisTemplate;

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${app.frontend-url}")
    private String frontendUrl;

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

    // ─────────────────────── ORG-ON-SIGNUP GUARANTEE (NEW) ──────────────────
    // Single source of truth for "every TENANT_ADMIN gets an Organization".
    // Called from register(), authenticateGoogle(), and
    // saveOnboardingForCurrentUser() so the guarantee holds no matter which
    // of the three entry points a TENANT_ADMIN came through, and no matter
    // whether onboardingAnswers happened to be present on that exact call.
    // Idempotent: safe to call on every login, not just first completion.
    @Transactional
    public void ensureOrganizationForTenantAdmin(User user) {
        if (user.getRole() != Role.TENANT_ADMIN || user.getOrganizationId() != null) {
            return;
        }

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

    @Transactional
    public AuthResponse register(RegisterRequest request, String requesterEmail) {
        if (userRepository.existsByEmail(request.getEmail())) {
            User existing = userRepository.findByEmail(request.getEmail()).get();
            if (existing.isEmailVerified() && !existing.isApproved()) {
                existing.setName(request.getName());
                existing.setRole(request.getRole() != null ? request.getRole() : existing.getRole());
                userRepository.save(existing);
                return null;
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

        boolean createdByAdmin = false;
        if (requesterEmail != null) {
            User requester = userRepository.findByEmail(requesterEmail).orElse(null);
            if (requester != null &&
                    (requester.getRole() == Role.TENANT_ADMIN || requester.getRole() == Role.SUPER_ADMIN)) {
                createdByAdmin = true;
            }
        }
        user.setPasswordSet(!createdByAdmin);

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

        // NEW — the fix for gap #4 on the register() path. Previously this
        // path had zero org-creation logic at all. This must run BEFORE the
        // USER_CREATED event below so the event's organizationId is correct
        // on the very first sync into User Service, instead of requiring a
        // second/racing event later.
        ensureOrganizationForTenantAdmin(savedUser);

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

        if (createdByAdmin) {
            String resetToken = UUID.randomUUID().toString();
            redisTemplate.opsForValue().set(
                "auth:reset:" + resetToken,
                savedUser.getEmail(),
                java.time.Duration.ofMinutes(15)
            );
            String setPasswordLink = frontendUrl + "/reset-password?token=" + resetToken;
            sesEmailService.sendSetPasswordEmail(savedUser.getEmail(), savedUser.getName(), setPasswordLink);
            return null;
        } else {
            sesEmailService.sendWelcomeEmail(savedUser.getEmail(), savedUser.getName());
            String token = jwtUtil.generateToken(savedUser);
            AuthResponse response = new AuthResponse(token, savedUser.getEmail(), savedUser.getRole().name(), savedUser.getName());
            response.setNewUser(true);
            response.setProfileCompleted(false);
            response.setOrganizationId(savedUser.getOrganizationId());
            return response;
        }
    }

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

    @Transactional
    public AuthResponse authenticateGoogle(String idToken, Role role,
            Map<String, List<String>> onboardingAnswers) {

        if (idToken == null || idToken.isBlank()) {
            throw new RuntimeException("Google ID token is missing");
        }

        try {
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

            // CHANGED — was previously inlined here and gated on
            // "COMPLETED".equals(user.getOnboardingStatus()) in this SAME
            // call. That gate is gone: this now fires on every Google login
            // for a TENANT_ADMIN with no org yet, regardless of whether
            // onboardingAnswers happened to arrive in this exact request.
            ensureOrganizationForTenantAdmin(user);

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
            response.setProfileCompleted(user.isProfileCompleted());
            response.setOrganizationId(user.getOrganizationId());
            return response;

        } catch (ResponseStatusException rse) {
            throw rse;
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void forgotPassword(String email) {
        userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
        String token = UUID.randomUUID().toString();

        redisTemplate.opsForValue().set(
            "auth:reset:" + token,
            email,
            Duration.ofMinutes(15)
        );

        String resetLink = frontendUrl + "/reset-password?token=" + token;
        emailService.sendResetPasswordMail(email, resetLink);
    }

    public void resetPassword(String token, String newPassword) {
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
        user.setPasswordSet(true);
        userRepository.save(user);

        redisTemplate.delete("auth:reset:" + token);

        sesEmailService.sendWelcomeEmail(user.getEmail(), user.getName());
    }

    public void markProfileCompleted(String email) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));
        user.setProfileCompleted(true);
        userRepository.save(user);
    }

    // CHANGED — this now actually does what its comment always claimed:
    // mirrors authenticateGoogle()'s org guarantee for manual-signup /
    // already-authenticated users completing onboarding via this endpoint.
    // Previously this method set role + onboardingStatus but had zero
    // Organization logic — a TENANT_ADMIN going through this specific path
    // would have organizationId stuck at null forever, silently.
    @Transactional
    public void saveOnboardingForCurrentUser(String email, Role role,
            Map<String, List<String>> onboardingAnswers) {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        if (role != null) {
            user.setRole(role);
        }

        if (onboardingAnswers != null && !onboardingAnswers.isEmpty()) {
            try {
                user.setOnboardingAnswers(objectMapper.writeValueAsString(onboardingAnswers));
                user.setOnboardingStatus("COMPLETED");
            } catch (Exception e) {
                System.err.println("Failed to save onboarding for " + email + ": " + e.getMessage());
            }
        } else {
            user.setOnboardingStatus("COMPLETED");
        }

        User savedUser = userRepository.save(user);

        // NEW — the actual fix. Idempotent no-op for STUDENT/TRAINER.
        ensureOrganizationForTenantAdmin(savedUser);
    }

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

    public void changePassword(ChangePasswordRequest request, String email) {
        if (!request.getNewPassword().equals(request.getConfirmPassword()))
            throw new RuntimeException("Passwords do not match");
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));
        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }

    public ResponseEntity<Map<String, Object>> checkGoogleUser(String idToken) {
        try {
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

    public Map<String, Object> getOnboardingResponses() {
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

            if (isSet(req.getLinkedinUrl()))   profileData.put("linkedinUrl",   req.getLinkedinUrl().trim());
            if (isSet(req.getCourseTopic()))   profileData.put("courseTopic",   req.getCourseTopic().trim());
            if (isSet(req.getAudienceSize()))  profileData.put("audienceSize",  req.getAudienceSize().trim());
            if (isSet(req.getFullTimeRole()))  profileData.put("fullTimeRole",  req.getFullTimeRole().trim());

            if (req.getPlatforms() != null && !req.getPlatforms().isEmpty()) {
                profileData.put("platforms", req.getPlatforms());
            }

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

    private boolean isSet(String val) {
        return val != null && !val.isBlank();
    }

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

    public List<AdminUserViewDTO> getOrgUsersForAdmin(String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));

        List<User> targetUsers;
        if (requester.getRole() == Role.SUPER_ADMIN) {
            targetUsers = userRepository.findAll();
        } else {
            if (requester.getOrganizationId() == null) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN, "No organization linked to this account");
            }
            targetUsers = userRepository.findByOrganizationId(requester.getOrganizationId());
        }

        List<AdminUserViewDTO> result = new java.util.ArrayList<>();
        for (User u : targetUsers) {
            AdminUserViewDTO dto = new AdminUserViewDTO();
            dto.setId(u.getId());
            dto.setName(u.getName());
            dto.setEmail(u.getEmail());
            dto.setRole(u.getRole().name());
            dto.setBlocked(u.isBlocked());
            dto.setPasswordSet(Boolean.TRUE.equals(u.getPasswordSet()));
            result.add(dto);
        }
        return result;
    }

    @Transactional
    public void adminUpdateUser(Long userId, AdminUpdateUserRequest req, String requesterEmail) {

        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean isSuperAdmin = requester.getRole() == Role.SUPER_ADMIN;
        if (!isSuperAdmin) {
            if (requester.getOrganizationId() == null
                    || target.getOrganizationId() == null
                    || !requester.getOrganizationId().equals(target.getOrganizationId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only manage users in your own organization");
            }
        }

        boolean nameChanged  = false;
        boolean emailChanged = false;

        if (isSet(req.getName()) && !req.getName().trim().equals(target.getName())) {
            target.setName(req.getName().trim());
            nameChanged = true;
        }

        if (isSet(req.getEmail()) && !req.getEmail().trim().equalsIgnoreCase(target.getEmail())) {
            String newEmail = req.getEmail().trim();
            if (userRepository.existsByEmail(newEmail)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
            }
            target.setEmail(newEmail);
            emailChanged = true;
        }

        if (isSet(req.getRole())) {
            try {
                target.setRole(Role.valueOf(req.getRole().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: " + req.getRole());
            }
        }

        userRepository.save(target);

        if (nameChanged || emailChanged || isSet(req.getRole())) {
            authEventProducer.sendEvent(new AuthEvent(
                    "USER_UPDATED",
                    target.getId(),
                    target.getEmail(),
                    target.getRole().name(),
                    target.getName(),
                    target.getOrganizationId() != null ? target.getOrganizationId().toString() : null
            ));
        }
    }

    @Transactional
    public void adminResendSetPasswordEmail(Long userId, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
        User target = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean isSuperAdmin = requester.getRole() == Role.SUPER_ADMIN;
        if (!isSuperAdmin) {
            if (requester.getOrganizationId() == null
                    || target.getOrganizationId() == null
                    || !requester.getOrganizationId().equals(target.getOrganizationId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only manage users in your own organization");
            }
        }

        if (Boolean.TRUE.equals(target.getPasswordSet())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This user has already set their password");
        }

        String resetToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                "auth:reset:" + resetToken,
                target.getEmail(),
                Duration.ofMinutes(15)
        );
        String setPasswordLink = frontendUrl + "/reset-password?token=" + resetToken;
        sesEmailService.sendSetPasswordEmail(target.getEmail(), target.getName(), setPasswordLink);
    }

    @Transactional
    public void adminUpdateUserByEmail(String targetEmail, AdminUpdateUserRequest req, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
        User target = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean isSuperAdmin = requester.getRole() == Role.SUPER_ADMIN;
        if (!isSuperAdmin) {
            if (requester.getOrganizationId() == null
                    || target.getOrganizationId() == null
                    || !requester.getOrganizationId().equals(target.getOrganizationId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only manage users in your own organization");
            }
        }

        boolean nameChanged  = false;
        boolean emailChanged = false;

        if (isSet(req.getName()) && !req.getName().trim().equals(target.getName())) {
            target.setName(req.getName().trim());
            nameChanged = true;
        }

        if (isSet(req.getEmail()) && !req.getEmail().trim().equalsIgnoreCase(target.getEmail())) {
            String newEmail = req.getEmail().trim();
            if (userRepository.existsByEmail(newEmail)) {
                throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already in use");
            }
            target.setEmail(newEmail);
            emailChanged = true;
        }

        if (isSet(req.getRole())) {
            try {
                target.setRole(Role.valueOf(req.getRole().toUpperCase()));
            } catch (IllegalArgumentException ex) {
                throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Invalid role: " + req.getRole());
            }
        }

        userRepository.save(target);

        if (nameChanged || emailChanged || isSet(req.getRole())) {
            authEventProducer.sendEvent(new AuthEvent(
                    "USER_UPDATED",
                    target.getId(),
                    target.getEmail(),
                    target.getRole().name(),
                    target.getName(),
                    target.getOrganizationId() != null ? target.getOrganizationId().toString() : null
            ));
        }
    }

    @Transactional
    public void adminResendSetPasswordEmailByEmail(String targetEmail, String requesterEmail) {
        User requester = userRepository.findByEmail(requesterEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Admin not found"));
        User target = userRepository.findByEmail(targetEmail)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));

        boolean isSuperAdmin = requester.getRole() == Role.SUPER_ADMIN;
        if (!isSuperAdmin) {
            if (requester.getOrganizationId() == null
                    || target.getOrganizationId() == null
                    || !requester.getOrganizationId().equals(target.getOrganizationId())) {
                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "You can only manage users in your own organization");
            }
        }

        if (Boolean.TRUE.equals(target.getPasswordSet())) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "This user has already set their password");
        }

        String resetToken = UUID.randomUUID().toString();
        redisTemplate.opsForValue().set(
                "auth:reset:" + resetToken,
                target.getEmail(),
                Duration.ofMinutes(15)
        );
        String setPasswordLink = frontendUrl + "/reset-password?token=" + resetToken;
        sesEmailService.sendSetPasswordEmail(target.getEmail(), target.getName(), setPasswordLink);
    }
}