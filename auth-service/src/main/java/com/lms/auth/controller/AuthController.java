package com.lms.auth.controller;

import com.lms.auth.dto.AuthResponse;

import com.lms.auth.dto.ForgotPasswordRequest;
import com.lms.auth.dto.GoogleLoginRequest;
import com.lms.auth.dto.LoginRequest;
import com.lms.auth.dto.RegisterRequest;
import com.lms.auth.model.Role;
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

    public AuthController(AuthService authService) {
        this.authService = authService;
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

}
