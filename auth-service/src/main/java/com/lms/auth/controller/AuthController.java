


package com.lms.auth.controller;

import com.lms.auth.dto.AuthResponse;
import com.lms.auth.dto.OnboardingSaveRequest;
import com.lms.auth.dto.ChangePasswordRequest;
import com.lms.auth.dto.ForgotPasswordRequest;
import com.lms.auth.dto.GoogleLoginRequest;
import com.lms.auth.dto.LoginRequest;
import com.lms.auth.dto.RegisterRequest;
import com.lms.auth.dto.UpdateProfileRequest;
import com.lms.auth.model.Role;
import com.lms.auth.security.JwtUtil;
import com.lms.auth.service.AuthService;
import com.lms.auth.dto.AdminUpdateUserRequest;
import com.lms.auth.dto.AdminUserViewDTO;
import java.util.List;
import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lms.auth.dto.VerifyEmailRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil;

    public AuthController(AuthService authService, JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

  
//    @PostMapping("/register")
//    public void register(@RequestBody RegisterRequest request,
//                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
//        String requesterEmail = null;
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            try {
//                requesterEmail = jwtUtil.extractEmail(authHeader.replace("Bearer ", ""));
//            } catch (Exception e) {
//                requesterEmail = null; // bad/expired token → treat as public self-signup
//            }
//        }
//        authService.register(request, requesterEmail);
//    }
    @PostMapping("/register")
    public ResponseEntity<?> register(@RequestBody RegisterRequest request,
                          @RequestHeader(value = "Authorization", required = false) String authHeader) {
        String requesterEmail = null;
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            try {
                requesterEmail = jwtUtil.extractEmail(authHeader.replace("Bearer ", ""));
            } catch (Exception e) {
                requesterEmail = null;
            }
        }
        AuthResponse authResponse = authService.register(request, requesterEmail);
        if (authResponse != null) {
            return ResponseEntity.ok(authResponse);
        }
        return ResponseEntity.ok(Map.of("message", "User created successfully"));
    }

    // ================= EMAIL LOGIN =================
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.authenticate(
                request.getEmail(),
                request.getPassword()
        );
    }

    // ================= SUPERADMIN: DELETE USER =================
    @DeleteMapping("/users/{userId}")
    public ResponseEntity<?> deleteUser(@PathVariable Long userId) {
        authService.deleteUser(userId);
        return ResponseEntity.ok(Map.of(
            "message", "User deleted successfully",
            "userId", userId
        ));
    }

    @PatchMapping("/users/{userId}/block")
    public ResponseEntity<?> toggleUserBlock(
            @PathVariable Long userId,
            @RequestParam boolean blocked) {
        authService.toggleUserBlock(userId, blocked);
        return ResponseEntity.ok(Map.of(
            "message", blocked ? "User blocked" : "User unblocked",
            "userId", userId,
            "blocked", blocked
        ));
    }

    // ================= GOOGLE LOGIN =================
    @PostMapping("/google")
    public AuthResponse googleLogin(@RequestBody GoogleLoginRequest request) {
        System.out.println(request);
        return authService.authenticateGoogle(
                request.getIdToken(),
                request.getRole() != null ? request.getRole() : Role.STUDENT,
                request.getOnboardingAnswers()
        );
    }

    // ================= FORGOT PASSWORD =================
    @PostMapping("/forgot-password")
    public ResponseEntity<Map<String, String>> forgotPassword(
            @RequestBody ForgotPasswordRequest request) {
        authService.forgotPassword(request.getEmail());
        return ResponseEntity.ok(
            Map.of("message", "Password reset link sent to your email")
        );
    }

    // ================= RESET PASSWORD =================
    @PostMapping("/reset-password")
    public void resetPassword(@RequestParam String token,
                              @RequestParam String newPassword) {
        authService.resetPassword(token, newPassword);
    }

    // ================= VERIFY EMAIL =================
    @PostMapping("/verify-email")
    public ResponseEntity<?> verifyEmail(@RequestBody VerifyEmailRequest request) {
        authService.verifyEmail(request.getEmail(), request.getToken());
        return ResponseEntity.ok(
                Map.of("message", "Email verified successfully")
        );
    }

    // ================= CHANGE PASSWORD =================
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        authService.changePassword(request, email);
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }

    // ================= CHECK GOOGLE USER (read-only) =================
    @PostMapping("/check-google")
    public ResponseEntity<Map<String, Object>> checkGoogleUser(@RequestBody GoogleLoginRequest request) {
        return authService.checkGoogleUser(request.getIdToken());
    }

    // ================= SUPERADMIN: GET ALL ONBOARDING RESPONSES =================
    @GetMapping("/admin/onboarding-responses")
    public ResponseEntity<Map<String, Object>> getOnboardingResponses() {
        return ResponseEntity.ok(authService.getOnboardingResponses());
    }

    // ================= SAVE ROLE-SPECIFIC PROFILE FIELDS =================
    // Called by ProfileDetailsForm.jsx: PATCH /api/auth/me/profile
    // Accepts all role-specific fields (student / trainer / Manager).
    // Publishes a PROFILE_UPDATED Kafka event so the User Service syncs
    // into its own role-specific tables (StudentProfile, TrainerProfile…).
    @PatchMapping("/me/profile")
    public ResponseEntity<?> updateProfile(
            @RequestBody UpdateProfileRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        authService.updateProfile(email, request);
        return ResponseEntity.ok(Map.of("message", "Profile updated successfully"));
    }

    // ================= MARK PROFILE COMPLETED =================
    // Frontend calls this right after /me/profile saves (or on skip).
    // Sets profileCompleted=true — this is what the dashboard gate reads.
    @PatchMapping("/me/profile-completed")
    public ResponseEntity<?> markProfileCompleted(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        authService.markProfileCompleted(email);
        return ResponseEntity.ok(Map.of(
            "message", "Profile marked as completed",
            "profileCompleted", true
        ));
    }
  //super admin sees admin onboadring responses this endpoint is used for that 
    @GetMapping("/admin/onboarding-by-email")
    public ResponseEntity<Map<String, Object>> getAdminOnboardingByEmail(
            @RequestParam String email) {
        return ResponseEntity.ok(authService.getAdminOnboardingByEmail(email));
    }
    @GetMapping("/admin/org-users")
    public ResponseEntity<List<AdminUserViewDTO>> getOrgUsers(
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        return ResponseEntity.ok(authService.getOrgUsersForAdmin(email));
    }

    @PatchMapping("/admin/users/{userId}")
    public ResponseEntity<?> adminUpdateUser(
            @PathVariable Long userId,
            @RequestBody AdminUpdateUserRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String requesterEmail = jwtUtil.extractEmail(token);
        authService.adminUpdateUser(userId, request, requesterEmail);
        return ResponseEntity.ok(Map.of("message", "User updated successfully"));
    }

    @PostMapping("/admin/users/{userId}/resend-set-password")
    public ResponseEntity<?> resendSetPassword(
            @PathVariable Long userId,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String requesterEmail = jwtUtil.extractEmail(token);
        authService.adminResendSetPasswordEmail(userId, requesterEmail);
        return ResponseEntity.ok(Map.of("message", "Set-password email resent"));
    }
    
    @PatchMapping("/admin/users/by-email")
    public ResponseEntity<?> adminUpdateUserByEmail(
            @RequestParam String email,
            @RequestBody AdminUpdateUserRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String requesterEmail = jwtUtil.extractEmail(token);
        authService.adminUpdateUserByEmail(email, request, requesterEmail);
        return ResponseEntity.ok(Map.of("message", "User updated successfully"));
    }

    @PostMapping("/admin/users/by-email/resend-set-password")
    public ResponseEntity<?> resendSetPasswordByEmail(
            @RequestParam String email,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String requesterEmail = jwtUtil.extractEmail(token);
        authService.adminResendSetPasswordEmailByEmail(email, requesterEmail);
        return ResponseEntity.ok(Map.of("message", "Set-password email resent"));
    }
    
    @PatchMapping("/me/onboarding")
    public ResponseEntity<?> saveOnboarding(
            @RequestBody OnboardingSaveRequest request,
            @RequestHeader("Authorization") String authHeader) {
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token);
        authService.saveOnboardingForCurrentUser(email, request.getRole(), request.getOnboardingAnswers());
        return ResponseEntity.ok(Map.of("message", "Onboarding saved successfully"));
    }
}
