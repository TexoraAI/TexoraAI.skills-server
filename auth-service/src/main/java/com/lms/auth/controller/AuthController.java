package com.lms.auth.controller;

import com.lms.auth.dto.AuthResponse;
import com.lms.auth.dto.ChangePasswordRequest;
import com.lms.auth.dto.ForgotPasswordRequest;
import com.lms.auth.dto.GoogleLoginRequest;
import com.lms.auth.dto.LoginRequest;
import com.lms.auth.dto.RegisterRequest;
import com.lms.auth.model.Role;
import com.lms.auth.security.JwtUtil;
import com.lms.auth.service.AuthService;

import java.util.Map;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import com.lms.auth.dto.VerifyEmailRequest;
import com.lms.auth.dto.ResendVerificationRequest;

@RestController
@RequestMapping("/api/auth")
public class AuthController {

    private final AuthService authService;
    private final JwtUtil jwtUtil; 

    public AuthController(AuthService authService,JwtUtil jwtUtil) {
        this.authService = authService;
        this.jwtUtil = jwtUtil;
    }

    // ================= REGISTER =================
    @PostMapping("/register")
    public void register(@RequestBody RegisterRequest request) {
        authService.register(request);
    }

    // ================= EMAIL LOGIN =================
    @PostMapping("/login")
    public AuthResponse login(@RequestBody LoginRequest request) {
        return authService.authenticate(
                request.getEmail(),
                request.getPassword()
        );
    }

    // ================= GOOGLE LOGIN =================
    @PostMapping("/google")
    public AuthResponse googleLogin(@RequestBody GoogleLoginRequest request) {
    	System.out.println(request);
        return authService.authenticateGoogle(
                request.getIdToken(),
                request.getRole() != null ? request.getRole() : Role.STUDENT
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

    // ================= RESEND VERIFICATION =================
    @PostMapping("/resend-verification")
    public ResponseEntity<?> resendVerification(@RequestBody ResendVerificationRequest request) {

        authService.resendVerification(request.getEmail());

        return ResponseEntity.ok(
                Map.of("message", "Verification link sent again to your email")
        );
    }
//    @PostMapping("/change-password")
//    public ResponseEntity<?> changePassword(@RequestBody ChangePasswordRequest request) {
//
//        authService.changePassword(request);
//
//        return ResponseEntity.ok(
//            Map.of("message", "Password updated successfully")
//        );
//    }
 // AuthController.java - change-password endpoint
    @PostMapping("/change-password")
    public ResponseEntity<?> changePassword(
            @RequestBody ChangePasswordRequest request,
            @RequestHeader("Authorization") String authHeader) {

        // Extract email from JWT directly — don't rely on SecurityContext
        String token = authHeader.replace("Bearer ", "");
        String email = jwtUtil.extractEmail(token); // add this method to JwtUtil

        authService.changePassword(request, email); // pass email explicitly
        return ResponseEntity.ok(Map.of("message", "Password updated successfully"));
    }
 // ================= CHECK GOOGLE USER (read-only) =================
    @PostMapping("/check-google")
    public ResponseEntity<Map<String, Object>> checkGoogleUser(@RequestBody GoogleLoginRequest request) {
        return authService.checkGoogleUser(request.getIdToken());
    }
}
