
package com.lms.video.controller;

import com.lms.video.constants.VideoFeatureKeys;
import com.lms.video.dto.UploadQuotaResponse;
import com.lms.video.model.Video;
import com.lms.video.security.JwtUtil;
import com.lms.video.service.VideoFeatureFlagsService;
import com.lms.video.service.VideoService;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import com.lms.video.dto.TranscriptResponse;
import com.lms.video.model.FeaturedTranscriptSegment;
import com.lms.video.model.FeaturedVideoTranscript;
import com.lms.video.model.TranscriptSourceType;
import com.lms.video.model.TranscriptStatus;
import com.lms.video.repository.FeaturedTranscriptSegmentRepository;
import com.lms.video.repository.FeaturedVideoTranscriptRepository;
import java.util.Optional;
@RestController
@RequestMapping("/api/video")
public class VideoController {

    private final VideoService service;
    private final JwtUtil jwtUtil;
    private final VideoFeatureFlagsService featureFlagsService; // NEW


    private final FeaturedVideoTranscriptRepository transcriptRepo;
    private final FeaturedTranscriptSegmentRepository segmentRepo;

    public VideoController(VideoService service,
                           JwtUtil jwtUtil,
                           VideoFeatureFlagsService featureFlagsService,
                           FeaturedVideoTranscriptRepository transcriptRepo,
                           FeaturedTranscriptSegmentRepository segmentRepo) {
        this.service             = service;
        this.jwtUtil             = jwtUtil;
        this.featureFlagsService = featureFlagsService;
        this.transcriptRepo      = transcriptRepo;
        this.segmentRepo         = segmentRepo;
    }

    // ── helpers ───────────────────────────────────────────────────────────────

    private String orgIdFrom(String authHeader) {
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7)
                : authHeader;
        return jwtUtil.extractOrganizationId(token);
    }

    private String emailFrom(String authHeader) {
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7)
                : authHeader;
        return jwtUtil.extractEmail(token);
    }

    // ── FILE UPLOAD ───────────────────────────────────────────────────────────
    @PostMapping("/upload")
    public Video uploadVideo(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", defaultValue = "") String description,
            @RequestParam(value = "batchId", required = false) Long batchId,
            @RequestParam(value = "tags",        defaultValue = "") String tags,
            @RequestParam(value = "category",    defaultValue = "") String category,
            @RequestParam(value = "language",    defaultValue = "English") String language,
            @RequestParam(value = "visibility",  defaultValue = "public") String visibility,
            @RequestParam(value = "audience",    defaultValue = "not-kids") String audience,
            @RequestParam(value = "ageRestrict", defaultValue = "false") boolean ageRestrict,
            @RequestParam(value = "course",      defaultValue = "") String course,
            @RequestParam(value = "status",      defaultValue = "published") String status
    ) throws Exception {
        String organizationId = orgIdFrom(authHeader);
        String email          = emailFrom(authHeader);

        // FEATURE GATE
        featureFlagsService.enforce(organizationId, email, VideoFeatureKeys.UPLOAD_VIDEO);

        return service.uploadVideo(
                file, title, description, batchId,
                tags, category, language, visibility, audience, ageRestrict, course, status,
                organizationId
        );
    }

    // ── URL UPLOAD ────────────────────────────────────────────────────────────
    @PostMapping("/upload-url")
    public Video uploadVideoUrl(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam("videoUrl") String videoUrl,
            @RequestParam("title") String title,
            @RequestParam(value = "description", defaultValue = "") String description,
            @RequestParam(value = "batchId", required = false) Long batchId,
            @RequestParam(value = "tags",        defaultValue = "") String tags,
            @RequestParam(value = "category",    defaultValue = "") String category,
            @RequestParam(value = "language",    defaultValue = "English") String language,
            @RequestParam(value = "visibility",  defaultValue = "public") String visibility,
            @RequestParam(value = "audience",    defaultValue = "not-kids") String audience,
            @RequestParam(value = "ageRestrict", defaultValue = "false") boolean ageRestrict,
            @RequestParam(value = "course",      defaultValue = "") String course,
            @RequestParam(value = "status",      defaultValue = "published") String status
    ) throws Exception {
        String organizationId = orgIdFrom(authHeader);
        String email          = emailFrom(authHeader);

        // FEATURE GATE
        featureFlagsService.enforce(organizationId, email, VideoFeatureKeys.UPLOAD_VIDEO_URL);

        return service.uploadVideoByUrl(
                videoUrl, title, description, batchId,
                tags, category, language, visibility, audience, ageRestrict, course, status,
                organizationId
        );
    }

    // ── UPLOAD QUOTA PREVIEW ─────────────────────────────────────────────────
    // ✅ NEW — lets the frontend check remaining quota before attempting an
    // upload, so it can warn/disable the UI instead of letting the upload
    // fail server-side. Not feature-gated: it's a read-only preview, same
    // spirit as GET /{id} above.
    @GetMapping("/upload-quota")
    public UploadQuotaResponse getUploadQuota(
            @RequestHeader("Authorization") String authHeader
    ) {
        String organizationId = orgIdFrom(authHeader);
        String email          = emailFrom(authHeader);

        return service.getUploadQuota(organizationId, email);
    }

    // ── PLAY VIDEO ────────────────────────────────────────────────────────────
 // ── PLAY VIDEO ────────────────────────────────────────────────────────────
 // Returns a presigned S3 URL (JSON), not bytes. Frontend does a two-step
 // fetch: authed call here for the URL, then an unauthed GET straight to S3
 // for the actual video bytes — S3 supports range requests natively, so
 // seeking/scrubbing works correctly too.
 @GetMapping("/play/{id}")
 public ResponseEntity<java.util.Map<String, String>> playVideo(
         @RequestHeader("Authorization") String authHeader,
         @PathVariable Long id
 ) {
     String organizationId = orgIdFrom(authHeader);
     String email          = emailFrom(authHeader);

     featureFlagsService.enforce(organizationId, email, VideoFeatureKeys.PLAY_VIDEO);

     String url = service.getPresignedPlayUrl(id, organizationId);
     return ResponseEntity.ok(java.util.Map.of("url", url));
 }

    // ── GET VIDEO META (not gated — super admin / public use) ─────────────────
    @GetMapping("/{id}")
    public Video getVideoInfo(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id
    ) {
        String organizationId = orgIdFrom(authHeader);
        return service.getVideoMeta(id, organizationId);
    }

    // ── ADMIN: LIST ALL VIDEOS ────────────────────────────────────────────────
    @GetMapping
    public List<Video> listVideos(
            @RequestHeader("Authorization") String authHeader,
            @RequestParam(value = "type", required = false) String type
    ) {
        String organizationId = orgIdFrom(authHeader);
        String email          = emailFrom(authHeader);

        // FEATURE GATE
        featureFlagsService.enforce(organizationId, email, VideoFeatureKeys.GET_ALL_VIDEOS);

        return service.getAllVideos(organizationId, type);
    }

    // ── STUDENT: GET ENROLLED VIDEOS ──────────────────────────────────────────
    @GetMapping("/student")
    public List<Video> getStudentVideos(
            @RequestHeader("Authorization") String authHeader
    ) {
        String organizationId = orgIdFrom(authHeader);
        String email          = emailFrom(authHeader);

        // FEATURE GATE
        featureFlagsService.enforce(organizationId, email, VideoFeatureKeys.GET_STUDENT_VIDEOS);

        return service.getVideosForStudent(organizationId);
    }
    
    
 // ── STUDENT: TRUE TOTAL (before tier cap) — lets frontend show
    // "N more locked" instead of silently truncating with no signal ──
    @GetMapping("/student/count")
    public java.util.Map<String, Object> getStudentVideoCount(
            @RequestHeader("Authorization") String authHeader
    ) {
        String organizationId = orgIdFrom(authHeader);
        String email          = emailFrom(authHeader);

        featureFlagsService.enforce(organizationId, email, VideoFeatureKeys.GET_STUDENT_VIDEOS);

        return service.getStudentVideoCount(organizationId);
    }
    
    

    // ── TRAINER: GET OWN VIDEOS ───────────────────────────────────────────────
    @GetMapping("/trainer")
    public List<Video> getTrainerVideos(
            @RequestHeader("Authorization") String authHeader
    ) {
        String organizationId = orgIdFrom(authHeader);
        String email          = emailFrom(authHeader);

        // FEATURE GATE
        featureFlagsService.enforce(organizationId, email, VideoFeatureKeys.GET_TRAINER_VIDEOS);

        return service.getVideosForTrainer(organizationId);
    }

    // ── DELETE VIDEO ──────────────────────────────────────────────────────────
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id
    ) {
        String organizationId = orgIdFrom(authHeader);
        String email          = emailFrom(authHeader);

        // FEATURE GATE
        featureFlagsService.enforce(organizationId, email, VideoFeatureKeys.DELETE_VIDEO);

        service.deleteVideo(id, organizationId);
        return ResponseEntity.noContent().build();
    }

    // ── ASSIGN BATCH ──────────────────────────────────────────────────────────
    @PatchMapping("/{id}/assign-batch")
    public ResponseEntity<Video> assignBatch(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestParam("batchId") Long batchId
    ) {
        String organizationId = orgIdFrom(authHeader);
        String email          = emailFrom(authHeader);

        // FEATURE GATE
        featureFlagsService.enforce(organizationId, email, VideoFeatureKeys.ASSIGN_BATCH);

        Video updated = service.assignBatchToVideo(id, batchId, organizationId);
        return ResponseEntity.ok(updated);
    }

    // ── PUBLISH VIDEO ─────────────────────────────────────────────────────────
    @PatchMapping("/{id}/publish")
    public ResponseEntity<Video> publishVideo(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id
    ) {
        String organizationId = orgIdFrom(authHeader);
        String email          = emailFrom(authHeader);

        // FEATURE GATE
        featureFlagsService.enforce(organizationId, email, VideoFeatureKeys.PUBLISH_VIDEO);

        Video updated = service.publishVideo(id, organizationId);
        return ResponseEntity.ok(updated);
    }

    // ── EDIT VIDEO (file) ─────────────────────────────────────────────────────
    @PutMapping(value = "/{id}/edit", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Video> editVideo(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestParam(value = "file",        required = false) MultipartFile file,
            @RequestParam("title")               String title,
            @RequestParam(value = "description",  defaultValue = "") String description,
            @RequestParam(value = "batchId",      required = false)  Long batchId,
            @RequestParam(value = "tags",         defaultValue = "") String tags,
            @RequestParam(value = "category",     defaultValue = "") String category,
            @RequestParam(value = "language",     defaultValue = "English") String language,
            @RequestParam(value = "visibility",   defaultValue = "public")  String visibility,
            @RequestParam(value = "audience",     defaultValue = "not-kids") String audience,
            @RequestParam(value = "ageRestrict",  defaultValue = "false")   boolean ageRestrict,
            @RequestParam(value = "course",       defaultValue = "") String course,
            @RequestParam(value = "status",       defaultValue = "draft")   String status
    ) throws Exception {
        String organizationId = orgIdFrom(authHeader);
        String email          = emailFrom(authHeader);

        // FEATURE GATE
        featureFlagsService.enforce(organizationId, email, VideoFeatureKeys.EDIT_VIDEO);

        Video updated = service.editVideo(
                id, file, title, description, batchId,
                tags, category, language, visibility, audience, ageRestrict, course, status,
                organizationId
        );
        return ResponseEntity.ok(updated);
    }

    // ── EDIT VIDEO (URL) ──────────────────────────────────────────────────────
    @PutMapping("/{id}/edit-url")
    public ResponseEntity<Video> editVideoUrl(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable Long id,
            @RequestParam(value = "videoUrl",    required = false)   String videoUrl,
            @RequestParam("title")                                    String title,
            @RequestParam(value = "description",  defaultValue = "")  String description,
            @RequestParam(value = "batchId",      required = false)   Long batchId,
            @RequestParam(value = "tags",         defaultValue = "")  String tags,
            @RequestParam(value = "category",     defaultValue = "")  String category,
            @RequestParam(value = "language",     defaultValue = "English") String language,
            @RequestParam(value = "visibility",   defaultValue = "public")  String visibility,
            @RequestParam(value = "audience",     defaultValue = "not-kids") String audience,
            @RequestParam(value = "ageRestrict",  defaultValue = "false")    boolean ageRestrict,
            @RequestParam(value = "course",       defaultValue = "")  String course,
            @RequestParam(value = "status",       defaultValue = "draft")    String status
    ) throws Exception {
        String organizationId = orgIdFrom(authHeader);
        String email          = emailFrom(authHeader);

        // FEATURE GATE
        featureFlagsService.enforce(organizationId, email, VideoFeatureKeys.EDIT_VIDEO_URL);

        Video updated = service.editVideoByUrl(
                id, videoUrl, title, description, batchId,
                tags, category, language, visibility, audience, ageRestrict, course, status,
                organizationId
        );
        return ResponseEntity.ok(updated);
    }
 // ── GET VIDEO TRANSCRIPT ──────────────────────────────────────────────────
    @GetMapping("/{id}/transcript")
    public TranscriptResponse getVideoTranscript(@PathVariable Long id) {
        Optional<FeaturedVideoTranscript> transcriptOpt =
                transcriptRepo.findBySessionIdAndSourceType(id, TranscriptSourceType.LIBRARY_VIDEO);

        if (transcriptOpt.isEmpty()) {
            return new TranscriptResponse("NONE", null, null, List.of());
        }

        FeaturedVideoTranscript transcript = transcriptOpt.get();

        if (transcript.getStatus() == TranscriptStatus.READY) {
            List<FeaturedTranscriptSegment> segments =
                    segmentRepo.findByTranscriptIdOrderByOrderIndexAsc(transcript.getId());
            List<TranscriptResponse.SegmentDto> segmentDtos = segments.stream()
                    .map(s -> new TranscriptResponse.SegmentDto(s.getStartSeconds(), s.getEndSeconds(), s.getText()))
                    .toList();
            return new TranscriptResponse("READY", transcript.getLanguage(), null, segmentDtos);
        }

        if (transcript.getStatus() == TranscriptStatus.FAILED) {
            return new TranscriptResponse("FAILED", transcript.getLanguage(), transcript.getErrorMessage(), List.of());
        }

        return new TranscriptResponse(transcript.getStatus().name(), transcript.getLanguage(), null, List.of());
    }
}