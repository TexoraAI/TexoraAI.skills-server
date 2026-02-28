package com.lms.auth.controller;

import com.lms.auth.dto.UserApprovalStatusResponse;
import com.lms.auth.service.AuthStatusService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/auth")
public class AuthStatusController {

    private final AuthStatusService authStatusService;

    public AuthStatusController(AuthStatusService authStatusService) {
        this.authStatusService = authStatusService;
    }

    // ✅ anyone can check their status
    // GET /api/auth/status?email=abc@gmail.com
    @GetMapping("/status")
    public ResponseEntity<UserApprovalStatusResponse> getStatus(@RequestParam String email) {
        return ResponseEntity.ok(authStatusService.getStatusByEmail(email));
    }
}
