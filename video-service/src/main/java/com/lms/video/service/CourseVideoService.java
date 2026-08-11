//
//
//package com.lms.video.service;
//
//import com.lms.video.model.CourseVideo;
//import com.lms.video.model.TranscriptSourceType;
//import com.lms.video.repository.CourseVideoRepository;
//import com.lms.video.repository.FeaturedTranscriptSegmentRepository;
//import com.lms.video.repository.FeaturedVideoTranscriptRepository;
//import com.lms.video.model.TranscriptSourceType;
//import com.lms.video.repository.FeaturedVideoTranscriptRepository;
//import com.lms.video.repository.FeaturedTranscriptSegmentRepository;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//
//import java.io.File;
//import java.io.IOException;
//import java.nio.file.Files;
//import java.nio.file.Path;
//import java.nio.file.Paths;
//
//@Service
//public class CourseVideoService {
//
//    private final CourseVideoRepository repo;
//    private final TranscriptGenerationService transcriptGenerationService;
//    private final FeaturedVideoTranscriptRepository transcriptRepo;
//    private final FeaturedTranscriptSegmentRepository segmentRepo;
//    private static final String VIDEO_DIR =
//            System.getProperty("user.dir") + "/videos/course-content/";
//
//    public CourseVideoService(CourseVideoRepository repo,
//            TranscriptGenerationService transcriptGenerationService,
//            FeaturedVideoTranscriptRepository transcriptRepo,
//            FeaturedTranscriptSegmentRepository segmentRepo) {
//        this.repo = repo;
//        this.transcriptGenerationService = transcriptGenerationService;
//        this.transcriptRepo = transcriptRepo;
//        this.segmentRepo = segmentRepo;
//    }
//
//    // ================= UPLOAD =================
//    public CourseVideo upload(
//            MultipartFile file,
//            Long courseId,
//            Long moduleId,
//            Long batchId,
//            String email
//    ) throws IOException {
//
//        File directory = new File(VIDEO_DIR);
//        if (!directory.exists()) {
//            directory.mkdirs();
//        }
//
//        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
//        Path path = Paths.get(VIDEO_DIR + fileName);
//        Files.copy(file.getInputStream(), path);
//
//        CourseVideo video = new CourseVideo();
//        video.setCourseId(courseId);
//        video.setModuleId(moduleId);
//        video.setBatchId(batchId);
//        video.setFileName(fileName);
//        video.setUrl("http://localhost:9000/api/course-videos/stream/" + fileName);
//        video.setUploadedBy(email);
//
//        return repo.save(video);
//        
//    }
//
//    // ================= EDIT (replace file + keep metadata) =================
//    public CourseVideo update(
//            Long id,
//            MultipartFile newFile,   // nullable — if null, keep existing file
//            Long courseId,
//            Long moduleId,
//            Long batchId,
//            String email
//    ) throws IOException {
//
//        CourseVideo existing = repo.findById(id)
//                .orElseThrow(() -> new RuntimeException("Course video not found: " + id));
//
//        if (newFile != null && !newFile.isEmpty()) {
//
//            // 1. Delete old file from disk
//            File oldFile = new File(VIDEO_DIR + existing.getFileName());
//            if (oldFile.exists()) {
//                oldFile.delete();
//            }
//
//            // 2. Ensure directory exists
//            File directory = new File(VIDEO_DIR);
//            if (!directory.exists()) {
//                directory.mkdirs();
//            }
//
//            // 3. Save new file
//            String newFileName = System.currentTimeMillis() + "_" + newFile.getOriginalFilename();
//            Path newPath = Paths.get(VIDEO_DIR + newFileName);
//            Files.copy(newFile.getInputStream(), newPath);
//
//            existing.setFileName(newFileName);
//            existing.setUrl("http://localhost:9000/api/course-videos/stream/" + newFileName);
//        }
//
//        // Update metadata (only if non-null values are provided)
//        if (courseId != null)  existing.setCourseId(courseId);
//        if (moduleId != null)  existing.setModuleId(moduleId);
//        if (batchId  != null)  existing.setBatchId(batchId);
//        if (email    != null)  existing.setUploadedBy(email);
//
//        return repo.save(existing);
//    }
//
//    // ================= DELETE =================
//    public void deleteById(Long id) {
//
//        CourseVideo video = repo.findById(id)
//                .orElseThrow(() -> new RuntimeException("Course video not found: " + id));
//
//        File file = new File(VIDEO_DIR + video.getFileName());
//        if (file.exists()) {
//            file.delete();
//        }
//
//        repo.delete(video);
//    }
//}
package com.lms.video.service;

import com.lms.video.model.CourseVideo;
import com.lms.video.model.TranscriptSourceType;
import com.lms.video.repository.CourseVideoRepository;
import com.lms.video.repository.FeaturedTranscriptSegmentRepository;
import com.lms.video.repository.FeaturedVideoTranscriptRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class CourseVideoService {

    private final CourseVideoRepository repo;
    private final TranscriptGenerationService transcriptGenerationService;
    private final FeaturedVideoTranscriptRepository transcriptRepo;
    private final FeaturedTranscriptSegmentRepository segmentRepo;
    private static final String VIDEO_DIR =
            System.getProperty("user.dir") + "/videos/course-content/";

    public CourseVideoService(CourseVideoRepository repo,
            TranscriptGenerationService transcriptGenerationService,
            FeaturedVideoTranscriptRepository transcriptRepo,
            FeaturedTranscriptSegmentRepository segmentRepo) {
        this.repo = repo;
        this.transcriptGenerationService = transcriptGenerationService;
        this.transcriptRepo = transcriptRepo;
        this.segmentRepo = segmentRepo;
    }

    // ================= UPLOAD =================
    public CourseVideo upload(
            MultipartFile file,
            Long courseId,
            Long moduleId,
            Long batchId,
            String email
    ) throws IOException {

        File directory = new File(VIDEO_DIR);
        if (!directory.exists()) {
            directory.mkdirs();
        }

        String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
        Path path = Paths.get(VIDEO_DIR + fileName);
        Files.copy(file.getInputStream(), path);

        CourseVideo video = new CourseVideo();
        video.setCourseId(courseId);
        video.setModuleId(moduleId);
        video.setBatchId(batchId);
        video.setFileName(fileName);
        video.setUrl("http://localhost:9000/api/course-videos/stream/" + fileName);
        video.setUploadedBy(email);

        CourseVideo saved = repo.save(video);

        try {
            transcriptGenerationService.generateAsync(
                    saved.getId(), path.toString(), TranscriptSourceType.COURSE_VIDEO);
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
            String email
    ) throws IOException {

        CourseVideo existing = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Course video not found: " + id));

        if (newFile != null && !newFile.isEmpty()) {

            // 1. Delete old file from disk
            File oldFile = new File(VIDEO_DIR + existing.getFileName());
            if (oldFile.exists()) {
                oldFile.delete();
            }

            // 2. Ensure directory exists
            File directory = new File(VIDEO_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // 3. Save new file
            String newFileName = System.currentTimeMillis() + "_" + newFile.getOriginalFilename();
            Path newPath = Paths.get(VIDEO_DIR + newFileName);
            Files.copy(newFile.getInputStream(), newPath);

            existing.setFileName(newFileName);
            existing.setUrl("http://localhost:9000/api/course-videos/stream/" + newFileName);

            // New file content means the old transcript no longer matches —
            // delete stale transcript/segments and kick off a fresh job.
            transcriptRepo.findBySessionIdAndSourceType(id, TranscriptSourceType.COURSE_VIDEO)
                    .ifPresent(t -> {
                        segmentRepo.deleteAll(segmentRepo.findByTranscriptIdOrderByOrderIndexAsc(t.getId()));
                        transcriptRepo.delete(t);
                    });

            try {
                transcriptGenerationService.generateAsync(
                        id, newPath.toString(), TranscriptSourceType.COURSE_VIDEO);
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

        File file = new File(VIDEO_DIR + video.getFileName());
        if (file.exists()) {
            file.delete();
        }

        repo.delete(video);

        transcriptRepo.findBySessionIdAndSourceType(id, TranscriptSourceType.COURSE_VIDEO)
                .ifPresent(t -> {
                    segmentRepo.deleteAll(segmentRepo.findByTranscriptIdOrderByOrderIndexAsc(t.getId()));
                    transcriptRepo.delete(t);
                });
    }
}