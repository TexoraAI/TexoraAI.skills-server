package com.lms.assessment.controller;

import com.lms.assessment.dto.SubmissionResponse;
import com.lms.assessment.service.AssignmentSubmissionService;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/submissions")
public class AssignmentSubmissionController {

    private final AssignmentSubmissionService service;

    public AssignmentSubmissionController(AssignmentSubmissionService service) {
        this.service = service;
    }

    // 🔵 Student upload
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{assignmentId}")
    public ResponseEntity<SubmissionResponse> submit(
            @PathVariable Long assignmentId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication) throws IOException {

        return ResponseEntity.ok(
                service.submit(assignmentId, file, authentication));
    }

    // 🔵 Trainer view all submissions
    @PreAuthorize("hasRole('TRAINER')")
    @GetMapping("/{assignmentId}")
    public ResponseEntity<List<SubmissionResponse>> getSubmissions(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(service.getByAssignment(assignmentId));
    }

 // 🔵 Trainer evaluate submission (give marks)
    @PreAuthorize("hasRole('TRAINER')")
    @PutMapping("/evaluate/{submissionId}")
    public ResponseEntity<SubmissionResponse> evaluateSubmission(
            @PathVariable Long submissionId,
            @RequestParam Integer marks) {

        return ResponseEntity.ok(
                service.evaluateSubmission(submissionId, marks)
        );
    }

    
 // 🔵 Student get his submissions (marks)
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my")
    public ResponseEntity<List<SubmissionResponse>> getMySubmissions(Authentication authentication) {
        return ResponseEntity.ok(service.getMySubmissions(authentication));
    }

    
    
    // 🔵 Download
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam String path) throws IOException {

        Path filePath = Paths.get("uploads").resolve(path);
        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }
}
