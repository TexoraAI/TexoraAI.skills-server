//
//
//
//package com.lms.video.controller;
//
//import com.lms.video.model.Video;
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
//
//    public VideoController(VideoService service) {
//        this.service = service;
//    }
//
//  
// // ✅ FILE UPLOAD — batchId is now Optional
//    @PostMapping("/upload")
//    public Video uploadVideo(
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
//        return service.uploadVideo(
//                file, title, description, batchId,
//                tags, category, language, visibility, audience, ageRestrict, course, status
//        );
//    }
//
//    // ✅ URL UPLOAD — batchId is now Optional
//    @PostMapping("/upload-url")
//    public Video uploadVideoUrl(
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
//        return service.uploadVideoByUrl(
//                videoUrl, title, description, batchId,
//                tags, category, language, visibility, audience, ageRestrict, course, status
//        );
//    }
//
//    
//    
//    
//    @GetMapping("/play/{fileName}")
//    public ResponseEntity<byte[]> playVideo(@PathVariable String fileName) throws Exception {
//
//        byte[] videoBytes = service.getVideoFile(fileName);
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
//    public Video getVideoInfo(@PathVariable Long id) {
//        return service.getVideoMeta(id);
//    }
//
//    // ⚠️ ADMIN ONLY (later we secure)
//    @GetMapping
//    public List<Video> listVideos() {
//        return service.getAllVideos();
//    }
//
// // 🎓 STUDENT DASHBOARD
//    @GetMapping("/student")
//    public List<Video> getStudentVideos() {
//        return service.getVideosForStudent();
//    }
//
// // 👨‍🏫 TRAINER DASHBOARD //trainer upaoded getting api 
//    @GetMapping("/trainer")
//    public List<Video> getTrainerVideos() {
//        return service.getVideosForTrainer();
//    }
//
//    
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
//        service.deleteVideo(id);
//        return ResponseEntity.noContent().build();
//    }
// // ✅ NEW — trainer assigns a batch to an existing video
//    @PatchMapping("/{id}/assign-batch")
//    public ResponseEntity<Video> assignBatch(
//            @PathVariable Long id,
//            @RequestParam("batchId") Long batchId
//    ) {
//        Video updated = service.assignBatchToVideo(id, batchId);
//        return ResponseEntity.ok(updated);
//    }
//    @PatchMapping("/{id}/publish")
//    public ResponseEntity<Video> publishVideo(@PathVariable Long id) {
//        Video updated = service.publishVideo(id);
//        return ResponseEntity.ok(updated);
//    }
// // ═══════════════════════════════════════════════════════════════════
////  ADD THESE TWO ENDPOINTS to your existing VideoController.java
////  (paste anywhere after the existing @PatchMapping("/{id}/publish") endpoint)
//// ═══════════════════════════════════════════════════════════════════
//
//    /**
//     * PUT /api/video/{id}/edit
//     * Edit a file-upload video. Send as multipart/form-data.
//     * "file" part is OPTIONAL — omit it to keep the existing stored file.
//     */
//    @PutMapping(value = "/{id}/edit", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
//    public ResponseEntity<Video> editVideo(
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
//        Video updated = service.editVideo(
//                id, file, title, description, batchId,
//                tags, category, language, visibility, audience, ageRestrict, course, status
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
//        Video updated = service.editVideoByUrl(
//                id, videoUrl, title, description, batchId,
//                tags, category, language, visibility, audience, ageRestrict, course, status
//        );
//        return ResponseEntity.ok(updated);
//    }
//    
//}
package com.lms.video.controller;

import com.lms.video.model.Video;
import com.lms.video.security.JwtUtil;
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
    private final JwtUtil jwtUtil;   // ✅ NEW

    public VideoController(VideoService service, JwtUtil jwtUtil) {
        this.service = service;
        this.jwtUtil = jwtUtil;
    }

    // ✅ NEW — extracts organizationId from the raw Authorization header.
    // Returns null for non-org users (Super Admin, Google login,
    // self-registered) which is exactly the "no restriction" signal the
    // rest of the system needs.
    private String orgIdFrom(String authHeader) {
        String token = (authHeader != null && authHeader.startsWith("Bearer "))
                ? authHeader.substring(7)
                : authHeader;
        return jwtUtil.extractOrganizationId(token);
    }


 // ✅ FILE UPLOAD — batchId is now Optional
    @PostMapping("/upload")
    public Video uploadVideo(
            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam(value = "description", defaultValue = "") String description,
            @RequestParam(value = "batchId", required = false) Long batchId,  // ✅ required=false
            @RequestParam(value = "tags",        defaultValue = "") String tags,
            @RequestParam(value = "category",    defaultValue = "") String category,
            @RequestParam(value = "language",    defaultValue = "English") String language,
            @RequestParam(value = "visibility",  defaultValue = "public") String visibility,
            @RequestParam(value = "audience",    defaultValue = "not-kids") String audience,
            @RequestParam(value = "ageRestrict", defaultValue = "false") boolean ageRestrict,
            @RequestParam(value = "course",      defaultValue = "") String course,
            @RequestParam(value = "status",      defaultValue = "published") String status
    ) throws Exception {
        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
        return service.uploadVideo(
                file, title, description, batchId,
                tags, category, language, visibility, audience, ageRestrict, course, status,
                organizationId
        );
    }

    // ✅ URL UPLOAD — batchId is now Optional
    @PostMapping("/upload-url")
    public Video uploadVideoUrl(
            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
            @RequestParam("videoUrl") String videoUrl,
            @RequestParam("title") String title,
            @RequestParam(value = "description", defaultValue = "") String description,
            @RequestParam(value = "batchId", required = false) Long batchId,  // ✅ required=false
            @RequestParam(value = "tags",        defaultValue = "") String tags,
            @RequestParam(value = "category",    defaultValue = "") String category,
            @RequestParam(value = "language",    defaultValue = "English") String language,
            @RequestParam(value = "visibility",  defaultValue = "public") String visibility,
            @RequestParam(value = "audience",    defaultValue = "not-kids") String audience,
            @RequestParam(value = "ageRestrict", defaultValue = "false") boolean ageRestrict,
            @RequestParam(value = "course",      defaultValue = "") String course,
            @RequestParam(value = "status",      defaultValue = "published") String status
    ) throws Exception {
        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
        return service.uploadVideoByUrl(
                videoUrl, title, description, batchId,
                tags, category, language, visibility, audience, ageRestrict, course, status,
                organizationId
        );
    }




    @GetMapping("/play/{fileName}")
    public ResponseEntity<byte[]> playVideo(
            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
            @PathVariable String fileName
    ) throws Exception {

        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
        byte[] videoBytes = service.getVideoFile(fileName, organizationId);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + fileName + "\"")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .contentType(MediaType.valueOf("video/mp4"))
                .body(videoBytes);
    }

    @GetMapping("/{id}")
    public Video getVideoInfo(
            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
            @PathVariable Long id
    ) {
        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
        return service.getVideoMeta(id, organizationId);
    }

    // ⚠️ ADMIN ONLY (later we secure)
    @GetMapping
    public List<Video> listVideos(
            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
            @RequestParam(value = "type", required = false) String type   // ✅ NEW — UPLOADED_FILE | YOUTUBE | VIMEO | DIRECT_URL | ALL
    ) {
        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
        return service.getAllVideos(organizationId, type);
    }

 // 🎓 STUDENT DASHBOARD
    @GetMapping("/student")
    public List<Video> getStudentVideos(
            @RequestHeader("Authorization") String authHeader   // ✅ NEW
    ) {
        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
        return service.getVideosForStudent(organizationId);
    }

 // 👨‍🏫 TRAINER DASHBOARD //trainer upaoded getting api
    @GetMapping("/trainer")
    public List<Video> getTrainerVideos(
            @RequestHeader("Authorization") String authHeader   // ✅ NEW
    ) {
        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
        return service.getVideosForTrainer(organizationId);
    }


    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(
            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
            @PathVariable Long id
    ) {
        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
        service.deleteVideo(id, organizationId);
        return ResponseEntity.noContent().build();
    }
 // ✅ NEW — trainer assigns a batch to an existing video
    @PatchMapping("/{id}/assign-batch")
    public ResponseEntity<Video> assignBatch(
            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
            @PathVariable Long id,
            @RequestParam("batchId") Long batchId
    ) {
        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
        Video updated = service.assignBatchToVideo(id, batchId, organizationId);
        return ResponseEntity.ok(updated);
    }

    @PatchMapping("/{id}/publish")
    public ResponseEntity<Video> publishVideo(
            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
            @PathVariable Long id
    ) {
        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
        Video updated = service.publishVideo(id, organizationId);
        return ResponseEntity.ok(updated);
    }

    /**
     * PUT /api/video/{id}/edit
     * Edit a file-upload video. Send as multipart/form-data.
     * "file" part is OPTIONAL — omit it to keep the existing stored file.
     */
    @PutMapping(value = "/{id}/edit", consumes = org.springframework.http.MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Video> editVideo(
            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
            @PathVariable Long id,
            @RequestParam(value = "file",        required = false) MultipartFile file,
            @RequestParam("title")         String title,
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
        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
        Video updated = service.editVideo(
                id, file, title, description, batchId,
                tags, category, language, visibility, audience, ageRestrict, course, status,
                organizationId
        );
        return ResponseEntity.ok(updated);
    }

    /**
     * PUT /api/video/{id}/edit-url
     * Edit a URL-based video. Send as multipart/form-data (or application/x-www-form-urlencoded).
     * "videoUrl" is OPTIONAL — omit to keep the existing URL.
     */
    @PutMapping("/{id}/edit-url")
    public ResponseEntity<Video> editVideoUrl(
            @RequestHeader("Authorization") String authHeader,   // ✅ NEW
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
        String organizationId = orgIdFrom(authHeader);   // ✅ NEW
        Video updated = service.editVideoByUrl(
                id, videoUrl, title, description, batchId,
                tags, category, language, visibility, audience, ageRestrict, course, status,
                organizationId
        );
        return ResponseEntity.ok(updated);
    }

}