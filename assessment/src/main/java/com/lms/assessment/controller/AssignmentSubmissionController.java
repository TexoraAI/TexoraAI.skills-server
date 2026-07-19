//
//package com.lms.assessment.controller;
//
//import com.lms.assessment.dto.SubmissionResponse;
//import com.lms.assessment.service.AssignmentSubmissionService;
//import jakarta.servlet.http.HttpServletRequest;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.UrlResource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.security.core.Authentication;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/submissions")
//public class AssignmentSubmissionController {
//
//    private final AssignmentSubmissionService service;
//
//    public AssignmentSubmissionController(AssignmentSubmissionService service) {
//        this.service = service;
//    }
//
//    // 🔵 Student upload
//    @PreAuthorize("hasRole('STUDENT')")
//    @PostMapping("/{assignmentId}")
//    public ResponseEntity<SubmissionResponse> submit(
//            @PathVariable Long assignmentId,
//            @RequestParam("file") MultipartFile file,
//            Authentication authentication,
//            HttpServletRequest httpRequest) throws IOException {
//        return ResponseEntity.ok(
//                service.submit(assignmentId, file, authentication, orgId(httpRequest)));
//    }
//
//    // 🔵 Trainer view all submissions
////    @PreAuthorize("hasRole('TRAINER')")
//    @PreAuthorize("hasAnyRole('TRAINER', 'TENANT_ADMIN', 'SUPER_ADMIN')")
//    @GetMapping("/{assignmentId}")
//    public ResponseEntity<List<SubmissionResponse>> getSubmissions(
//            @PathVariable Long assignmentId,
//            HttpServletRequest httpRequest) {
//        return ResponseEntity.ok(service.getByAssignment(assignmentId, orgId(httpRequest)));
//    }
//
//    // 🔵 Trainer evaluate submission (give marks)
//    @PreAuthorize("hasRole('TRAINER')")
//    @PutMapping("/evaluate/{submissionId}")
//    public ResponseEntity<SubmissionResponse> evaluateSubmission(
//            @PathVariable Long submissionId,
//            @RequestParam Integer marks,
//            HttpServletRequest httpRequest) {
//        return ResponseEntity.ok(
//                service.evaluateSubmission(submissionId, marks, orgId(httpRequest))
//        );
//    }
//
//    // 🔵 Student get his submissions (marks)
//    @PreAuthorize("hasRole('STUDENT')")
//    @GetMapping("/my")
//    public ResponseEntity<List<SubmissionResponse>> getMySubmissions(
//            Authentication authentication,
//            HttpServletRequest httpRequest) {
//        return ResponseEntity.ok(service.getMySubmissions(authentication, orgId(httpRequest)));
//    }
//
//    // 🔵 Download
//    @GetMapping("/download")
//    public ResponseEntity<Resource> download(@RequestParam String path) throws IOException {
//        Path filePath = Paths.get("uploads").resolve(path);
//        Resource resource = new UrlResource(filePath.toUri());
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename=\"" + resource.getFilename() + "\"")
//                .body(resource);
//    }
//
//    private String orgId(HttpServletRequest request) {
//        return (String) request.getAttribute("organizationId");
//    }
//}

package com.lms.assessment.controller;

import com.lms.assessment.constants.AssessmentFeatureKeys;
import com.lms.assessment.dto.SubmissionResponse;
import com.lms.assessment.service.AssessmentFeatureFlagsService;
import com.lms.assessment.service.AssignmentSubmissionService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
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
    private final AssessmentFeatureFlagsService featureFlagsService;

    public AssignmentSubmissionController(AssignmentSubmissionService service,
                                          AssessmentFeatureFlagsService featureFlagsService) {
        this.service = service;
        this.featureFlagsService = featureFlagsService;
    }

    // 🔵 Student upload
    @PreAuthorize("hasRole('STUDENT')")
    @PostMapping("/{assignmentId}")
    public ResponseEntity<SubmissionResponse> submit(
            @PathVariable Long assignmentId,
            @RequestParam("file") MultipartFile file,
            Authentication authentication,
            HttpServletRequest httpRequest) throws IOException {
        featureFlagsService.enforce(orgId(httpRequest), authentication.getName(), AssessmentFeatureKeys.SUBMIT_ASSIGNMENT);
        return ResponseEntity.ok(
                service.submit(assignmentId, file, authentication, orgId(httpRequest)));
    }

    // 🔵 Trainer view all submissions
//    @PreAuthorize("hasRole('TRAINER')")
    @PreAuthorize("hasAnyRole('TRAINER', 'TENANT_ADMIN', 'SUPER_ADMIN')")
    @GetMapping("/{assignmentId}")
    public ResponseEntity<List<SubmissionResponse>> getSubmissions(
            @PathVariable Long assignmentId,
            HttpServletRequest httpRequest) {
        // Only enforce when the caller is actually the TRAINER; ADMIN/TENANT_ADMIN/SUPER_ADMIN
        // viewing this as an org report must never be blocked by the trainer-facing flag.
        if (!isAdminCaller()) {
            featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.CREATE_ASSIGNMENT);
        }
        return ResponseEntity.ok(service.getByAssignment(assignmentId, orgId(httpRequest)));
    }

    // 🔵 Trainer evaluate submission (give marks)
    @PreAuthorize("hasRole('TRAINER')")
    @PutMapping("/evaluate/{submissionId}")
    public ResponseEntity<SubmissionResponse> evaluateSubmission(
            @PathVariable Long submissionId,
            @RequestParam Integer marks,
            HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.CREATE_ASSIGNMENT);
        return ResponseEntity.ok(
                service.evaluateSubmission(submissionId, marks, orgId(httpRequest))
        );
    }

    // 🔵 Student get his submissions (marks)
    @PreAuthorize("hasRole('STUDENT')")
    @GetMapping("/my")
    public ResponseEntity<List<SubmissionResponse>> getMySubmissions(
            Authentication authentication,
            HttpServletRequest httpRequest) {
        featureFlagsService.enforce(orgId(httpRequest), authentication.getName(), AssessmentFeatureKeys.SUBMIT_ASSIGNMENT);
        return ResponseEntity.ok(service.getMySubmissions(authentication, orgId(httpRequest)));
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

    private String orgId(HttpServletRequest request) {
        return (String) request.getAttribute("organizationId");
    }

    // ── Feature-flag role helpers ─────────────────────────────────────────────
    private String callerEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }

    private boolean isAdminCaller() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null) return false;
        for (GrantedAuthority ga : auth.getAuthorities()) {
            String r = ga.getAuthority();
            if (r.equals("ROLE_ADMIN") || r.equals("ROLE_TENANT_ADMIN") || r.equals("ROLE_SUPER_ADMIN")) {
                return true;
            }
        }
        return false;
    }
}