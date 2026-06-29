//
//
//import com.lms.video.model.Video;
//import com.lms.video.security.JwtUtil;
//import com.lms.video.service.VideoService;
//import org.springframework.http.HttpHeaders;
//import org.springframework.http.MediaType;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/video")
//public class VideoController {
//
//    private final VideoService service;
//    private final JwtUtil jwtUtil;   // ✅ NEW
//
//    public VideoController(VideoService service, JwtUtil jwtUtil) {
//        this.service = service;
//        this.jwtUtil = jwtUtil;
//    }
//
//    // ✅ NEW — extracts organizationId from the raw Authorization header.
//    // Returns null for non-org users (Super Admin, Google login,
//    // self-registered) which is exactly the "no restriction" signal the
//    // rest of the system needs.
//    private String orgIdFrom(String authHeader) {
//        String token = (authHeader != null && authHeader.startsWith("Bearer "))
//                ? authHeader.substring(7)
//                : authHeader;
//        return jwtUtil.extractOrganizationId(token);
//    }
//
//
// // ✅ FILE UPLOAD — batchId is now Optional
//    @PostMapping("/upload")
//    public Video uploadVideo(
//            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("title") String title,
//            @RequestParam(value = "description", defaultValue = "") String description,
//            @RequestParam(value = "batchId", required = false) Long batchId,  // ✅ required=false
//            @RequestParam(value = "tags",        defaultValue = "") String tags,
//            @RequestParam(value = "category",    defaultValue = "") String category,
//            @RequestParam(value = "language",    defaultValue = "English") String language,
//            @RequestParam(value = "visibility",  defaultValue = "public") String visibility,
//            @RequestParam(value = "audience",    defaultValue = "not-kids") String audience,
//            @RequestParam(value = "ageRestrict", defaultValue = "false") boolean ageRestrict,
//            @RequestParam(value = "course",      defaultValue = "") String course,
//            @RequestParam(value = "status",      defaultValue = "published") String status
//    ) throws Exception {
//        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
//        return service.uploadVideo(
//                file, title, description, batchId,
//                tags, category, language, visibility, audience, ageRestrict, course, status,
//                organizationId
//        );
//    }
//
//    // ✅ URL UPLOAD — batchId is now Optional
//    @PostMapping("/upload-url")
//    public Video uploadVideoUrl(
//            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
//            @RequestParam("videoUrl") String videoUrl,
//            @RequestParam("title") String title,
//            @RequestParam(value = "description", defaultValue = "") String description,
//            @RequestParam(value = "batchId", required = false) Long batchId,  // ✅ required=false
//            @RequestParam(value = "tags",        defaultValue = "") String tags,
//            @RequestParam(value = "category",    defaultValue = "") String category,
//            @RequestParam(value = "language",    defaultValue = "English") String language,
//            @RequestParam(value = "visibility",  defaultValue = "public") String visibility,
//            @RequestParam(value = "audience",    defaultValue = "not-kids") String audience,
//            @RequestParam(value = "ageRestrict", defaultValue = "false") boolean ageRestrict,
//            @RequestParam(value = "course",      defaultValue = "") String course,
//            @RequestParam(value = "status",      defaultValue = "published") String status
//    ) throws Exception {
//        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
//        return service.uploadVideoByUrl(
//                videoUrl, title, description, batchId,
//                tags, category, language, visibility, audience, ageRestrict, course, status,
//                organizationId
//        );
//    }
//
//
//
//
//    @GetMapping("/play/{fileName}")
//    public ResponseEntity<byte[]> playVideo(
//            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
//            @PathVariable String fileName
//    ) throws Exception {
//
//        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
//        byte[] videoBytes = service.getVideoFile(fileName, organizationId);
//
//        return ResponseEntity.ok()
//                .header(HttpHeaders.CONTENT_DISPOSITION,
//                        "inline; filename=\"" + fileName + "\"")
//                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
//                .contentType(MediaType.valueOf("video/mp4"))
//                .body(videoBytes);
//    }
//
//    @GetMapping("/{id}")
//    public Video getVideoInfo(
//            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
//            @PathVariable Long id
//    ) {
//        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
//        return service.getVideoMeta(id, organizationId);
//    }
//
//    // ⚠️ ADMIN ONLY (later we secure)
//    @GetMapping
//    public List<Video> listVideos(
//            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
//            @RequestParam(value = "type", required = false) String type   // ✅ NEW — UPLOADED_FILE | YOUTUBE | VIMEO | DIRECT_URL | ALL
//    ) {
//        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
//        return service.getAllVideos(organizationId, type);
//    }
//
// // 🎓 STUDENT DASHBOARD
//    @GetMapping("/student")
//    public List<Video> getStudentVideos(
//            @RequestHeader("Authorization") String authHeader   // ✅ NEW
//    ) {
//        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
//        return service.getVideosForStudent(organizationId);
//    }
//
// // 👨‍🏫 TRAINER DASHBOARD //trainer upaoded getting api
//    @GetMapping("/trainer")
//    public List<Video> getTrainerVideos(
//            @RequestHeader("Authorization") String authHeader   // ✅ NEW
//    ) {
//        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
//        return service.getVideosForTrainer(organizationId);
//    }
//
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteVideo(
//            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
//            @PathVariable Long id
//    ) {
//        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
//        service.deleteVideo(id, organizationId);
//        return ResponseEntity.noContent().build();
//    }
// // ✅ NEW — trainer assigns a batch to an existing video
//    @PatchMapping("/{id}/assign-batch")
//    public ResponseEntity<Video> assignBatch(
//            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
//            @PathVariable Long id,
//            @RequestParam("batchId") Long batchId
//    ) {
//        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
//        Video updated = service.assignBatchToVideo(id, batchId, organizationId);
//        return ResponseEntity.ok(updated);
//    }
//
//    @PatchMapping("/{id}/publish")
//    public ResponseEntity<Video> publishVideo(
//            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
//            @PathVariable Long id
//    ) {
//        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
//        Video updated = service.publishVideo(id, organizationId);
//        return ResponseEntity.ok(updated);
//    }
//
//    /**
//     * PUT /api/video/{id}/edit
//     * Edit a file-upload video. Send as multipart/form-data.
//     * "file" part is OPTIONAL — omit it to keep the existing stored file.
//     */
//    @PutMapping(value = "/{id}/edit", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<Video> editVideo(
//            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
//            @PathVariable Long id,
//            @RequestParam(value = "file",        required = false) MultipartFile file,
//            @RequestParam("title")         String title,
//            @RequestParam(value = "description",  defaultValue = "") String description,
//            @RequestParam(value = "batchId",      required = false)  Long batchId,
//            @RequestParam(value = "tags",         defaultValue = "") String tags,
//            @RequestParam(value = "category",     defaultValue = "") String category,
//            @RequestParam(value = "language",     defaultValue = "English") String language,
//            @RequestParam(value = "visibility",   defaultValue = "public")  String visibility,
//            @RequestParam(value = "audience",     defaultValue = "not-kids") String audience,
//            @RequestParam(value = "ageRestrict",  defaultValue = "false")   boolean ageRestrict,
//            @RequestParam(value = "course",       defaultValue = "") String course,
//            @RequestParam(value = "status",       defaultValue = "draft")   String status
//    ) throws Exception {
//        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
//        Video updated = service.editVideo(
//                id, file, title, description, batchId,
//                tags, category, language, visibility, audience, ageRestrict, course, status,
//                organizationId
//        );
//        return ResponseEntity.ok(updated);
//    }
//
//    /**
//     * PUT /api/video/{id}/edit-url
//     * Edit a URL-based video. Send as multipart/form-data (or application/x-www-form-urlencoded).
//     * "videoUrl" is OPTIONAL — omit to keep the existing URL.
//     */
//    @PutMapping("/{id}/edit-url")
//    public ResponseEntity<Video> editVideoUrl(
//            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
//            @PathVariable Long id,
//            @RequestParam(value = "videoUrl",    required = false)   String videoUrl,
//            @RequestParam("title")                                    String title,
//            @RequestParam(value = "description",  defaultValue = "")  String description,
//            @RequestParam(value = "batchId",      required = false)   Long batchId,
//            @RequestParam(value = "tags",         defaultValue = "")  String tags,
//            @RequestParam(value = "category",     defaultValue = "")  String category,
//            @RequestParam(value = "language",     defaultValue = "English") String language,
//            @RequestParam(value = "visibility",   defaultValue = "public")  String visibility,
//            @RequestParam(value = "audience",     defaultValue = "not-kids") String audience,
//            @RequestParam(value = "ageRestrict",  defaultValue = "false")    boolean ageRestrict,
//            @RequestParam(value = "course",       defaultValue = "")  String course,
//            @RequestParam(value = "status",       defaultValue = "draft")    String status
//    ) throws Exception {
//        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
//        Video updated = service.editVideoByUrl(
//                id, videoUrl, title, description, batchId,
//                tags, category, language, visibility, audience, ageRestrict, course, status,
//                organizationId
//        );
//        return ResponseEntity.ok(updated);
//    }
//
//}
package com.lms.video.controller;

import com.lms.video.constants.VideoFeatureKeys;
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

@RestController
@RequestMapping("/api/video")
public class VideoController {

    private final VideoService service;
    private final JwtUtil jwtUtil;
    private final VideoFeatureFlagsService featureFlagsService; // NEW

    public VideoController(VideoService service,
                           JwtUtil jwtUtil,
                           VideoFeatureFlagsService featureFlagsService) { // NEW
        this.service             = service;
        this.jwtUtil             = jwtUtil;
        this.featureFlagsService = featureFlagsService; // NEW
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

    // ── PLAY VIDEO ────────────────────────────────────────────────────────────
    @GetMapping("/play/{fileName}")
    public ResponseEntity<byte[]> playVideo(
            @RequestHeader("Authorization") String authHeader,
            @PathVariable String fileName
    ) throws Exception {
        String organizationId = orgIdFrom(authHeader);
        String email          = emailFrom(authHeader);

        // FEATURE GATE
        featureFlagsService.enforce(organizationId, email, VideoFeatureKeys.PLAY_VIDEO);

        byte[] videoBytes = service.getVideoFile(fileName, organizationId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + fileName + "\"")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .contentType(MediaType.valueOf("video/mp4"))
                .body(videoBytes);
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
}