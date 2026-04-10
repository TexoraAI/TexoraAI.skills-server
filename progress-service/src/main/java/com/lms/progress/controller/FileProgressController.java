
package com.lms.progress.controller;
 
import com.lms.progress.dto.FileProgressResponse;
import com.lms.progress.service.FileProgressService;
import org.springframework.web.bind.annotation.*;
 
@RestController
@RequestMapping("/api/file-progress")   // ✅ separate path — not mixed with course or video progress
public class FileProgressController {
 
    private final FileProgressService service;
 
    public FileProgressController(FileProgressService service) {
        this.service = service;
    }
 
    // ============================
    // GET — load existing download progress on page load
    // Called by Documents.jsx useEffect on mount
    // GET /api/file-progress/user?email=x&batchId=54
    // Returns 0% progress instead of error if student hasn't started
    // ============================
    @GetMapping("/user")
    public FileProgressResponse getProgress(
            @RequestParam String email,
            @RequestParam Long   batchId) {
 
        return service.getByStudentAndBatch(email, batchId);
    }
 
    // ============================
    // POST — mark a file as downloaded/previewed
    // Called by Documents.jsx when student clicks Preview button
    // POST /api/file-progress/mark-downloaded?email=x&batchId=54&fileId=12&totalFileCount=5
    // Uses batchId — NOT courseId — files belong to batch not course
    // ============================
    @PostMapping("/mark-downloaded")
    public FileProgressResponse markDownloaded(
            @RequestParam String email,
            @RequestParam Long   batchId,
            @RequestParam Long   fileId,
            @RequestParam int    totalFileCount) {
 
        return service.markFileDownloaded(email, batchId, fileId, totalFileCount);
    }
}