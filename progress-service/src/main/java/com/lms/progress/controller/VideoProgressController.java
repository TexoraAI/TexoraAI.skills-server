
package com.lms.progress.controller;
 
import com.lms.progress.dto.VideoProgressResponse;
import com.lms.progress.service.VideoProgressService;
import org.springframework.web.bind.annotation.*;
 
@RestController
@RequestMapping("/api/video-progress")  // ✅ separate path — not mixed with course progress
public class VideoProgressController {
 
    private final VideoProgressService service;
 
    public VideoProgressController(VideoProgressService service) {
        this.service = service;
    }
 
    // ============================
    // GET — load existing watch progress on page load
    // Called by VideoLectures.jsx useEffect on mount
    // GET /api/video-progress/user?email=x&batchId=54
    // Returns 0% progress instead of error if student hasn't started
    // ============================
    @GetMapping("/user")
    public VideoProgressResponse getProgress(
            @RequestParam String email,
            @RequestParam Long   batchId) {
 
        return service.getByStudentAndBatch(email, batchId);
    }
 
    // ============================
    // POST — mark a video as watched
    // Called by VideoLectures.jsx when student clicks "Mark as Watched"
    // POST /api/video-progress/mark-watched?email=x&batchId=54&videoId=35&totalVideoCount=5
    // Uses batchId — NOT courseId — videos belong to batch not course
    // ============================
    @PostMapping("/mark-watched")
    public VideoProgressResponse markWatched(
            @RequestParam String email,
            @RequestParam Long   batchId,
            @RequestParam Long   videoId,
            @RequestParam int    totalVideoCount) {
 
        return service.markVideoWatched(email, batchId, videoId, totalVideoCount);
    }
}