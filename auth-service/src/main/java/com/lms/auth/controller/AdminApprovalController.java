package com.lms.auth.controller;

import com.lms.auth.dto.PendingUserResponse;
import com.lms.auth.model.Role;
import com.lms.auth.service.AdminApprovalService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/admin/approval")
public class AdminApprovalController {

    private final AdminApprovalService adminApprovalService;

    public AdminApprovalController(AdminApprovalService adminApprovalService) {
        this.adminApprovalService = adminApprovalService;
    }

    // ================= STUDENT =================
    @GetMapping("/students/pending")
    public List<PendingUserResponse> pendingStudents() {
        return adminApprovalService.getPending(Role.STUDENT);
    }

    @PutMapping("/students/approve/{id}")
    public ResponseEntity<?> approveStudent(@PathVariable Long id) {
        adminApprovalService.approve(id, Role.STUDENT);
        return ResponseEntity.ok().body("Student approved successfully");
    }

    @DeleteMapping("/students/reject/{id}")
    public ResponseEntity<?> rejectStudent(@PathVariable Long id) {
        adminApprovalService.reject(id, Role.STUDENT);
        return ResponseEntity.ok().body("Student rejected successfully");
    }

    // ================= TRAINER =================
    @GetMapping("/trainers/pending")
    public List<PendingUserResponse> pendingTrainers() {
        return adminApprovalService.getPending(Role.TRAINER);
    }

    @PutMapping("/trainers/approve/{id}")
    public ResponseEntity<?> approveTrainer(@PathVariable Long id) {
        adminApprovalService.approve(id, Role.TRAINER);
        return ResponseEntity.ok().body("Trainer approved successfully");
    }

    @DeleteMapping("/trainers/reject/{id}")
    public ResponseEntity<?> rejectTrainer(@PathVariable Long id) {
        adminApprovalService.reject(id, Role.TRAINER);
        return ResponseEntity.ok().body("Trainer rejected successfully");
    }

    // ================= BUSINESS =================
    @GetMapping("/business/pending")
    public List<PendingUserResponse> pendingBusiness() {
        return adminApprovalService.getPending(Role.BUSINESS);
    }

    @PutMapping("/business/approve/{id}")
    public ResponseEntity<?> approveBusiness(@PathVariable Long id) {
        adminApprovalService.approve(id, Role.BUSINESS);
        return ResponseEntity.ok().body("Business approved successfully");
    }

    @DeleteMapping("/business/reject/{id}")
    public ResponseEntity<?> rejectBusiness(@PathVariable Long id) {
        adminApprovalService.reject(id, Role.BUSINESS);
        return ResponseEntity.ok().body("Business rejected successfully");
    }

    // ================= ADMIN =================
    @GetMapping("/admins/pending")
    public List<PendingUserResponse> pendingAdmins() {
        return adminApprovalService.getPending(Role.ADMIN);
    }

    @PutMapping("/admins/approve/{id}")
    public ResponseEntity<?> approveAdmin(@PathVariable Long id) {
        adminApprovalService.approve(id, Role.ADMIN);
        return ResponseEntity.ok().body("Admin approved successfully");
    }

    @DeleteMapping("/admins/reject/{id}")
    public ResponseEntity<?> rejectAdmin(@PathVariable Long id) {
        adminApprovalService.reject(id, Role.ADMIN);
        return ResponseEntity.ok().body("Admin rejected successfully");
    }
}
