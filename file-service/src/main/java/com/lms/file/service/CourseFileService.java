
package com.lms.file.service;

import com.lms.file.constants.CourseContentTierLimits;
import com.lms.file.constants.FileTierResolver;
import com.lms.file.exception.FileSizeLimitExceededException;
import com.lms.file.exception.FileStorageLimitExceededException;
import com.lms.file.model.CourseFile;
import com.lms.file.repository.CourseFileRepository;
import com.lms.file.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

@Service
public class CourseFileService {

    private final CourseFileRepository repo;
    private final S3Service s3Service; // NEW — replaces local disk I/O
    private final FileTierResolver fileTierResolver; // NEW — reused bean from normal-upload task

    // NEW — S3 key prefix per your bucket layout:
    // ilmora-media-storage/files/course-service-files/
    @Value("${course-file.s3-prefix}")
    private String s3Prefix;

    // NEW — public gateway base URL, used to build the stored `url` field
    @Value("${gateway.base-url}")
    private String gatewayBaseUrl;

    // NEW — how long presigned URLs stay valid once someone actually hits /download/{id}
    @Value("${course-file.presign-expiry-minutes:15}")
    private long presignExpiryMinutes;

    public CourseFileService(CourseFileRepository repo, S3Service s3Service, FileTierResolver fileTierResolver) { // NEW param
        this.repo = repo;
        this.s3Service = s3Service;
        this.fileTierResolver = fileTierResolver; // NEW
    }

    // ================= PLAN ENFORCEMENT (course-file pool) =================
    // WHY: separate limit pool from FileTierLimits/normal uploads — uses
    // CourseContentTierLimits instead. organizationId is read via
    // SecurityUtils.getCurrentOrganizationId() (static, request-scoped) so
    // org-purchased plans apply here too, consistent with the rest of the system.
    private void enforceCourseFileLimits(long newFileSize, String email) {
        String organizationId = SecurityUtils.getCurrentOrganizationId();
        String tier = fileTierResolver.resolveTier(organizationId, email);

        long maxSize = CourseContentTierLimits.maxSizeFor(tier);
        if (newFileSize > maxSize) {
            throw new FileSizeLimitExceededException(newFileSize, maxSize, tier);
        }

        long currentUsage = repo.sumStorageUsageByTrainer(email);
        long capacity = CourseContentTierLimits.storageCapFor(tier);
        if (currentUsage + newFileSize > capacity) {
            throw new FileStorageLimitExceededException(currentUsage, newFileSize, capacity, tier);
        }
    }

    // ================= UPLOAD =================
    public CourseFile upload(
            MultipartFile file,
            Long courseId,
            Long moduleId,
            Long batchId,
            String email
    ) throws IOException {

        // NEW — first line, before S3 upload
        enforceCourseFileLimits(file.getSize(), email);

        String storedKey = s3Prefix + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        s3Service.uploadFile(storedKey, file);

        CourseFile courseFile = new CourseFile();
        courseFile.setCourseId(courseId);
        courseFile.setModuleId(moduleId);
        courseFile.setBatchId(batchId);
        courseFile.setFileName(storedKey); // now holds the S3 key, not a local filename
        courseFile.setUploadedBy(email);
        courseFile.setSize(file.getSize()); // NEW

        CourseFile saved = repo.save(courseFile);

        // url points at our own gateway endpoint (id-based), never at S3 directly —
        // this way the stored URL never expires; the endpoint always issues a fresh
        // presigned URL at request time.
        saved.setUrl(gatewayBaseUrl + "/api/course-files/download/" + saved.getId());
        return repo.save(saved);
    }

    // ================= EDIT (replace file + keep metadata) =================
    public CourseFile update(
            Long id,
            MultipartFile newFile,
            Long courseId,
            Long moduleId,
            Long batchId,
            String email
    ) throws IOException {
        CourseFile existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course file not found: " + id));

        if (newFile != null && !newFile.isEmpty()) {

            // NEW — replacing-size-aware check: subtract the file being replaced
            // from current usage before checking the new file against capacity.
            String organizationId = SecurityUtils.getCurrentOrganizationId();
            String tier = fileTierResolver.resolveTier(organizationId, email);

            long maxSize = CourseContentTierLimits.maxSizeFor(tier);
            if (newFile.getSize() > maxSize) {
                throw new FileSizeLimitExceededException(newFile.getSize(), maxSize, tier);
            }

            long existingSize = existing.getSize();
            long currentUsage = repo.sumStorageUsageByTrainer(email) - existingSize;
            long capacity = CourseContentTierLimits.storageCapFor(tier);
            if (currentUsage + newFile.getSize() > capacity) {
                throw new FileStorageLimitExceededException(currentUsage, newFile.getSize(), capacity, tier);
            }

            // Delete old S3 object (best-effort)
            if (existing.getFileName() != null && !existing.getFileName().isBlank()) {
                try {
                    s3Service.deleteFile(existing.getFileName());
                } catch (Exception e) {
                    System.out.println("Could not delete old S3 object: " + e.getMessage());
                }
            }

            String newStoredKey = s3Prefix + System.currentTimeMillis() + "_" + newFile.getOriginalFilename();
            s3Service.uploadFile(newStoredKey, newFile);

            existing.setFileName(newStoredKey);
            existing.setSize(newFile.getSize()); // NEW
            // url stays the same (gateway + id) — no need to regenerate it,
            // since it doesn't encode the filename anymore.
        }

        if (courseId != null) existing.setCourseId(courseId);
        if (moduleId != null) existing.setModuleId(moduleId);
        if (batchId  != null) existing.setBatchId(batchId);
        if (email    != null) existing.setUploadedBy(email);

        return repo.save(existing);
    }

    // ================= DELETE =================
    public void deleteById(Long id) {
        CourseFile file = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course file not found: " + id));

        if (file.getFileName() != null && !file.getFileName().isBlank()) {
            s3Service.deleteFile(file.getFileName());
        }
        repo.delete(file);
    }

    // ================= NEW — used by the controller's download/view endpoint =================
    public CourseFile getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course file not found: " + id));
    }

    public String getPresignedUrl(String storedKey) {
        return s3Service.generatePresignedUrl(storedKey, Duration.ofMinutes(presignExpiryMinutes));
    }
    
    
    // ================= USAGE (for quota pill) =================
    public java.util.Map<String, Object> getCourseFileUsage(String email) {
        String organizationId = SecurityUtils.getCurrentOrganizationId();
        String tier = fileTierResolver.resolveTier(organizationId, email);
        long used = repo.sumStorageUsageByTrainer(email);
        long capacity = CourseContentTierLimits.storageCapFor(tier);
        long maxFileSize = CourseContentTierLimits.maxSizeFor(tier);

        return java.util.Map.of(
                "tier", tier,
                "usedBytes", used,
                "capacityBytes", capacity,
                "maxFileSizeBytes", maxFileSize
        );
    }
}