package com.lms.assessment.controller;

import com.lms.assessment.dto.AssignmentResponse;
import com.lms.assessment.dto.CreateAssignmentRequest;
import com.lms.assessment.service.AssignmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/api/assignments")
public class AssignmentController {

    private final AssignmentService service;

    public AssignmentController(AssignmentService service) {
        this.service = service;
    }

    // ================= CREATE =================

    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping
    public ResponseEntity<AssignmentResponse> createAssignment(
            @RequestBody CreateAssignmentRequest request,
            Principal principal) {

        String trainerEmail = principal.getName();

        AssignmentResponse response =
                service.createAssignment(request, trainerEmail);

        return ResponseEntity.ok(response);
    }

    // ================= GET BY BATCH (STUDENT + TRAINER) =================
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/student")
    public ResponseEntity<List<AssignmentResponse>> getStudentAssignments(
            Principal principal) {

        return ResponseEntity.ok(
                service.getStudentAssignments(
                        principal.getName()
                )
        );
    }

    // ================= GET TRAINER ASSIGNMENTS =================

    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/trainer")
    public ResponseEntity<List<AssignmentResponse>> getMyAssignments(
            Principal principal) {

        String trainerEmail = principal.getName();

        return ResponseEntity.ok(
                service.getAssignmentsByTrainer(trainerEmail)
        );
    }

    // ================= UPDATE =================

    @PreAuthorize("hasRole('TRAINER')")
    @PutMapping("/{id}")
    public ResponseEntity<AssignmentResponse> updateAssignment(
            @PathVariable Long id,
            @RequestBody CreateAssignmentRequest request,
            Principal principal) {

        String trainerEmail = principal.getName();

        AssignmentResponse updated =
                service.updateAssignment(id, request, trainerEmail);

        return ResponseEntity.ok(updated);
    }

    // ================= DELETE =================

    @PreAuthorize("hasRole('TRAINER')")
    @DeleteMapping("/{id}")
    public ResponseEntity<String> deleteAssignment(
            @PathVariable Long id,
            Principal principal) {

        String trainerEmail = principal.getName();

        service.deleteAssignment(id, trainerEmail);

        return ResponseEntity.ok("Assignment Deleted Successfully");
    }
}
