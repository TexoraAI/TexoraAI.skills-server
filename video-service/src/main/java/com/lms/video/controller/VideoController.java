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
//    // ✅ FIXED + title & description supported
//    @PostMapping("/upload")
//    public Video uploadVideo(
//            @RequestParam("file") MultipartFile file,
//            @RequestParam("title") String title,
//            @RequestParam("description") String description
//    ) throws Exception {
//
//        return service.uploadVideo(file, title, description);
//    }
//
//    @GetMapping("/play/{fileName}")
//    public ResponseEntity<byte[]> playVideo(@PathVariable String fileName) throws Exception {
//
//        byte[] videoBytes = service.getVideoFile(fileName);
//
//        return ResponseEntity.ok()
//                .header(
//                        HttpHeaders.CONTENT_DISPOSITION,
//                        "inline; filename=\"" + fileName + "\""
//                )
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
//    @GetMapping
//    public List<Video> listVideos() {
//        return service.getAllVideos();
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
//        service.deleteVideo(id);
//        return ResponseEntity.noContent().build();
//    }
//}


package com.lms.video.controller;

import com.lms.video.model.Video;
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

    public VideoController(VideoService service) {
        this.service = service;
    }

    // 🔥 NOW TRAINER MUST PROVIDE batchId
    @PostMapping("/upload")
    public Video uploadVideo(
            @RequestParam("file") MultipartFile file,
            @RequestParam("title") String title,
            @RequestParam("description") String description,
            @RequestParam("batchId") Long batchId
    ) throws Exception {

        return service.uploadVideo(file, title, description, batchId);
    }

    @GetMapping("/play/{fileName}")
    public ResponseEntity<byte[]> playVideo(@PathVariable String fileName) throws Exception {

        byte[] videoBytes = service.getVideoFile(fileName);

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "inline; filename=\"" + fileName + "\"")
                .header(HttpHeaders.ACCEPT_RANGES, "bytes")
                .contentType(MediaType.valueOf("video/mp4"))
                .body(videoBytes);
    }

    @GetMapping("/{id}")
    public Video getVideoInfo(@PathVariable Long id) {
        return service.getVideoMeta(id);
    }

    // ⚠️ ADMIN ONLY (later we secure)
    @GetMapping
    public List<Video> listVideos() {
        return service.getAllVideos();
    }

 // 🎓 STUDENT DASHBOARD
    @GetMapping("/student")
    public List<Video> getStudentVideos() {
        return service.getVideosForStudent();
    }

 // 👨‍🏫 TRAINER DASHBOARD //trainer upaoded getting api 
    @GetMapping("/trainer")
    public List<Video> getTrainerVideos() {
        return service.getVideosForTrainer();
    }

    
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteVideo(@PathVariable Long id) {
        service.deleteVideo(id);
        return ResponseEntity.noContent().build();
    }
}
