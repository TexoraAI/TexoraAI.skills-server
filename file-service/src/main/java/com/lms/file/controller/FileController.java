
package com.lms.file.controller;

import com.lms.file.constants.FileFeatureKeys;
import com.lms.file.dto.UploadQuotaResponse;
import com.lms.file.model.FileResource;
import com.lms.file.security.SecurityUtils;
import com.lms.file.service.FileFeatureFlagsService;
import com.lms.file.service.FileService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;

@RestController
@RequestMapping("/api/file")
public class FileController {

    private final FileService service;
    private final FileFeatureFlagsService featureFlagsService;

    public FileController(FileService service,
                           FileFeatureFlagsService featureFlagsService) {
        this.service = service;
        this.featureFlagsService = featureFlagsService;
    }

    // ================= TRAINER UPLOAD — batchId now optional =================
    @PostMapping("/upload")
    public FileResource upload(
            @RequestParam MultipartFile file,
            @RequestParam(required = false) Long batchId,
            @RequestParam(required = false) String title,
            @RequestParam(required = false) String description,
            @RequestParam(required = false) Long courseId,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "draft") String status
    ) throws Exception {
        featureFlagsService.enforce(currentOrgId(), currentEmail(), FileFeatureKeys.UPLOAD_FILE);
        return service.upload(file, batchId, title, description, courseId, category, status);
    }

    // ================= TRAINER FILES =================
    @GetMapping("/trainer")
    public List<FileResource> trainerFiles() {
        featureFlagsService.enforce(currentOrgId(), currentEmail(), FileFeatureKeys.GET_TRAINER_FILES);
        return service.getTrainerFiles();
    }

    // ================= STUDENT FILES =================
    @GetMapping("/student")
    public List<FileResource> studentFiles() {
        featureFlagsService.enforce(currentOrgId(), currentEmail(), FileFeatureKeys.GET_STUDENT_FILES);
        return service.getStudentFiles();
    }

    // ================= PLAN-TIER: UPLOAD QUOTA SNAPSHOT =================
    // Read-only, so left ungated — matches the other GET endpoints in this
    // controller that don't call featureFlagsService.enforce(...).
    @GetMapping("/upload-quota")
    public UploadQuotaResponse uploadQuota() {
        return service.getUploadQuota(currentOrgId(), currentEmail());
    }

    // ================= DOWNLOAD =================
    // Returns a JSON payload with a fresh presigned S3 URL — NOT a redirect
    // and NOT the raw bytes — because the frontend calls this with an
    // Authorization header via axios/fetch, and that header would otherwise
    // get forwarded to S3 on a redirect and get rejected.
    @GetMapping("/download/{id}")
    public ResponseEntity<java.util.Map<String, String>> download(@PathVariable Long id) throws Exception {
        featureFlagsService.enforce(currentOrgId(), currentEmail(), FileFeatureKeys.DOWNLOAD_FILE);
        FileResource file = service.getById(id);
        String url = service.getPresignedUrl(file.getStoredName());
        return ResponseEntity.ok(java.util.Map.of(
                "url", url,
                "contentType", file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                "originalName", file.getOriginalName() != null ? file.getOriginalName() : ""
        ));
    }

    // ================= VIEW / PREVIEW =================
    @GetMapping("/view/{id}")
    public ResponseEntity<java.util.Map<String, String>> view(@PathVariable Long id) throws Exception {
        featureFlagsService.enforce(currentOrgId(), currentEmail(), FileFeatureKeys.VIEW_FILE);
        FileResource file = service.getById(id);
        String url = service.getPresignedUrl(file.getStoredName());
        return ResponseEntity.ok(java.util.Map.of(
                "url", url,
                "contentType", file.getContentType() != null ? file.getContentType() : "application/octet-stream",
                "originalName", file.getOriginalName() != null ? file.getOriginalName() : ""
        ));
    }
    // ================= DELETE =================
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) throws Exception {
        featureFlagsService.enforce(currentOrgId(), currentEmail(), FileFeatureKeys.DELETE_FILE);
        service.delete(id);
        return ResponseEntity.noContent().build();
    }

    // ✅ publish a draft file that already has a batch
    @PatchMapping("/{id}/publish")
    public ResponseEntity<FileResource> publishFile(@PathVariable Long id) {
        featureFlagsService.enforce(currentOrgId(), currentEmail(), FileFeatureKeys.PUBLISH_FILE);
        return ResponseEntity.ok(service.publishFile(id));
    }

    // ✅ assign batch to a no-batch draft (also sets published)
    @PatchMapping("/{id}/assign-batch")
    public ResponseEntity<FileResource> assignBatch(
            @PathVariable Long id,
            @RequestParam Long batchId
    ) {
        featureFlagsService.enforce(currentOrgId(), currentEmail(), FileFeatureKeys.ASSIGN_BATCH);
        return ResponseEntity.ok(service.assignBatch(id, batchId));
    }

    @PutMapping(value = "/{id}/edit", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<FileResource> editFile(
            @PathVariable Long id,
            @RequestParam(value = "file",        required = false) MultipartFile file,
            @RequestParam(value = "title",        required = false) String title,
            @RequestParam(value = "description",  defaultValue = "") String description,
            @RequestParam(value = "batchId",      required = false) Long batchId,
            @RequestParam(value = "courseId",     required = false) Long courseId,
            @RequestParam(value = "category",     defaultValue = "") String category,
            @RequestParam(value = "status",       defaultValue = "draft") String status
    ) throws Exception {
        featureFlagsService.enforce(currentOrgId(), currentEmail(), FileFeatureKeys.EDIT_FILE);
        FileResource updated = service.editFile(
                id, file, title, description, batchId, courseId, category, status
        );
        return ResponseEntity.ok(updated);
    }

    // ================= ADMIN: ALL FILES =================
    @GetMapping("/admin/all")
    public List<FileResource> adminAllFiles() {
        String role = SecurityUtils.getCurrentRole();
        if (!"ADMIN".equalsIgnoreCase(role) && !"TENANT_ADMIN".equalsIgnoreCase(role)) {
            throw new RuntimeException("Admin access required");
        }
        featureFlagsService.enforce(currentOrgId(), currentEmail(), FileFeatureKeys.GET_ALL_FILES);
        return service.getAllFilesForAdmin();
    }

    private String currentOrgId() {
        return SecurityUtils.getCurrentOrganizationId();
    }

    private String currentEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || !auth.isAuthenticated()) return null;
        String name = auth.getName();
        return (name == null || "anonymousUser".equals(name)) ? null : name;
    }
    
    

    // ✅ NEW — true total (before tier cap), for "Upgrade to unlock N more" UI
    @GetMapping("/student/count")
    public java.util.Map<String, Object> studentFilesCount() {
        featureFlagsService.enforce(currentOrgId(), currentEmail(), FileFeatureKeys.GET_STUDENT_FILES);
        return service.getStudentFileCount(currentOrgId(), currentEmail());
    }
}

