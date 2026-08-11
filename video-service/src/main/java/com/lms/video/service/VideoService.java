
package com.lms.video.service;
import com.lms.video.model.TranscriptSourceType;
import com.lms.video.model.FeaturedVideoTranscript;
import com.lms.video.repository.FeaturedVideoTranscriptRepository;
import com.lms.video.repository.FeaturedTranscriptSegmentRepository;
import com.lms.video.kafka.VideoProducer;
import com.lms.video.model.Video;
import com.lms.video.model.TrainerBatchMap;
import com.lms.video.repository.VideoRepository;
import com.lms.video.repository.TrainerBatchMapRepository;
import com.lms.video.repository.StudentBatchMapRepository;
import com.lms.video.model.StudentBatchMap;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;

import java.io.IOException;
import java.nio.file.*;
import java.util.List;
import java.util.Collections;
@Service
public class VideoService {

    @Value("${video.upload-dir}")
    private String uploadDir;

    private final VideoRepository repo;
    private final VideoProducer videoProducer;
    private final TrainerBatchMapRepository trainerBatchMapRepository;
    private final StudentBatchMapRepository studentBatchMapRepository;
    private final TranscriptGenerationService transcriptGenerationService;
    private final FeaturedVideoTranscriptRepository transcriptRepo;
    private final FeaturedTranscriptSegmentRepository segmentRepo;


    public VideoService(VideoRepository repo,
            VideoProducer videoProducer,
            TrainerBatchMapRepository trainerBatchMapRepository,
            StudentBatchMapRepository studentBatchMapRepository,
            TranscriptGenerationService transcriptGenerationService,
            FeaturedVideoTranscriptRepository transcriptRepo,
            FeaturedTranscriptSegmentRepository segmentRepo) {
this.repo = repo;
this.videoProducer = videoProducer;
this.trainerBatchMapRepository = trainerBatchMapRepository;
this.studentBatchMapRepository = studentBatchMapRepository;
this.transcriptGenerationService = transcriptGenerationService;
this.transcriptRepo = transcriptRepo;
this.segmentRepo = segmentRepo;
}

    // ✅ NEW — centralized org-isolation check, reused everywhere a single
    // video is read or mutated. Non-org callers (organizationId == null)
    // are never restricted — that's the existing behavior we must preserve.
    private void validateOrgAccess(Video video, String organizationId) {
        if (organizationId != null && !organizationId.equals(video.getOrganizationId())) {
            throw new AccessDeniedException("Cross-organization access is not allowed");
        }
    }


    public Video uploadVideo(
            MultipartFile file,
            String title,
            String description,
            Long batchId,          // null = no batch
            String tags,
            String category,
            String language,
            String visibility,
            String audience,
            boolean ageRestrict,
            String course,
            String status,
            String organizationId   // ✅ NEW — from caller's JWT, null for non-org users
    ) throws Exception {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();



        // ✅ Only check batch ownership if batchId is provided
        if (batchId != null) {
            TrainerBatchMap map = trainerBatchMapRepository
                    .findByTrainerEmailAndBatchId(email, batchId)
                    .orElse(null);

            System.out.println("LOGIN USER = " + email);
            System.out.println("BATCH ID = " + batchId);

            boolean allowed = map != null
                    && (organizationId == null || organizationId.equals(map.getOrganizationId()));

            if (!allowed) {
                throw new RuntimeException("You are not assigned to this batch");
            }
        }

        Path directory = Paths.get(uploadDir);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        String storedFileName =
                System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path filePath = directory.resolve(storedFileName);
        Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setOriginalFileName(file.getOriginalFilename());
        video.setStoredFileName(storedFileName);
        video.setSize(file.getSize());
        video.setBatchId(batchId);   // ✅ null is fine — no batch assigned yet
        video.setUploadedBy(email.trim().toLowerCase());
        video.setOrganizationId(organizationId);   // ✅ NEW — stamped once, at creation
        video.setTags(tags != null ? tags : "");
        video.setCategory(category != null ? category : "");
        video.setLanguage(language != null ? language : "English");
        video.setVisibility(visibility != null ? visibility : "public");
        video.setAudience(audience != null ? audience : "not-kids");
        video.setAgeRestrict(ageRestrict);
        video.setCourse(course != null ? course : "");
        video.setStatus(status != null ? status : "draft");
        Video saved = repo.save(video);
        try {
            transcriptGenerationService.generateAsync(
                    saved.getId(), filePath.toString(), TranscriptSourceType.LIBRARY_VIDEO);
        } catch (Exception ignored) {
            // transcript kickoff failures must never affect the upload response
        }

        // ✅ Only send Kafka event if batch is assigned
        if (batchId != null) {
            videoProducer.sendVideoUploadedEvent(storedFileName, title, batchId);
        }

        return saved;
    }




    public Video uploadVideoByUrl(
            String videoUrl,
            String title,
            String description,
            Long batchId,
            String tags,
            String category,
            String language,
            String visibility,
            String audience,
            boolean ageRestrict,
            String course,
            String status,
            String organizationId   // ✅ NEW
    ) throws Exception {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // ✅ Only check batch ownership if batchId is provided
        if (batchId != null) {
            TrainerBatchMap map = trainerBatchMapRepository
                    .findByTrainerEmailAndBatchId(email, batchId)
                    .orElse(null);

            boolean allowed = map != null
                    && (organizationId == null || organizationId.equals(map.getOrganizationId()));

            if (!allowed) {
                throw new RuntimeException("You are not assigned to this batch");
            }
        }

        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setVideoUrl(videoUrl);
        video.setOriginalFileName("");
        video.setStoredFileName("");
        video.setSize(0);
        video.setBatchId(batchId);   // ✅ null is fine
        video.setUploadedBy(email.trim().toLowerCase());
        video.setOrganizationId(organizationId);   // ✅ NEW — stamped once, at creation
        video.setTags(tags != null ? tags : "");
        video.setCategory(category != null ? category : "");
        video.setLanguage(language != null ? language : "English");
        video.setVisibility(visibility != null ? visibility : "public");
        video.setAudience(audience != null ? audience : "not-kids");
        video.setAgeRestrict(ageRestrict);
        video.setCourse(course != null ? course : "");
        video.setStatus(status != null ? status : "draft");

        return repo.save(video);
    }


    public byte[] getVideoFile(String fileName, String organizationId) throws Exception {
        // ✅ NEW — previously this method had no db lookup at all, so there
        // was nothing to enforce an org check against. Now resolve the
        // Video row first and validate before touching disk.
        Video video = repo.findByStoredFileName(fileName)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        validateOrgAccess(video, organizationId);

        Path path = Paths.get(uploadDir).resolve(fileName);
        return Files.readAllBytes(path);
    }

    public Video getVideoMeta(Long id, String organizationId) {
        Video video = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        validateOrgAccess(video, organizationId);   // ✅ NEW

        return video;
    }

    public List<Video> getAllVideos(String organizationId, String type) {
        // ✅ org admins see only their org; non-org callers (e.g.
        // Super Admin) keep the existing unrestricted behavior.
        List<Video> all = (organizationId != null)
                ? repo.findByOrganizationIdOrderByUploadedAtDesc(organizationId)
                : repo.findAll(Sort.by(Sort.Direction.DESC, "uploadedAt"));

        // ✅ NEW — optional filter by computed videoType
        // (UPLOADED_FILE | YOUTUBE | VIMEO | DIRECT_URL). Filtering happens
        // in memory because videoType is derived, not a DB column — fine at
        // this list size, but if the videos table grows very large this is
        // a candidate to revisit (e.g. persist videoType at upload time).
        if (type == null || type.isBlank() || "ALL".equalsIgnoreCase(type)) {
            return all;
        }
        return all.stream()
                .filter(v -> type.equalsIgnoreCase(v.getVideoType()))
                .toList();
    }

    public void deleteVideo(Long id, String organizationId) {

        Video video = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        validateOrgAccess(video, organizationId);   // ✅ NEW

        Path videoPath = Paths.get(uploadDir).resolve(video.getStoredFileName());

        try {
            Files.deleteIfExists(videoPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete video file", e);
        }

        repo.delete(video);
        transcriptRepo.findBySessionIdAndSourceType(id, TranscriptSourceType.LIBRARY_VIDEO)
        .ifPresent(t -> {
            segmentRepo.deleteAll(segmentRepo.findByTranscriptIdOrderByOrderIndexAsc(t.getId()));
            transcriptRepo.delete(t);
        });

        try {
            videoProducer.sendVideoDeletedEvent(video.getStoredFileName());
        } catch (Exception e) {
            System.out.println("Kafka down. Video deleted without event: " + e.getMessage());
        }
    }


    public List<Video> getVideosForStudent(String organizationId) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName()
                .trim()
                .toLowerCase();

        List<StudentBatchMap> mappings =
                studentBatchMapRepository.findAllByStudentEmail(email);

        if (mappings.isEmpty()) {
            return Collections.emptyList();
        }

        List<Long> batchIds = mappings.stream()
                .map(StudentBatchMap::getBatchId)
                .toList();

        // ✅ NEW — null-safe org filter baked into the query itself
        return repo.findByBatchIdInAndStatusAndOrganizationId(batchIds, "published", organizationId);
    }

    public List<Video> getVideosForTrainer(String organizationId) {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName()
                .trim()
                .toLowerCase();

        // ✅ NEW — org-aware lookup (email alone isn't safe across orgs
        // unless global email uniqueness is guaranteed by Auth Service)
        return repo.findByUploadedByAndOrganizationId(email, organizationId);
    }

    public Video assignBatchToVideo(Long videoId, Long batchId, String organizationId) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // Verify trainer owns the video
        Video video = repo.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        validateOrgAccess(video, organizationId);   // ✅ NEW

        if (!video.uploadedBy().equalsIgnoreCase(email.trim())) {
            throw new RuntimeException("Not your video");
        }

        // Verify trainer is assigned to the new batch (and that batch
        // belongs to the same org as the caller — ✅ NEW)
        TrainerBatchMap map = trainerBatchMapRepository
                .findByTrainerEmailAndBatchId(email, batchId)
                .orElse(null);

        boolean allowed = map != null
                && (organizationId == null || organizationId.equals(map.getOrganizationId()));

        if (!allowed) {
            throw new RuntimeException("You are not assigned to this batch");
        }

        video.setBatchId(batchId);
        video.setStatus("published");

        Video saved = repo.save(video);

        // Now that batch is assigned, fire the Kafka event
        try {
            videoProducer.sendVideoUploadedEvent(
                video.getStoredFileName(), video.getTitle(), batchId
            );
        } catch (Exception e) {
            System.out.println("Kafka event failed for batch assignment: " + e.getMessage());
        }

        return saved;
    }


    public Video publishVideo(Long videoId, String organizationId) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Video video = repo.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        validateOrgAccess(video, organizationId);   // ✅ NEW

        if (!video.uploadedBy().equalsIgnoreCase(email.trim())) {
            throw new RuntimeException("Not your video");
        }

        if (video.getBatchId() == null) {
            throw new RuntimeException("Assign a batch before publishing");
        }

        video.setStatus("published");
        Video saved = repo.save(video);

        try {
            videoProducer.sendVideoUploadedEvent(
                video.getStoredFileName(), video.getTitle(), video.getBatchId()
            );
        } catch (Exception e) {
            System.out.println("Kafka publish event failed: " + e.getMessage());
        }

        return saved;
    }

    /**
     * Edit a file-upload video.
     * - file is OPTIONAL: if null, the old stored file is kept.
     * - All other metadata fields are always updated.
     * - organizationId is NEVER updated here — it is immutable after upload.
     */
    public Video editVideo(
            Long videoId,
            MultipartFile file,        // nullable — null = keep existing file
            String title,
            String description,
            Long batchId,              // nullable
            String tags,
            String category,
            String language,
            String visibility,
            String audience,
            boolean ageRestrict,
            String course,
            String status,
            String organizationId      // ✅ NEW — caller's org, used only for validation
    ) throws Exception {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName()
                .trim()
                .toLowerCase();

        Video video = repo.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        validateOrgAccess(video, organizationId);   // ✅ NEW

        // Only the uploader may edit
        if (!video.uploadedBy().equalsIgnoreCase(email)) {
            throw new RuntimeException("Not your video");
        }

        // If a new batchId is supplied, verify the trainer is assigned to it
        // (and that the batch belongs to the same org — ✅ NEW)
        if (batchId != null) {
            TrainerBatchMap map = trainerBatchMapRepository
                    .findByTrainerEmailAndBatchId(email, batchId)
                    .orElse(null);

            boolean allowed = map != null
                    && (organizationId == null || organizationId.equals(map.getOrganizationId()));

            if (!allowed) {
                throw new RuntimeException("You are not assigned to this batch");
            }
        }

        // ── Replace file only when a new one is provided ──
        if (file != null && !file.isEmpty()) {
            // Delete old physical file (best-effort)
            if (video.getStoredFileName() != null && !video.getStoredFileName().isBlank()) {
                Path oldPath = Paths.get(uploadDir).resolve(video.getStoredFileName());
                try {
                    Files.deleteIfExists(oldPath);
                } catch (IOException e) {
                    System.out.println("Could not delete old file: " + e.getMessage());
                }
            }

            Path directory = Paths.get(uploadDir);
            if (!Files.exists(directory)) {
                Files.createDirectories(directory);
            }

            String storedFileName =
                    System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path filePath = directory.resolve(storedFileName);
            Files.copy(file.getInputStream(), filePath, StandardCopyOption.REPLACE_EXISTING);

            video.setOriginalFileName(file.getOriginalFilename());
            video.setStoredFileName(storedFileName);
            video.setSize(file.getSize());
            // Clear URL fields when switching from URL → file
            video.setVideoUrl(null);
         // New file content means the old transcript no longer matches
            // this video — delete stale transcript/segments and kick off a
            // fresh transcription job for the new file, same pattern as
            // deleteVideo()'s cleanup + uploadVideo()'s kickoff.
            transcriptRepo.findBySessionIdAndSourceType(videoId, TranscriptSourceType.LIBRARY_VIDEO)
                    .ifPresent(t -> {
                        segmentRepo.deleteAll(segmentRepo.findByTranscriptIdOrderByOrderIndexAsc(t.getId()));
                        transcriptRepo.delete(t);
                    });

            try {
                transcriptGenerationService.generateAsync(
                        videoId, filePath.toString(), TranscriptSourceType.LIBRARY_VIDEO);
            } catch (Exception ignored) {
                // transcript kickoff failures must never affect the edit response
            }
        }

        // ── Update metadata ──
        video.setTitle(title);
        video.setDescription(description != null ? description : "");
        video.setBatchId(batchId);
        video.setTags(tags != null ? tags : "");
        video.setCategory(category != null ? category : "");
        video.setLanguage(language != null ? language : "English");
        video.setVisibility(visibility != null ? visibility : "public");
        video.setAudience(audience != null ? audience : "not-kids");
        video.setAgeRestrict(ageRestrict);
        video.setCourse(course != null ? course : "");
        video.setStatus(status != null ? status : video.getStatus());
        // ✅ organizationId is intentionally NOT touched here — immutable after upload

        Video saved = repo.save(video);

        // Fire Kafka only if a batch is now assigned
        if (batchId != null) {
            try {
                videoProducer.sendVideoUploadedEvent(
                        saved.getStoredFileName(), saved.getTitle(), batchId);
            } catch (Exception e) {
                System.out.println("Kafka event failed during edit: " + e.getMessage());
            }
        }

        return saved;
    }

    /**
     * Edit a URL-based video.
     * - videoUrl is OPTIONAL: if null/blank, the old URL is kept.
     * - All other metadata fields are always updated.
     * - organizationId is NEVER updated here — it is immutable after upload.
     */
    public Video editVideoByUrl(
            Long videoId,
            String videoUrl,           // nullable — null = keep existing URL
            String title,
            String description,
            Long batchId,
            String tags,
            String category,
            String language,
            String visibility,
            String audience,
            boolean ageRestrict,
            String course,
            String status,
            String organizationId      // ✅ NEW — caller's org, used only for validation
    ) throws Exception {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName()
                .trim()
                .toLowerCase();

        Video video = repo.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        validateOrgAccess(video, organizationId);   // ✅ NEW

        if (!video.uploadedBy().equalsIgnoreCase(email)) {
            throw new RuntimeException("Not your video");
        }

        if (batchId != null) {
            TrainerBatchMap map = trainerBatchMapRepository
                    .findByTrainerEmailAndBatchId(email, batchId)
                    .orElse(null);

            boolean allowed = map != null
                    && (organizationId == null || organizationId.equals(map.getOrganizationId()));

            if (!allowed) {
                throw new RuntimeException("You are not assigned to this batch");
            }
        }

        // Update URL only if a new one was supplied
        if (videoUrl != null && !videoUrl.isBlank()) {
            video.setVideoUrl(videoUrl.trim());
        }

        video.setTitle(title);
        video.setDescription(description != null ? description : "");
        video.setBatchId(batchId);
        video.setTags(tags != null ? tags : "");
        video.setCategory(category != null ? category : "");
        video.setLanguage(language != null ? language : "English");
        video.setVisibility(visibility != null ? visibility : "public");
        video.setAudience(audience != null ? audience : "not-kids");
        video.setAgeRestrict(ageRestrict);
        video.setCourse(course != null ? course : "");
        video.setStatus(status != null ? status : video.getStatus());
        // ✅ organizationId is intentionally NOT touched here — immutable after upload

        return repo.save(video);
    }

}