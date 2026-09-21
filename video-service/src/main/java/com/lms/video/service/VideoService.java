

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
import com.lms.video.constants.VideoTierResolver;
import com.lms.video.constants.VideoTierLimits;
import com.lms.video.dto.UploadQuotaResponse;
import com.lms.video.exception.VideoSizeLimitExceededException;
import com.lms.video.exception.VideoStorageLimitExceededException;
import com.lms.video.exception.VideoCountLimitExceededException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;

import java.time.Duration;
import java.util.List;
import java.util.Collections;
@Service
public class VideoService {

    private final VideoRepository repo;
    private final VideoProducer videoProducer;
    private final TrainerBatchMapRepository trainerBatchMapRepository;
    private final StudentBatchMapRepository studentBatchMapRepository;
    private final TranscriptGenerationService transcriptGenerationService;
    private final FeaturedVideoTranscriptRepository transcriptRepo;
    private final FeaturedTranscriptSegmentRepository segmentRepo;
    private final S3Service s3Service; // ✅ NEW — all file storage now goes through this
    private final VideoTierResolver videoTierResolver; // ✅ NEW — plan-tier lookup for upload limits


    public VideoService(VideoRepository repo,
            VideoProducer videoProducer,
            TrainerBatchMapRepository trainerBatchMapRepository,
            StudentBatchMapRepository studentBatchMapRepository,
            TranscriptGenerationService transcriptGenerationService,
            FeaturedVideoTranscriptRepository transcriptRepo,
            FeaturedTranscriptSegmentRepository segmentRepo,
            S3Service s3Service, // ✅ NEW
            VideoTierResolver videoTierResolver) { // ✅ NEW
this.repo = repo;
this.videoProducer = videoProducer;
this.trainerBatchMapRepository = trainerBatchMapRepository;
this.studentBatchMapRepository = studentBatchMapRepository;
this.transcriptGenerationService = transcriptGenerationService;
this.transcriptRepo = transcriptRepo;
this.segmentRepo = segmentRepo;
this.s3Service = s3Service; // ✅ NEW
this.videoTierResolver = videoTierResolver; // ✅ NEW
}

    // ✅ NEW — centralized org-isolation check, reused everywhere a single
    // video is read or mutated. Non-org callers (organizationId == null)
    // are never restricted — that's the existing behavior we must preserve.
    private void validateOrgAccess(Video video, String organizationId) {
        if (organizationId != null && !organizationId.equals(video.getOrganizationId())) {
            throw new AccessDeniedException("Cross-organization access is not allowed");
        }
    }

    // ✅ NEW — plan-tier quota enforcement, shared by uploadVideo and
    // editVideo (file-replace path). Order matters: size check is cheapest
    // and rejects obviously-oversized files before we touch the DB at all;
    // storage-cap check and count check both hit the repo.
    //
    // KNOWN SIMPLIFICATION (see editVideo): when called from a file-replace,
    // the old file's size is NOT subtracted from currentUsage first, so a
    // like-for-like replacement within the cap can be over-restricted. Fine
    // for now — flag if you want the subtract-old-then-check-new version.
    private void enforceUploadLimits(long newFileSize, String organizationId, String email) {
        String tier = videoTierResolver.resolveTier(organizationId, email);

        long maxSize = VideoTierLimits.maxVideoSizeFor(tier);
        if (newFileSize > maxSize) {
            throw new VideoSizeLimitExceededException(newFileSize, maxSize, tier);
        }

        long currentUsage = repo.sumStorageUsage(organizationId, email);
        long capacity = VideoTierLimits.storageCapFor(tier);
        if (currentUsage + newFileSize > capacity) {
            throw new VideoStorageLimitExceededException(currentUsage, newFileSize, capacity, tier);
        }

        long currentCount = repo.countVideos(organizationId, email);
        int maxCount = VideoTierLimits.maxVideoCountFor(tier);
        if (currentCount + 1 > maxCount) {
            throw new VideoCountLimitExceededException((int) currentCount, maxCount, tier);
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

        // ✅ NEW — plan-tier quota check, FIRST thing after email is resolved.
        // Runs before the batch-ownership check and before any S3 call, so a
        // caller over their limit never touches storage or Kafka.
        enforceUploadLimits(file.getSize(), organizationId, email);

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

        // ✅ CHANGED — video goes straight to S3, no local disk involved at all
        String storedFileName = "videos/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
        s3Service.uploadFile(storedFileName, file);

        // ✅ NEW — temporary secure link so ffmpeg can read the file directly
        // from S3 (valid for 2 hours — plenty of time even for large videos)
        String presignedUrl = s3Service.generatePresignedUrl(storedFileName, Duration.ofHours(2));

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
            // ✅ CHANGED — pass the presigned S3 URL instead of a local file path
            transcriptGenerationService.generateAsync(
                    saved.getId(), presignedUrl, TranscriptSourceType.LIBRARY_VIDEO);
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

        // ⚠️ NOTE — enforceUploadLimits is intentionally NOT called here.
        // URL-based videos have size=0 and consume no S3 storage, so the
        // size/storage checks would be no-ops anyway. They also skip the
        // video-count cap for now (a URL video still occupies a "slot" in
        // the trainer's library — flag if you want count enforcement added
        // here too via a size=0 call to enforceUploadLimits).

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
        // Video row first and validate before touching S3.
        Video video = repo.findByStoredFileName(fileName)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        validateOrgAccess(video, organizationId);

        // ✅ CHANGED — read bytes from S3 instead of local disk
        return s3Service.downloadFile(fileName);
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

        // ✅ CHANGED — delete from S3 instead of local disk. s3Service.deleteFile
        // is safe to call even if storedFileName is blank/missing (URL-based videos).
        if (video.getStoredFileName() != null && !video.getStoredFileName().isBlank()) {
            try {
                s3Service.deleteFile(video.getStoredFileName());
            } catch (Exception e) {
                throw new RuntimeException("Failed to delete video file from S3", e);
            }
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
        // (repo query already orders by uploaded_at DESC, so most-recent-first
        // is preserved by the cap below)
        List<Video> all = repo.findByBatchIdInAndStatusAndOrganizationId(batchIds, "published", organizationId);

        // ✅ NEW — plan-tier cap on how many videos a student can see.
        // This truncates rather than throwing: a student isn't performing an
        // "action" by viewing their list, so a silent cap is correct UX here.
        String tier = videoTierResolver.resolveTier(organizationId, email);
        int visibleCap = VideoTierLimits.studentVisibleCountFor(tier);
        if (visibleCap == -1) {
            return all; // unlimited (premium)
        }
        return all.stream().limit(visibleCap).toList();
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

    // ✅ NEW — lets the frontend preview quota before attempting an upload.
    public UploadQuotaResponse getUploadQuota(String organizationId, String email) {
        String tier = videoTierResolver.resolveTier(organizationId, email);

        long storageUsed = repo.sumStorageUsage(organizationId, email);
        long storageCap = VideoTierLimits.storageCapFor(tier);
        long videoCount = repo.countVideos(organizationId, email);
        int maxVideoCount = VideoTierLimits.maxVideoCountFor(tier);
        long maxVideoSize = VideoTierLimits.maxVideoSizeFor(tier);

        return new UploadQuotaResponse(tier, storageUsed, storageCap, videoCount, maxVideoCount, maxVideoSize);
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

            // ✅ NEW — plan-tier quota check, only runs when a new file is
            // actually being uploaded (metadata-only edits skip this
            // entirely, since no new storage/count is being consumed).
            //
            // KNOWN SIMPLIFICATION: the old file's size is NOT subtracted
            // from currentUsage first, so this may slightly over-restrict a
            // like-for-like file replacement that's within the cap. Flag if
            // you want the subtract-old-then-check-new logic added.
            enforceUploadLimits(file.getSize(), organizationId, email);

            // ✅ CHANGED — delete old file from S3 (best-effort), not local disk
            if (video.getStoredFileName() != null && !video.getStoredFileName().isBlank()) {
                try {
                    s3Service.deleteFile(video.getStoredFileName());
                } catch (Exception e) {
                    System.out.println("Could not delete old file from S3: " + e.getMessage());
                }
            }

            // ✅ CHANGED — new file goes straight to S3, no local disk involved
            String storedFileName = "videos/" + System.currentTimeMillis() + "_" + file.getOriginalFilename();
            s3Service.uploadFile(storedFileName, file);

            // ✅ NEW — temporary secure link so ffmpeg can read the new file
            String presignedUrl = s3Service.generatePresignedUrl(storedFileName, Duration.ofHours(2));

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
                // ✅ CHANGED — pass the presigned S3 URL instead of a local file path
                transcriptGenerationService.generateAsync(
                        videoId, presignedUrl, TranscriptSourceType.LIBRARY_VIDEO);
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
    public String getPresignedPlayUrl(Long id, String organizationId) {
        Video video = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));
        validateOrgAccess(video, organizationId);
        if (video.getStoredFileName() == null || video.getStoredFileName().isBlank()) {
            throw new RuntimeException("This video has no stored file (URL-based video)");
        }
        return s3Service.generatePresignedUrl(video.getStoredFileName(), Duration.ofMinutes(30));
    }
    
 // ✅ NEW — companion to getVideosForStudent(): reports the TRUE total
    // (before the tier cap truncates it) so the frontend can render an
    // "Upgrade to unlock N more" tile instead of silently stopping.
    // Deliberately re-derives `all` the same way rather than caching it,
    // to stay correct if this is called independently of the list call.
    public java.util.Map<String, Object> getStudentVideoCount(String organizationId) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName()
                .trim()
                .toLowerCase();

        List<StudentBatchMap> mappings =
                studentBatchMapRepository.findAllByStudentEmail(email);

        List<Long> batchIds = mappings.stream()
                .map(StudentBatchMap::getBatchId)
                .toList();

        int totalCount = batchIds.isEmpty()
                ? 0
                : repo.findByBatchIdInAndStatusAndOrganizationId(batchIds, "published", organizationId).size();

        String tier = videoTierResolver.resolveTier(organizationId, email);
        int visibleCap = VideoTierLimits.studentVisibleCountFor(tier);
        int visibleCount = visibleCap == -1 ? totalCount : Math.min(totalCount, visibleCap);

        return java.util.Map.of(
                "totalCount", totalCount,
                "visibleCount", visibleCount,
                "tier", tier,
                "unlimited", visibleCap == -1
        );
    }
}