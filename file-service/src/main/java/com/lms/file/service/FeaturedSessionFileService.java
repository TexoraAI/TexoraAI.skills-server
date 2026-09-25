package com.lms.file.service;

import com.lms.file.kafka.FeaturedFileKafkaProducer;
import com.lms.file.model.FeaturedSessionFile;
import com.lms.file.repository.FeaturedSessionFileRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.time.Duration;
import java.time.Instant;

@Service
public class FeaturedSessionFileService {

    private final FeaturedSessionFileRepository repo;
    private final FeaturedFileKafkaProducer kafkaProducer;
    private final S3Service s3Service;

    // ✅ same as video-service — real public gateway URL, not localhost
    @Value("${gateway.base-url}")
    private String publicApiUrl;

    // ✅ per-course folder, mirrors video's VIDEO_S3_PREFIX pattern
    private static final String BASE_S3_PREFIX = "featured-courses/";
    private static final String FILE_SUBFOLDER = "/files/";

    public FeaturedSessionFileService(FeaturedSessionFileRepository repo,
                                       FeaturedFileKafkaProducer kafkaProducer,
                                       S3Service s3Service) {
        this.repo = repo;
        this.kafkaProducer = kafkaProducer;
        this.s3Service = s3Service;
    }

    private String buildS3Key(String courseSlug, String fileName) {
        String safeSlug = (courseSlug == null || courseSlug.isBlank())
                ? "unassigned"
                : courseSlug.toLowerCase().replaceAll("[^a-z0-9-]+", "-");
        return BASE_S3_PREFIX + safeSlug + FILE_SUBFOLDER + fileName;
    }

    // ================= UPLOAD (direct from frontend) =================
    public FeaturedSessionFile upload(MultipartFile file, Long sessionId, String courseSlug) {
        try {
            // replace existing file for this session, if any
            repo.findBySessionId(sessionId).ifPresent(existing -> {
                if (existing.getS3Key() != null) {
                    try {
                        s3Service.deleteFile(existing.getS3Key());
                    } catch (Exception e) {
                        System.out.println("⚠️ Could not delete old featured file from S3: " + e.getMessage());
                    }
                }
                repo.delete(existing);
            });

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String s3Key = buildS3Key(courseSlug, fileName);

            s3Service.uploadFile(s3Key, file);

            String url = publicApiUrl + "/api/featured-files/stream/" + fileName;

            FeaturedSessionFile record = new FeaturedSessionFile();
            record.setSessionId(sessionId);
            record.setFileName(fileName);
            record.setS3Key(s3Key);          // ✅ NEW field — needed to reconstruct path later
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

    // ================= STREAM / VIEW — fresh presigned redirect =================
    public String getPlaybackUrl(String fileName) {
        FeaturedSessionFile record = repo.findByFileName(fileName)
                .orElseThrow(() -> new RuntimeException("Featured file not found: " + fileName));
        return s3Service.generatePresignedUrl(record.getS3Key(), Duration.ofHours(2));
    }

    // ================= DELETE (called by FeaturedFileKafkaConsumer on FEATURED_FILE_DELETED) =================
    public void deleteByUrl(String url) {
        repo.findByUrl(url).ifPresentOrElse(
            record -> {
                if (record.getS3Key() != null) {
                    try {
                        s3Service.deleteFile(record.getS3Key());
                    } catch (Exception e) {
                        System.out.println("⚠️ Could not delete featured file from S3: " + e.getMessage());
                    }
                }
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
            if (f.getS3Key() != null) {
                try {
                    s3Service.deleteFile(f.getS3Key());
                } catch (Exception e) {
                    System.out.println("⚠️ Could not delete featured file from S3: " + e.getMessage());
                }
            }
        }
        repo.deleteAll(files);
        System.out.println("🧹 Featured files cleaned for sessionIds=" + sessionIds + " count=" + files.size());
    }

    // used by FeaturedSyllabusFileController's old "mark processing" flow, if still needed
    public FeaturedSessionFile getFile(Long sessionId) {
        return repo.findBySessionId(sessionId)
                .orElseThrow(() -> new RuntimeException("No file for session " + sessionId));
    }
}