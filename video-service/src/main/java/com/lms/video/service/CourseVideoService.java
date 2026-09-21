
package com.lms.video.service;

import com.lms.video.constants.CourseContentTierLimits;
import com.lms.video.constants.VideoTierResolver;
import com.lms.video.exception.VideoSizeLimitExceededException;
import com.lms.video.exception.VideoStorageLimitExceededException;
import com.lms.video.model.CourseVideo;
import com.lms.video.model.TranscriptSourceType;
import com.lms.video.repository.CourseVideoRepository;
import com.lms.video.repository.FeaturedTranscriptSegmentRepository;
import com.lms.video.repository.FeaturedVideoTranscriptRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;

@Service
public class CourseVideoService {

    private final CourseVideoRepository repo;
    private final TranscriptGenerationService transcriptGenerationService;
    private final FeaturedVideoTranscriptRepository transcriptRepo;
    private final FeaturedTranscriptSegmentRepository segmentRepo;
    private final S3Service s3Service; // ✅ NEW
    private final VideoTierResolver videoTierResolver; // NEW — reused bean from normal-upload task

    // ✅ NEW — all course-module videos live under this S3 prefix, keeping
    // them separate from library videos (videos/), featured videos
    // (videos/featured-videos/) and watch-now videos (videos/watchnow-videos/).
    private static final String S3_KEY_PREFIX = "videos/course-videos/";

    public CourseVideoService(CourseVideoRepository repo,
            TranscriptGenerationService transcriptGenerationService,
            FeaturedVideoTranscriptRepository transcriptRepo,
            FeaturedTranscriptSegmentRepository segmentRepo,
            S3Service s3Service, // ✅ NEW
            VideoTierResolver videoTierResolver) { // NEW param
        this.repo = repo;
        this.transcriptGenerationService = transcriptGenerationService;
        this.transcriptRepo = transcriptRepo;
        this.segmentRepo = segmentRepo;
        this.s3Service = s3Service; // ✅ NEW
        this.videoTierResolver = videoTierResolver; // NEW
    }

    // ================= PLAN ENFORCEMENT (course-video pool) =================
    // WHY: separate limit pool from VideoTierLimits/normal uploads — uses
    // CourseContentTierLimits instead. organizationId is passed through from
    // the controller (JWT-derived) so org-purchased plans apply here too,
    // consistent with the rest of the system.
    private void enforceCourseVideoLimits(long newFileSize, String email, String organizationId) {
        String tier = videoTierResolver.resolveTier(organizationId, email);

        long maxSize = CourseContentTierLimits.maxSizeFor(tier);
        if (newFileSize > maxSize) {
            throw new VideoSizeLimitExceededException(newFileSize, maxSize, tier);
        }

        long currentUsage = repo.sumStorageUsageByTrainer(email);
        long capacity = CourseContentTierLimits.storageCapFor(tier);
        if (currentUsage + newFileSize > capacity) {
            throw new VideoStorageLimitExceededException(currentUsage, newFileSize, capacity, tier);
        }
    }

    // ================= UPLOAD =================
    public CourseVideo upload(
            MultipartFile file,
            Long courseId,
            Long moduleId,
            Long batchId,
            String email,
            String organizationId // NEW param
    ) throws IOException {

        // NEW — first line, before S3 upload
        enforceCourseVideoLimits(file.getSize(), email, organizationId);

        // ✅ CHANGED — video goes straight to S3, no local disk involved
        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        String s3Key = S3_KEY_PREFIX + fileName;
        s3Service.uploadFile(s3Key, file);

        // ✅ NEW — temporary secure link so ffmpeg can read the file directly from S3
        String presignedUrl = s3Service.generatePresignedUrl(s3Key, Duration.ofHours(2));

        CourseVideo video = new CourseVideo();
        video.setCourseId(courseId);
        video.setModuleId(moduleId);
        video.setBatchId(batchId);
        video.setFileName(fileName); // unchanged shape — just the base name, prefix is added when talking to S3
        video.setUrl("http://localhost:9000/api/course-videos/stream/" + fileName);
        video.setUploadedBy(email);
        video.setSize(file.getSize()); // NEW

        CourseVideo saved = repo.save(video);

        try {
            // ✅ CHANGED — pass the presigned S3 URL instead of a local file path
            transcriptGenerationService.generateAsync(
                    saved.getId(), presignedUrl, TranscriptSourceType.COURSE_VIDEO);
        } catch (Exception ignored) {
            // transcript kickoff failures must never affect the upload response
        }

        return saved;
    }

    // ================= EDIT (replace file + keep metadata) =================
    public CourseVideo update(
            Long id,
            MultipartFile newFile,   // nullable — if null, keep existing file
            Long courseId,
            Long moduleId,
            Long batchId,
            String email,
            String organizationId // NEW param
    ) throws IOException {

        CourseVideo existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course video not found: " + id));

        if (newFile != null && !newFile.isEmpty()) {

            // NEW — replacing-size-aware check: subtract the file being replaced
            // from current usage before checking the new file against capacity,
            // same approach as the normal-upload fix.
            String tier = videoTierResolver.resolveTier(organizationId, email);

            long maxSize = CourseContentTierLimits.maxSizeFor(tier);
            if (newFile.getSize() > maxSize) {
                throw new VideoSizeLimitExceededException(newFile.getSize(), maxSize, tier);
            }

            long existingSize = existing.getSize();
            long currentUsage = repo.sumStorageUsageByTrainer(email) - existingSize;
            long capacity = CourseContentTierLimits.storageCapFor(tier);
            if (currentUsage + newFile.getSize() > capacity) {
                throw new VideoStorageLimitExceededException(currentUsage, newFile.getSize(), capacity, tier);
            }

            // ✅ CHANGED — delete old file from S3 (best-effort), not local disk
            if (existing.getFileName() != null && !existing.getFileName().isBlank()) {
                try {
                    s3Service.deleteFile(S3_KEY_PREFIX + existing.getFileName());
                } catch (Exception e) {
                    System.out.println("Could not delete old course video from S3: " + e.getMessage());
                }
            }

            // ✅ CHANGED — new file goes straight to S3, no local disk involved
            String newFileName = System.currentTimeMillis() + "_" + newFile.getOriginalFilename();
            String newS3Key = S3_KEY_PREFIX + newFileName;
            s3Service.uploadFile(newS3Key, newFile);

            // ✅ NEW — temporary secure link so ffmpeg can read the new file
            String presignedUrl = s3Service.generatePresignedUrl(newS3Key, Duration.ofHours(2));

            existing.setFileName(newFileName);
            existing.setUrl("http://localhost:9000/api/course-videos/stream/" + newFileName);
            existing.setSize(newFile.getSize()); // NEW

            // New file content means the old transcript no longer matches —
            // delete stale transcript/segments and kick off a fresh job.
            transcriptRepo.findBySessionIdAndSourceType(id, TranscriptSourceType.COURSE_VIDEO)
                    .ifPresent(t -> {
                        segmentRepo.deleteAll(segmentRepo.findByTranscriptIdOrderByOrderIndexAsc(t.getId()));
                        transcriptRepo.delete(t);
                    });

            try {
                // ✅ CHANGED — pass the presigned S3 URL instead of a local file path
                transcriptGenerationService.generateAsync(
                        id, presignedUrl, TranscriptSourceType.COURSE_VIDEO);
            } catch (Exception ignored) {
                // transcript kickoff failures must never affect the edit response
            }
        }

        // Update metadata (only if non-null values are provided)
        if (courseId != null)  existing.setCourseId(courseId);
        if (moduleId != null)  existing.setModuleId(moduleId);
        if (batchId  != null)  existing.setBatchId(batchId);
        if (email    != null)  existing.setUploadedBy(email);

        return repo.save(existing);
    }

    // ================= DELETE =================
    public void deleteById(Long id) {

        CourseVideo video = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course video not found: " + id));

        // ✅ CHANGED — delete from S3 instead of local disk
        if (video.getFileName() != null && !video.getFileName().isBlank()) {
            try {
                s3Service.deleteFile(S3_KEY_PREFIX + video.getFileName());
            } catch (Exception e) {
                System.out.println("Could not delete course video from S3: " + e.getMessage());
            }
        }

        repo.delete(video);

        transcriptRepo.findBySessionIdAndSourceType(id, TranscriptSourceType.COURSE_VIDEO)
                .ifPresent(t -> {
                    segmentRepo.deleteAll(segmentRepo.findByTranscriptIdOrderByOrderIndexAsc(t.getId()));
                    transcriptRepo.delete(t);
                });
    }

    // ================= PLAYBACK URL =================
    public String getPlaybackUrl(String fileName) {
        return s3Service.generatePresignedUrl(S3_KEY_PREFIX + fileName, Duration.ofHours(2));
    }
    
    // ================= USAGE (for quota pill) =================
    public java.util.Map<String, Object> getCourseVideoUsage(String email, String organizationId) {
        String tier = videoTierResolver.resolveTier(organizationId, email);
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