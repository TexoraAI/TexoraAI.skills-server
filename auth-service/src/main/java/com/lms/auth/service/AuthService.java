





package com.lms.auth.service;
import com.lms.auth.event.AuthEvent;

import com.lms.auth.producer.AuthEventProducer;

//Spring
import org.springframework.http.ResponseEntity;

//Java Utility
import java.util.Map;
import java.util.Collections;

//Google Auth
import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;

//(Already needed in your class, agar use ho raha hai)
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;

import com.google.api.client.googleapis.auth.oauth2.GoogleIdToken;
import com.google.api.client.googleapis.auth.oauth2.GoogleIdTokenVerifier;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.javanet.NetHttpTransport;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.lms.auth.dto.AuthResponse;
import com.lms.auth.dto.ChangePasswordRequest;
import com.lms.auth.dto.RegisterRequest;
import com.lms.auth.model.EmailVerificationToken;
import com.lms.auth.model.Role;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

import com.lms.auth.model.User;
import com.lms.auth.repository.EmailVerificationTokenRepository;
import com.lms.auth.repository.UserRepository;
import com.lms.auth.security.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class AuthService {
	private final AuthEventProducer authEventProducer;

    private final UserRepository userRepository;
    private final EmailVerificationTokenRepository tokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final EmailService emailService;
    private final JwtUtil jwtUtil;

    @Value("${google.client-id}")
    private String googleClientId;

    @Value("${app.frontend-url}")
    private String frontendUrl;

    
    private final ConcurrentHashMap<String, String> resetTokens = new ConcurrentHashMap<>();

    private static final JacksonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();
    private static NetHttpTransport HTTP_TRANSPORT;

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public AuthService(UserRepository userRepository,
                       EmailVerificationTokenRepository tokenRepository,
                       PasswordEncoder passwordEncoder,
                       EmailService emailService,
                       JwtUtil jwtUtil,AuthEventProducer authEventProducer) {
        this.userRepository = userRepository;
        this.tokenRepository = tokenRepository;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
        this.jwtUtil = jwtUtil;
        this.authEventProducer = authEventProducer;
    }

    // ================= REGISTER =================
//    public void register(RegisterRequest request) {
//
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
//        }
//
//        User user = new User();
//        user.setName(request.getName());
//        user.setEmail(request.getEmail());
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//        user.setRole(request.getRole() != null ? request.getRole() : Role.STUDENT);
//
//        user.setApproved(false);
//        user.setEmailVerified(false);
//
//        User savedUser = userRepository.save(user);
//
//        // ✅ Send Verification Mail
//        sendVerificationLink(savedUser);
//    }

    public void register(RegisterRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            
            // If this is a Google user re-submitting their profile form,
            // the account may already exist (emailVerified=true, approved=false)
            // Just update their details instead of throwing 409
            User existing = userRepository.findByEmail(request.getEmail()).get();
            
            if (existing.isEmailVerified() && !existing.isApproved()) {
                // Update profile fields and re-send verification / approval flow
                existing.setName(request.getName());
                existing.setRole(request.getRole() != null ? request.getRole() : existing.getRole());
                userRepository.save(existing);
                return; // ← success, no 409
            }
            
            // Genuinely duplicate registration attempt
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setName(request.getName());
        user.setEmail(request.getEmail());
        user.setPassword(
            request.getPassword() != null
                ? passwordEncoder.encode(request.getPassword())
                : passwordEncoder.encode(UUID.randomUUID().toString()) // Google user
        );
        user.setRole(request.getRole() != null ? request.getRole() : Role.STUDENT);
        user.setApproved(false);
        user.setEmailVerified(false);

        User savedUser = userRepository.save(user);
        sendVerificationLink(savedUser);
    }
    
    // ================= EMAIL LOGIN =================
    public AuthResponse authenticate(String email, String password) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.UNAUTHORIZED,
                        "Invalid email or password"
                ));

        if (!passwordEncoder.matches(password, user.getPassword())) {
            throw new ResponseStatusException(
                    HttpStatus.UNAUTHORIZED,
                    "Invalid email or password"
            );
        }

        // ✅ Block login until email verified
        if (!user.isEmailVerified()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Please verify your email first"
            );
        }

        // ✅ Block login until approved (Student + Trainer + Business)
        if ((user.getRole() == Role.STUDENT || user.getRole() == Role.TRAINER || user.getRole() == Role.BUSINESS)
                && !user.isApproved()) {
            throw new ResponseStatusException(
                    HttpStatus.FORBIDDEN,
                    "Your application is not approved yet"
            );
        }

        String token = jwtUtil.generateToken(user);

        return new AuthResponse(
                token,
                user.getEmail(),
                user.getRole().name()
        );
    }

    // ================= GOOGLE LOGIN =================
    public AuthResponse authenticateGoogle(String idToken, Role role) {

        if (idToken == null || idToken.isBlank()) {
            throw new RuntimeException("Google ID token is missing");
        }

        try {
            GoogleIdTokenVerifier verifier =
                    new GoogleIdTokenVerifier.Builder(HTTP_TRANSPORT, JSON_FACTORY)
                            .setAudience(Collections.singletonList(googleClientId))
                            .build();

            GoogleIdToken googleIdToken = verifier.verify(idToken);

            if (googleIdToken == null) {
                throw new RuntimeException("Invalid Google token");
            }

            GoogleIdToken.Payload payload = googleIdToken.getPayload();
            String email = payload.getEmail();
            String name = (String) payload.get("name");

            User user = userRepository.findByEmail(email)
                    .orElseGet(() -> createGoogleUser(email, name, role));

            // ✅ Google users are verified automatically
            user.setEmailVerified(true);
            userRepository.save(user);

            // ✅ Block login until approved (Student + Trainer + Business)
            if ((user.getRole() == Role.STUDENT || user.getRole() == Role.TRAINER || user.getRole() == Role.BUSINESS)
                    && !user.isApproved()) {
                throw new ResponseStatusException(
                        HttpStatus.FORBIDDEN,
                        "Your application is not approved yet"
                );
            }

            String jwt = jwtUtil.generateToken(user);

            return new AuthResponse(jwt, user.getEmail(), user.getRole().name());

        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    // ================= FORGOT PASSWORD =================
    public void forgotPassword(String email) {

        userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        String token = UUID.randomUUID().toString();
        resetTokens.put(token, email);

        String resetLink = frontendUrl + "/reset-password?token=" + token;



        emailService.sendResetPasswordMail(email, resetLink);
    }

    // ================= RESET PASSWORD =================
    public void resetPassword(String token, String newPassword) {

        String email = resetTokens.get(token);
        if (email == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid or expired token"
            );
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);

        resetTokens.remove(token);
    }

//    // ================= VERIFY EMAIL =================

    public void verifyEmail(String email, String token) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        EmailVerificationToken savedToken = tokenRepository.findByUserId(user.getId())
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.BAD_REQUEST,
                        "No verification token found. Please resend verification email."
                ));

        if (!savedToken.getToken().equals(token)) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Invalid verification token"
            );
        }

        if (savedToken.getExpiryTime().isBefore(LocalDateTime.now())) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Verification link expired"
            );
        }

        user.setEmailVerified(true);
        userRepository.save(user);

        tokenRepository.delete(savedToken);
    }


    // ================= RESEND VERIFICATION =================
    public void resendVerification(String email) {

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "User not found"
                ));

        if (user.isEmailVerified()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "Email already verified"
            );
        }

        // delete old token if exists
        tokenRepository.deleteByUserId(user.getId());

        sendVerificationLink(user);
    }

    // ================= HELPER: SEND VERIFICATION LINK =================
    private void sendVerificationLink(User user) {

        String token = UUID.randomUUID().toString();

        EmailVerificationToken verifyToken = new EmailVerificationToken(
                token,
                LocalDateTime.now().plusHours(24),
                user
        );

        tokenRepository.save(verifyToken);
     // ✅ Improvement 1: encode email properly
        String encodedEmail = URLEncoder.encode(user.getEmail(), StandardCharsets.UTF_8);

        // ✅ Improvement 2: frontend url from config
        String verifyLink =
                frontendUrl + "/verify-email?token=" + token + "&email=" + encodedEmail;

        emailService.sendVerificationMail(user.getEmail(), verifyLink);
    }
//    public void changePassword(ChangePasswordRequest request) {
//
//        // 🔴 1. Validate
//        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
//            throw new RuntimeException("Passwords do not match");
//        }
//
//        // 🔴 2. Get logged-in user
//        String email = SecurityContextHolder.getContext()
//                .getAuthentication()
//                .getName();
//
//        User user = userRepository.findByEmail(email)
//                .orElseThrow(() -> new RuntimeException("User not found"));
//
//        // 🔴 3. Update password
//        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
//
//        userRepository.save(user);
//    }
    
    public void changePassword(ChangePasswordRequest request, String email) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new RuntimeException("Passwords do not match");
        }

        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("User not found"));

        user.setPassword(passwordEncoder.encode(request.getNewPassword()));
        userRepository.save(user);
    }
    
    
    // ================= CHECK GOOGLE USER (read-only, never writes to DB) =================
    	    public ResponseEntity<Map<String, Object>> checkGoogleUser(String idToken) {
    	        try {
    	            GoogleIdTokenVerifier verifier =
    	                    new GoogleIdTokenVerifier.Builder(HTTP_TRANSPORT, JSON_FACTORY)
    	                            .setAudience(Collections.singletonList(googleClientId))
    	                            .build();
//hello
    	            GoogleIdToken googleIdToken = verifier.verify(idToken);
    	            if (googleIdToken == null) {
    	                return ResponseEntity.ok(Map.of("isNewUser", true));
    	            }

    	            String email = googleIdToken.getPayload().getEmail();

    	            return userRepository.findByEmail(email)
    	                    .filter(User::isApproved)
    	                    .map(user -> ResponseEntity.ok(Map.<String, Object>of(
    	                            "isNewUser", false,
    	                            "role",      user.getRole().name(),
    	                            "name",      user.getName(),
    	                            "email",     user.getEmail()
    	                    )))
    	                    .orElse(ResponseEntity.ok(Map.of("isNewUser", true)));

    	        } catch (Exception e) {
    	            // On any error, treat as new user — safe fallback
    	            return ResponseEntity.ok(Map.of("isNewUser", true));
    	        }
    	    }
        // ================= GOOGLE USER CREATION =================
    
    
    
    private User createGoogleUser(String email, String name, Role role) {

        User user = new User();
        user.setName(name);
        user.setEmail(email);
        user.setPassword(passwordEncoder.encode(UUID.randomUUID().toString()));
        user.setRole(role != null ? role : Role.STUDENT);

        user.setApproved(false);
        user.setEmailVerified(true); // google verified

        return userRepository.save(user);
    }
}






