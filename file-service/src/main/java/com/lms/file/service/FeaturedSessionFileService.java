package com.lms.file.service;

import com.lms.file.kafka.FeaturedFileKafkaProducer;
import com.lms.file.model.FeaturedSessionFile;
import com.lms.file.repository.FeaturedSessionFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.io.IOException;
import java.nio.file.*;
import java.time.Instant;

@Service
public class FeaturedSessionFileService {

    private final FeaturedSessionFileRepository repo;
    private final FeaturedFileKafkaProducer kafkaProducer;

    @Value("${gateway.base-url}")
    private String gatewayBaseUrl;

    private static final String FILE_DIR =
            System.getProperty("user.dir") + "/files/featured-content/";

    public FeaturedSessionFileService(FeaturedSessionFileRepository repo,
                                       FeaturedFileKafkaProducer kafkaProducer) {
        this.repo = repo;
        this.kafkaProducer = kafkaProducer;
    }

    // ================= UPLOAD (direct from frontend, same shape as CourseFileService) =================
    public FeaturedSessionFile upload(MultipartFile file, Long sessionId) {
        try {
            File directory = new File(FILE_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            // if this session already has a file, replace it (delete old first)
            repo.findBySessionId(sessionId).ifPresent(existing -> {
                deleteFileFromDisk(existing.getFileName());
                repo.delete(existing);
            });

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(FILE_DIR + fileName);
            Files.copy(file.getInputStream(), path);

            String url = gatewayBaseUrl + "/api/featured-files/download/" + fileName;

            FeaturedSessionFile record = new FeaturedSessionFile();
            record.setSessionId(sessionId);
            record.setFileName(fileName);
            record.setUrl(url);
            record.setUploadedAt(Instant.now());
            record.setStatus("READY");

            FeaturedSessionFile saved = repo.save(record);

            kafkaProducer.publishFileReady(sessionId, saved.getId().toString(), url, fileName);

            return saved;

        } catch (IOException e) {
            System.out.println("❌ Featured file upload failed for sessionId=" + sessionId + ": " + e.getMessage());
            kafkaProducer.publishFileFailed(sessionId, e.getMessage());
            throw new RuntimeException("Failed to upload featured file", e);
        }
    }

    // ================= DELETE (called by FeaturedFileKafkaConsumer on FEATURED_FILE_DELETED) =================
    public void deleteByUrl(String url) {
        repo.findByUrl(url).ifPresentOrElse(
            record -> {
                deleteFileFromDisk(record.getFileName());
                repo.delete(record);
                System.out.println("🧹 Featured file deleted → url=" + url);
            },
            () -> System.out.println("⚠️ Featured file not found for url=" + url)
        );
    }

    // ================= BULK DELETE (program deleted → cleanup by sessionIds) =================
    public void deleteBySessionIds(java.util.List<Long> sessionIds) {
        java.util.List<FeaturedSessionFile> files = repo.findBySessionIdIn(sessionIds);
        for (FeaturedSessionFile f : files) {
            deleteFileFromDisk(f.getFileName());
        }
        repo.deleteAll(files);
        System.out.println("🧹 Featured files cleaned for sessionIds=" + sessionIds + " count=" + files.size());
    }

    private void deleteFileFromDisk(String fileName) {
        if (fileName == null || fileName.isBlank()) return;
        File file = new File(FILE_DIR + fileName);
        if (file.exists()) {
            boolean deleted = file.delete();
            if (!deleted) {
                System.out.println("⚠️ Could not delete featured file from disk: " + fileName);
            }
        }
    }
}