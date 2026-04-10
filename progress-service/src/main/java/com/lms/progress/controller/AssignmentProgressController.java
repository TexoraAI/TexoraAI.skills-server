package com.lms.progress.controller;

import com.lms.progress.dto.AssignmentProgressResponse;
import com.lms.progress.service.AssignmentProgressService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/assignment-progress")
public class AssignmentProgressController {

    private final AssignmentProgressService service;

    public AssignmentProgressController(AssignmentProgressService service) {
        this.service = service;
    }

    @GetMapping("/user")
    public AssignmentProgressResponse get(
            @RequestParam String email,
            @RequestParam Long batchId) {

        return service.get(email, batchId);
    }

    @PostMapping("/mark-complete")
    public AssignmentProgressResponse mark(
            @RequestParam String email,
            @RequestParam Long batchId,
            @RequestParam Long assignmentId,
            @RequestParam int totalAssignments) {

        return service.markCompleted(email, batchId, assignmentId, totalAssignments);
    }
}