package com.lms.auth.controller;

import com.lms.auth.dto.StudentApplyRequest;
import com.lms.auth.dto.StudentResponse;
import com.lms.auth.service.StudentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/student")
public class StudentController {

    private final StudentService studentService;

    public StudentController(StudentService studentService) {
        this.studentService = studentService;
    }

    // ✅ Student Apply
    @PostMapping("/apply")
    public ResponseEntity<Map<String, String>> apply(@RequestBody StudentApplyRequest request) {
        studentService.apply(request);
        return ResponseEntity.ok(Map.of("message", "Student application submitted. Waiting for approval."));
    }

    // ✅ Student check approval status (ApprovalPending.jsx)
    @GetMapping("/status")
    public ResponseEntity<Map<String, Object>> status(@RequestParam String email) {
        boolean approved = studentService.getStatus(email);
        return ResponseEntity.ok(Map.of("approved", approved));
    }

    // ✅ Admin: pending list
    @GetMapping("/pending")
    public ResponseEntity<List<StudentResponse>> pendingStudents() {
        return ResponseEntity.ok(studentService.getPendingStudents());
    }

    // ✅ Admin: approve
    @PutMapping("/approve/{id}")
    public ResponseEntity<Map<String, String>> approve(@PathVariable Long id) {
        studentService.approveStudent(id);
        return ResponseEntity.ok(Map.of("message", "Student approved successfully"));
    }

    // ✅ Admin: reject
    @DeleteMapping("/reject/{id}")
    public ResponseEntity<Map<String, String>> reject(@PathVariable Long id) {
        studentService.rejectStudent(id);
        return ResponseEntity.ok(Map.of("message", "Student rejected successfully"));
    }
}
