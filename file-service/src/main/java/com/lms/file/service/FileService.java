

package com.lms.file.service;

import com.lms.file.constants.FileTierLimits;
import com.lms.file.constants.FileTierResolver;
import com.lms.file.dto.UploadQuotaResponse;
import com.lms.file.exception.FileCountLimitExceededException;
import com.lms.file.exception.FileSizeLimitExceededException;
import com.lms.file.exception.FileStorageLimitExceededException;
import com.lms.file.kafka.FileEventProducer;
import com.lms.file.model.BatchTrainer;
import com.lms.file.model.FileClassroomAccess;
import com.lms.file.model.FileResource;
import com.lms.file.repository.BatchTrainerRepository;
import com.lms.file.repository.FileClassroomAccessRepository;
import com.lms.file.repository.FileRepository;
import com.lms.file.security.SecurityUtils;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

@Service
public class FileService {

    // NEW — S3 key prefix for this service, e.g. "files/file-service-files/"
    @Value("${file.s3-prefix}")
    private String s3Prefix;

    // NEW — how long presigned URLs stay valid
    @Value("${file.presign-expiry-minutes:15}")
    private long presignExpiryMinutes;

    private final FileRepository repo;
    private final FileClassroomAccessRepository accessRepo;
    private final FileEventProducer producer;
    private final BatchTrainerRepository trainerRepo;
    private final S3Service s3Service; // NEW — replaces local disk I/O
    private final FileTierResolver fileTierResolver; // NEW — plan-tier resolution

    public FileService(FileRepository repo,
                       FileClassroomAccessRepository accessRepo,
                       FileEventProducer producer,
                       BatchTrainerRepository trainerRepo,
                       S3Service s3Service,
                       FileTierResolver fileTierResolver) { // NEW param
        this.repo = repo;
        this.accessRepo = accessRepo;
        this.producer = producer;
        this.trainerRepo = trainerRepo;
        this.s3Service = s3Service;
        this.fileTierResolver = fileTierResolver;
    }

    // ================================================================
    // Multi-tenancy validation helper — unchanged.
    // ================================================================
    private void validateOrganizationAccess(Long batchId) {
        String currentOrgId = SecurityUtils.getCurrentOrganizationId();

        if (currentOrgId == null) {
            return;
        }
        if (batchId == null) {
            return;
        }

        String batchOrgId = trainerRepo.findByBatchId(batchId)
                .map(BatchTrainer::getOrganizationId)
                .orElse(null);

        if (batchOrgId == null || !batchOrgId.equals(currentOrgId)) {
            throw new RuntimeException("Cross-organization access denied");
        }
    }

    // ================================================================
    // Plan-tier enforcement — mirrors video-service's final, fixed version.
    // replacingSize is 0 for a brand-new upload, or the size of the file
    // being overwritten in editFile() so that swap doesn't double-count
    // against storage capacity or trip the file-count check.
    // ================================================================
    private void enforceUploadLimits(long newFileSize, String organizationId,
                                      String email, long replacingSize) {
        String tier = fileTierResolver.resolveTier(organizationId, email);

        long maxSize = FileTierLimits.maxFileSizeFor(tier);
        if (newFileSize > maxSize) {
            throw new FileSizeLimitExceededException(newFileSize, maxSize, tier);
        }

        long currentUsage = repo.sumStorageUsage(organizationId, email) - replacingSize;
        long capacity = FileTierLimits.storageCapFor(tier);
        if (currentUsage + newFileSize > capacity) {
            throw new FileStorageLimitExceededException(currentUsage, newFileSize, capacity, tier);
        }

        if (replacingSize == 0) {
            long currentCount = repo.countFiles(organizationId, email);
            int maxCount = FileTierLimits.maxFileCountFor(tier);
            if (currentCount + 1 > maxCount) {
                throw new FileCountLimitExceededException((int) currentCount, maxCount, tier);
            }
        }
    }

    // ================= TRAINER UPLOAD =================
    public FileResource upload(MultipartFile file, Long batchId, String title,
                               String description, Long courseId, String category,
                               String status) throws Exception {

        String trainerEmail = SecurityContextHolder
                .getContext().getAuthentication().getName();

        String organizationId = SecurityUtils.getCurrentOrganizationId();
        enforceUploadLimits(file.getSize(), organizationId, trainerEmail, 0L);

        if (batchId != null) {
            boolean allowed = accessRepo
                    .findByTrainerEmailAndBatchId(trainerEmail, batchId)
                    .size() > 0;
            if (!allowed)
                throw new RuntimeException("You are not assigned to this batch");

            validateOrganizationAccess(batchId);
        }

        // ── S3 upload replaces local disk write ──
        String storedKey = s3Prefix + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        s3Service.uploadFile(storedKey, file);

        FileResource fr = new FileResource();
        fr.setOriginalName(file.getOriginalFilename());
        fr.setStoredName(storedKey);   // now holds the S3 key, not a local filename
        fr.setTitle(title);
        fr.setDescription(description);
        fr.setCategory(category);
        fr.setCourseId(courseId);
        fr.setContentType(file.getContentType());
        fr.setSize(file.getSize());
        fr.setUploadedAt(Instant.now());
        fr.setBatchId(batchId);
        fr.setTrainerEmail(trainerEmail);
        fr.setStatus(status != null ? status : "draft");

        FileResource saved = repo.save(fr);

        if (batchId != null && "published".equals(status)) {
            try {
                producer.sendFileUploadedEvent(saved.getId(), saved.getTitle(),
                        saved.getBatchId(), saved.getTrainerEmail());
            } catch (Exception ignored) {}
        }

        return saved;
    }

    /**
     * Edit an existing file resource.
     * - newFile is OPTIONAL: if null, the existing S3 object is kept.
     * - All metadata fields are always updated.
     */
    public FileResource editFile(
            Long fileId,
            MultipartFile newFile,
            String title,
            String description,
            Long batchId,
            Long courseId,
            String category,
            String status
    ) throws Exception {

        String trainerEmail = SecurityContextHolder
                .getContext().getAuthentication().getName()
                .trim().toLowerCase();

        String organizationId = SecurityUtils.getCurrentOrganizationId();

        FileResource fr = repo.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + fileId));

        if (!fr.getTrainerEmail().equalsIgnoreCase(trainerEmail)) {
            throw new RuntimeException("Not your file");
        }

        if (batchId != null) {
            boolean allowed = accessRepo
                    .findByTrainerEmailAndBatchId(trainerEmail, batchId)
                    .size() > 0;
            if (!allowed) {
                throw new RuntimeException("You are not assigned to this batch");
            }

            validateOrganizationAccess(batchId);
        }

        // ── Replace S3 object only when a new one is provided ──
        if (newFile != null && !newFile.isEmpty()) {
            long oldSize = fr.getSize();
            enforceUploadLimits(newFile.getSize(), organizationId, trainerEmail, oldSize);

            if (fr.getStoredName() != null && !fr.getStoredName().isBlank()) {
                try {
                    s3Service.deleteFile(fr.getStoredName());
                } catch (Exception e) {
                    System.out.println("Could not delete old S3 object: " + e.getMessage());
                }
            }

            String storedKey = s3Prefix + System.currentTimeMillis() + "_" + newFile.getOriginalFilename();
            s3Service.uploadFile(storedKey, newFile);

            fr.setOriginalName(newFile.getOriginalFilename());
            fr.setStoredName(storedKey);
            fr.setSize(newFile.getSize());
            fr.setContentType(newFile.getContentType());
        }

        fr.setTitle(title != null ? title : fr.getTitle());
        fr.setDescription(description != null ? description : "");
        fr.setBatchId(batchId);
        fr.setCourseId(courseId);
        fr.setCategory(category != null ? category : "");
        fr.setStatus(status != null ? status : fr.getStatus());

        FileResource saved = repo.save(fr);

        if (batchId != null && "published".equals(saved.getStatus())) {
            try {
                producer.sendFileUploadedEvent(saved.getId(), saved.getTitle(),
                        saved.getBatchId(), saved.getTrainerEmail());
            } catch (Exception ignored) {}
        }

        return saved;
    }

    // ================= PUBLISH FILE =================
    public FileResource publishFile(Long fileId) {
        String trainerEmail = SecurityContextHolder
                .getContext().getAuthentication().getName();

        FileResource fr = repo.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!fr.getTrainerEmail().equals(trainerEmail))
            throw new RuntimeException("Not your file");

        if (fr.getBatchId() == null)
            throw new RuntimeException("Assign a batch before publishing");

        validateOrganizationAccess(fr.getBatchId());

        fr.setStatus("published");
        FileResource saved = repo.save(fr);

        try {
            producer.sendFileUploadedEvent(saved.getId(), saved.getTitle(),
                    saved.getBatchId(), saved.getTrainerEmail());
        } catch (Exception ignored) {}

        return saved;
    }

    // ================= ASSIGN BATCH =================
    public FileResource assignBatch(Long fileId, Long batchId) {
        String trainerEmail = SecurityContextHolder
                .getContext().getAuthentication().getName();

        FileResource fr = repo.findById(fileId)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!fr.getTrainerEmail().equals(trainerEmail))
            throw new RuntimeException("Not your file");

        boolean allowed = accessRepo
                .findByTrainerEmailAndBatchId(trainerEmail, batchId)
                .size() > 0;
        if (!allowed)
            throw new RuntimeException("You are not assigned to this batch");

        validateOrganizationAccess(batchId);

        fr.setBatchId(batchId);
        fr.setStatus("published");
        FileResource saved = repo.save(fr);

        try {
            producer.sendFileUploadedEvent(saved.getId(), saved.getTitle(),
                    saved.getBatchId(), saved.getTrainerEmail());
        } catch (Exception ignored) {}

        return saved;
    }

    // ================= TRAINER FILES =================
    public List<FileResource> getTrainerFiles() {
        String trainerEmail = SecurityContextHolder
                .getContext().getAuthentication().getName();
        return repo.findByTrainerEmail(trainerEmail);
    }

    // ================= STUDENT FILES — only published =================
    public List<FileResource> getStudentFiles() {
        String studentEmail = SecurityContextHolder
                .getContext().getAuthentication().getName()
                .trim().toLowerCase();

        String currentOrgId = SecurityUtils.getCurrentOrganizationId();

        Optional<FileClassroomAccess> accessOpt = accessRepo.findByStudentEmail(studentEmail);

        if (accessOpt.isEmpty()) return Collections.emptyList();

        FileClassroomAccess access = accessOpt.get();

        if (currentOrgId != null) {
            String accessOrgId = access.getOrganizationId();
            if (accessOrgId == null || !accessOrgId.equals(currentOrgId)) {
                return Collections.emptyList();
            }
        }

        Long batchId = access.getBatchId();
        List<FileResource> all = repo.findByBatchIdInAndStatusOrderByUploadedAtDesc(List.of(batchId), "published");

        String tier = fileTierResolver.resolveTier(currentOrgId, studentEmail);
        int visibleCap = FileTierLimits.studentVisibleCountFor(tier);
        if (visibleCap == -1) return all;
        return all.stream().limit(visibleCap).toList();
    }

    // ================= NEW — plan-tier upload quota snapshot =================
    public UploadQuotaResponse getUploadQuota(String organizationId, String email) {
        String tier = fileTierResolver.resolveTier(organizationId, email);

        long storageUsed = repo.sumStorageUsage(organizationId, email);
        long storageCap = FileTierLimits.storageCapFor(tier);
        long fileCount = repo.countFiles(organizationId, email);
        int maxFileCount = FileTierLimits.maxFileCountFor(tier);
        long maxFileSize = FileTierLimits.maxFileSizeFor(tier);

        return new UploadQuotaResponse(tier, storageUsed, storageCap, fileCount, maxFileCount, maxFileSize);
    }

    // ================= DOWNLOAD (bytes, still supported) =================
    public byte[] download(String storedName) throws Exception {
        return s3Service.downloadFile(storedName);
    }

    // ================= NEW — presigned URL for view/download redirects =================
    public String getPresignedUrl(String storedName) {
        return s3Service.generatePresignedUrl(storedName, Duration.ofMinutes(presignExpiryMinutes));
    }

    // ================= DELETE =================
    public void delete(Long id) throws Exception {
        String trainerEmail = SecurityContextHolder
                .getContext().getAuthentication().getName();

        FileResource fr = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found"));

        if (!fr.getTrainerEmail().equals(trainerEmail))
            throw new RuntimeException("You can delete only your files");

        s3Service.deleteFile(fr.getStoredName());
        repo.delete(fr);
    }

    public FileResource getById(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("File not found with id: " + id));
    }

    public byte[] viewFile(Long id) throws Exception {
        FileResource file = getById(id);
        return s3Service.downloadFile(file.getStoredName());
    }

    // ================= ADMIN: ALL FILES (org-scoped) =================
    public List<FileResource> getAllFilesForAdmin() {
        String currentOrgId = SecurityUtils.getCurrentOrganizationId();

        if (currentOrgId == null) {
            return repo.findAll();
        }

        List<Long> batchIds = trainerRepo.findByOrganizationId(currentOrgId)
                .stream()
                .map(BatchTrainer::getBatchId)
                .distinct()
                .toList();

        if (batchIds.isEmpty()) {
            return Collections.emptyList();
        }

        return repo.findByBatchIdIn(batchIds);
    }
    
    
 // ✅ NEW — companion to getStudentFiles(): reports the TRUE total
    // (before the tier cap) so the frontend can render an "Upgrade to
    // unlock N more" tile instead of silently stopping.
    public java.util.Map<String, Object> getStudentFileCount(String organizationId, String email) {
        String studentEmail = (email != null) ? email.trim().toLowerCase() : null;

        Optional<FileClassroomAccess> accessOpt = accessRepo.findByStudentEmail(studentEmail);
        if (accessOpt.isEmpty()) {
            return java.util.Map.of("totalCount", 0, "visibleCount", 0, "tier", "free", "unlimited", false);
        }

        FileClassroomAccess access = accessOpt.get();
        if (organizationId != null) {
            String accessOrgId = access.getOrganizationId();
            if (accessOrgId == null || !accessOrgId.equals(organizationId)) {
                return java.util.Map.of("totalCount", 0, "visibleCount", 0, "tier", "free", "unlimited", false);
            }
        }

        Long batchId = access.getBatchId();
        int totalCount = repo.findByBatchIdInAndStatusOrderByUploadedAtDesc(List.of(batchId), "published").size();

        String tier = fileTierResolver.resolveTier(organizationId, studentEmail);
        int visibleCap = FileTierLimits.studentVisibleCountFor(tier);
        int visibleCount = visibleCap == -1 ? totalCount : Math.min(totalCount, visibleCap);

        return java.util.Map.of(
                "totalCount", totalCount,
                "visibleCount", visibleCount,
                "tier", tier,
                "unlimited", visibleCap == -1
        );
    }
}
