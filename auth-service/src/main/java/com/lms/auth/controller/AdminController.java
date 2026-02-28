package com.lms.auth.controller;

import com.lms.auth.dto.AdminApplyRequest;
import com.lms.auth.dto.AdminResponse;
import com.lms.auth.service.AdminService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/admin")
public class AdminController {

    private final AdminService adminService;

    public AdminController(AdminService adminService) {
        this.adminService = adminService;
    }

    @PostMapping(value = "/apply", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<?> applyAdmin(
            @RequestPart("data") String data,
            @RequestPart("idProof") MultipartFile idProof,
            @RequestPart("appointmentLetter") MultipartFile appointmentLetter
    ) {
        System.out.println("DATA = " + data);
        System.out.println("ID Proof = " + idProof.getOriginalFilename());
        System.out.println("Appointment = " + appointmentLetter.getOriginalFilename());

        return ResponseEntity.ok("OK");
    }



    // ✅ PENDING ADMINS
    @GetMapping("/pending")
    public ResponseEntity<List<AdminResponse>> pendingAdmins() {
        return ResponseEntity.ok(adminService.getPendingAdmins());
    }

    // ✅ APPROVE ADMIN
    @PutMapping("/approve/{id}")
    public ResponseEntity<Map<String, String>> approve(@PathVariable Long id) {
        adminService.approveAdmin(id);
        return ResponseEntity.ok(Map.of("message", "Admin approved successfully"));
    }

    // ✅ REJECT ADMIN
    @PutMapping("/reject/{id}")
    public ResponseEntity<Map<String, String>> reject(@PathVariable Long id) {
        adminService.rejectAdmin(id);
        return ResponseEntity.ok(Map.of("message", "Admin rejected successfully"));
    }
}
