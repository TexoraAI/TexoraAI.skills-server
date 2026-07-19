//package com.lms.assessment.controller;
//
//import com.lms.assessment.dto.FileUploadResponse;
//import com.lms.assessment.service.AssignmentFileService;
//import org.springframework.core.io.Resource;
//import org.springframework.core.io.UrlResource;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.access.prepost.PreAuthorize;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.IOException;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/assignment-files")
//public class AssignmentFileController {
//
//    private final AssignmentFileService service;
//
//    public AssignmentFileController(AssignmentFileService service) {
//        this.service = service;
//    }
//
//    // 🔵 Upload file (Trainer only)
//    @PreAuthorize("hasRole('TRAINER')")
//    @PostMapping("/{assignmentId}")
//    public ResponseEntity<FileUploadResponse> upload(
//            @PathVariable Long assignmentId,
//            @RequestParam("file") MultipartFile file) throws IOException {
//
//        return ResponseEntity.ok(service.uploadFile(assignmentId, file));
//    }
//
//    // 🔵 Get all files of assignment
//    @GetMapping("/{assignmentId}")
//    public ResponseEntity<List<FileUploadResponse>> getFiles(
//            @PathVariable Long assignmentId) {
//
//        return ResponseEntity.ok(service.getFilesByAssignment(assignmentId));
//    }
//
//    // 🔵 Download file
//    @GetMapping("/download")
//    public ResponseEntity<Resource> download(@RequestParam String path) throws IOException {
//
//        Path filePath = Paths.get("uploads").resolve(path);
//        Resource resource = new UrlResource(filePath.toUri());
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "attachment; filename=\"" + resource.getFilename() + "\"")
//                .body(resource);
//    }
//}
package com.lms.assessment.controller;

import com.lms.assessment.constants.AssessmentFeatureKeys;
import com.lms.assessment.dto.FileUploadResponse;
import com.lms.assessment.service.AssessmentFeatureFlagsService;
import com.lms.assessment.service.AssignmentFileService;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.List;

@RestController
@RequestMapping("/api/assignment-files")
public class AssignmentFileController {

    private final AssignmentFileService service;
    private final AssessmentFeatureFlagsService featureFlagsService;

    public AssignmentFileController(AssignmentFileService service,
                                    AssessmentFeatureFlagsService featureFlagsService) {
        this.service = service;
        this.featureFlagsService = featureFlagsService;
    }

    // 🔵 Upload file (Trainer only)
    @PreAuthorize("hasRole('TRAINER')")
    @PostMapping("/{assignmentId}")
    public ResponseEntity<FileUploadResponse> upload(
            @PathVariable Long assignmentId,
            @RequestParam("file") MultipartFile file,
            HttpServletRequest httpRequest) throws IOException {

        featureFlagsService.enforce(orgId(httpRequest), callerEmail(), AssessmentFeatureKeys.CREATE_ASSIGNMENT);
        return ResponseEntity.ok(service.uploadFile(assignmentId, file));
    }

    // 🔵 Get all files of assignment
    @GetMapping("/{assignmentId}")
    public ResponseEntity<List<FileUploadResponse>> getFiles(
            @PathVariable Long assignmentId) {

        return ResponseEntity.ok(service.getFilesByAssignment(assignmentId));
    }

    // 🔵 Download file
    @GetMapping("/download")
    public ResponseEntity<Resource> download(@RequestParam String path) throws IOException {

        Path filePath = Paths.get("uploads").resolve(path);
        Resource resource = new UrlResource(filePath.toUri());

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + resource.getFilename() + "\"")
                .body(resource);
    }

    // 🏢 Pulled from the JwtFilter's request attribute — never from the request body.
    private String orgId(HttpServletRequest request) {
        return (String) request.getAttribute("organizationId");
    }

    // ── Feature-flag helper: email — no Authentication param exists on this endpoint ──
    private String callerEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null ? auth.getName() : null;
    }
}