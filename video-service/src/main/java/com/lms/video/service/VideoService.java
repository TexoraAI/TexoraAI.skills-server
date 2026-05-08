package com.lms.video.service;

import com.lms.video.kafka.VideoProducer;
import com.lms.video.model.Video;
import com.lms.video.model.TrainerBatchMap;
import com.lms.video.repository.VideoRepository;
import com.lms.video.repository.TrainerBatchMapRepository;
import com.lms.video.repository.StudentBatchMapRepository;
import com.lms.video.model.StudentBatchMap;

import org.springframework.beans.factory.annotation.Value;
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

    public VideoService(VideoRepository repo,
            VideoProducer videoProducer,
            TrainerBatchMapRepository trainerBatchMapRepository,
            StudentBatchMapRepository studentBatchMapRepository) {
this.repo = repo;
this.videoProducer = videoProducer;
this.trainerBatchMapRepository = trainerBatchMapRepository;
this.studentBatchMapRepository = studentBatchMapRepository;
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
            String status 
    ) throws Exception {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        
        
        // ✅ Only check batch ownership if batchId is provided
        if (batchId != null) {
            boolean allowed = trainerBatchMapRepository
                    .findByTrainerEmailAndBatchId(email, batchId)
                    .isPresent();

            System.out.println("LOGIN USER = " + email);
            System.out.println("BATCH ID = " + batchId);

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
        video.setTags(tags != null ? tags : "");
        video.setCategory(category != null ? category : "");
        video.setLanguage(language != null ? language : "English");
        video.setVisibility(visibility != null ? visibility : "public");
        video.setAudience(audience != null ? audience : "not-kids");
        video.setAgeRestrict(ageRestrict);
        video.setCourse(course != null ? course : "");
        video.setStatus(status != null ? status : "draft"); 
        Video saved = repo.save(video);

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
            String status 
    ) throws Exception {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // ✅ Only check batch ownership if batchId is provided
        if (batchId != null) {
            boolean allowed = trainerBatchMapRepository
                    .findByTrainerEmailAndBatchId(email, batchId)
                    .isPresent();

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
    
    
    public byte[] getVideoFile(String fileName) throws Exception {
        Path path = Paths.get(uploadDir).resolve(fileName);
        return Files.readAllBytes(path);
    }

    public Video getVideoMeta(Long id) {
        return repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));
    }

    public List<Video> getAllVideos() {
        return repo.findAll(Sort.by(Sort.Direction.DESC, "uploadedAt"));
    }

    public void deleteVideo(Long id) {

        Video video = repo.findById(id)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        Path videoPath = Paths.get(uploadDir).resolve(video.getStoredFileName());

        try {
            Files.deleteIfExists(videoPath);
        } catch (IOException e) {
            throw new RuntimeException("Failed to delete video file", e);
        }

        repo.delete(video);

        try {
            videoProducer.sendVideoDeletedEvent(video.getStoredFileName());
        } catch (Exception e) {
            System.out.println("Kafka down. Video deleted without event: " + e.getMessage());
        }
    }
    

    public List<Video> getVideosForStudent() {

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

        // ✅ ONLY this line changes — filter by "published" status
        return repo.findByBatchIdInAndStatus(batchIds, "published");
    }
 
    public List<Video> getVideosForTrainer() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName()
                .trim()
                .toLowerCase();

        return repo.findByUploadedBy(email);
    }
    
    public Video assignBatchToVideo(Long videoId, Long batchId) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // Verify trainer owns the video
        Video video = repo.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        if (!video.uploadedBy().equalsIgnoreCase(email.trim())) {
            throw new RuntimeException("Not your video");
        }

        // Verify trainer is assigned to the new batch
        boolean allowed = trainerBatchMapRepository
                .findByTrainerEmailAndBatchId(email, batchId)
                .isPresent();

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
    
    
    public Video publishVideo(Long videoId) {
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        Video video = repo.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

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
 // ═══════════════════════════════════════════════════════════════════
//  ADD THESE TWO METHODS to your existing VideoService.java
//  (paste anywhere after the existing methods, before the last closing brace)
// ═══════════════════════════════════════════════════════════════════

    /**
     * Edit a file-upload video.
     * - file is OPTIONAL: if null, the old stored file is kept.
     * - All other metadata fields are always updated.
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
            String status
    ) throws Exception {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName()
                .trim()
                .toLowerCase();

        Video video = repo.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        // Only the uploader may edit
        if (!video.uploadedBy().equalsIgnoreCase(email)) {
            throw new RuntimeException("Not your video");
        }

        // If a new batchId is supplied, verify the trainer is assigned to it
        if (batchId != null) {
            boolean allowed = trainerBatchMapRepository
                    .findByTrainerEmailAndBatchId(email, batchId)
                    .isPresent();
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
            String status
    ) throws Exception {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName()
                .trim()
                .toLowerCase();

        Video video = repo.findById(videoId)
                .orElseThrow(() -> new RuntimeException("Video not found"));

        if (!video.uploadedBy().equalsIgnoreCase(email)) {
            throw new RuntimeException("Not your video");
        }

        if (batchId != null) {
            boolean allowed = trainerBatchMapRepository
                    .findByTrainerEmailAndBatchId(email, batchId)
                    .isPresent();
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

        return repo.save(video);
    }

}
