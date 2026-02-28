//
//
//
//package com.lms.video.service;
//
//import com.lms.video.kafka.VideoProducer;
//import com.lms.video.model.Video;
//import com.lms.video.repository.VideoRepository;
//import org.springframework.beans.factory.annotation.Value;
//import org.springframework.stereotype.Service;
//import org.springframework.web.multipart.MultipartFile;
//import org.springframework.data.domain.Sort;
//
//import java.io.IOException;
//import java.nio.file.*;
//import java.util.List;
//
//@Service
//public class VideoService {
//
//    @Value("${video.upload-dir}")
//    private String uploadDir;
//
//    private final VideoRepository repo;
//    private final VideoProducer videoProducer;
//
//    public VideoService(VideoRepository repo, VideoProducer videoProducer) {
//        this.repo = repo;
//        this.videoProducer = videoProducer;
//    }
//
//    // ✅ UPDATED: title & description added
//    public Video uploadVideo(
//            MultipartFile file,
//            String title,
//            String description
//    ) throws Exception {
//
//        Path directory = Paths.get(uploadDir);
//
//        if (!Files.exists(directory)) {
//            Files.createDirectories(directory);
//        }
//
//        String storedFileName =
//                System.currentTimeMillis() + "_" + file.getOriginalFilename();
//
//        Path filePath = directory.resolve(storedFileName);
//
//        Files.copy(
//                file.getInputStream(),
//                filePath,
//                StandardCopyOption.REPLACE_EXISTING
//        );
//
//        Video video = new Video();
//        video.setTitle(title);                 // ✅ NEW
//        video.setDescription(description);     // ✅ NEW
//        video.setOriginalFileName(file.getOriginalFilename());
//        video.setStoredFileName(storedFileName);
//        video.setSize(file.getSize());
//
//        Video saved = repo.save(video);
//
//        // 🔥 Kafka upload event (unchanged)
//        videoProducer.sendVideoUploadedEvent(storedFileName);
//
//        return saved;
//    }
//
//    public byte[] getVideoFile(String fileName) throws Exception {
//        Path path = Paths.get(uploadDir).resolve(fileName);
//        return Files.readAllBytes(path);
//    }
//
//    public Video getVideoMeta(Long id) {
//        return repo.findById(id)
//                .orElseThrow(() -> new RuntimeException("Video not found"));
//    }
//
//    public List<Video> getAllVideos() {
//        return repo.findAll(
//                Sort.by(Sort.Direction.DESC, "uploadedAt")
//        );
//    }
//
//    public void deleteVideo(Long id) {
//
//        Video video = repo.findById(id)
//                .orElseThrow(() -> new RuntimeException("Video not found"));
//
//        Path videoPath =
//                Paths.get(uploadDir).resolve(video.getStoredFileName());
//
//        try {
//            Files.deleteIfExists(videoPath);
//        } catch (IOException e) {
//            throw new RuntimeException("Failed to delete video file", e);
//        }
//
//        repo.delete(video);
//
//        try {
//            videoProducer.sendVideoDeletedEvent(video.getStoredFileName());
//        } catch (Exception e) {
//            System.out.println(
//                "Kafka down. Video deleted without event: " + e.getMessage()
//            );
//        }
//    }
//}


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


    // 🔥 NOW BATCH SECURED UPLOAD
    public Video uploadVideo(
            MultipartFile file,
            String title,
            String description,
            Long batchId
    ) throws Exception {
    	
    	


        // 1️⃣ Get logged trainer email from JWT
        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName();

        // 2️⃣ Check trainer owns this batch (from Kafka cache table)
        boolean allowed = trainerBatchMapRepository
                .findByTrainerEmailAndBatchId(email, batchId)
                .isPresent();

        System.out.println("LOGIN USER = " + email);
    	System.out.println("BATCH ID = " + batchId);
        
        if (!allowed) {
            throw new RuntimeException("You are not assigned to this batch");
        }

        // 3️⃣ Save video file
        Path directory = Paths.get(uploadDir);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }

        String storedFileName =
                System.currentTimeMillis() + "_" + file.getOriginalFilename();

        Path filePath = directory.resolve(storedFileName);

        Files.copy(
                file.getInputStream(),
                filePath,
                StandardCopyOption.REPLACE_EXISTING
        );

        // 4️⃣ Save DB record
        Video video = new Video();
        video.setTitle(title);
        video.setDescription(description);
        video.setOriginalFileName(file.getOriginalFilename());
        video.setStoredFileName(storedFileName);
        video.setSize(file.getSize());
        video.setBatchId(batchId); // ⭐ IMPORTANT
        video.setUploadedBy(email.trim().toLowerCase());
        Video saved = repo.save(video);

        // Kafka event unchanged
        videoProducer.sendVideoUploadedEvent(storedFileName);

        return saved;
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
    
// // 🔥 STUDENT DASHBOARD VIDEOS
//    public List<Video> getVideosForStudent() {
//
//        String email = SecurityContextHolder
//                .getContext()
//                .getAuthentication()
//                .getName()
//                .trim()
//                .toLowerCase();
//
//        return studentBatchMapRepository
//                .findByStudentEmail(email)
//                .map(map -> repo.findByBatchId(map.getBatchId()))
//                .orElse(Collections.emptyList());
//    }
    //🔥 STUDENT DASHBOARD VIDEOS
    public List<Video> getVideosForStudent() {

        String email = SecurityContextHolder
                .getContext()
                .getAuthentication()
                .getName()
                .trim()
                .toLowerCase();

        // 1️⃣ Get all batches of student
        List<StudentBatchMap> mappings =
                studentBatchMapRepository.findAllByStudentEmail(email);

        if (mappings.isEmpty()) {
            return Collections.emptyList();
        }

        // 2️⃣ Extract batch IDs
        List<Long> batchIds = mappings.stream()
                .map(StudentBatchMap::getBatchId)
                .toList();

        // 3️⃣ Fetch videos from all batches
        return repo.findByBatchIdIn(batchIds);
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

}
