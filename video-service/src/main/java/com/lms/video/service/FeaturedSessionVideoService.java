package com.lms.video.service;

import com.lms.video.kafka.FeaturedVideoKafkaProducer;
import com.lms.video.model.FeaturedSessionVideo;
import com.lms.video.model.FeaturedVideoStatus;
import com.lms.video.repository.FeaturedSessionVideoRepository;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import com.lms.video.model.TranscriptSourceType;
import com.lms.video.repository.FeaturedVideoTranscriptRepository;
import com.lms.video.repository.FeaturedTranscriptSegmentRepository;
import org.springframework.beans.factory.annotation.Value;

import java.io.IOException;
import java.time.Duration;
import java.util.NoSuchElementException;

@Service
public class FeaturedSessionVideoService {

    // ✅ unchanged — this is the property already proven correct in production
    @Value("${app.public-api-url}")
    private String publicApiUrl;

    private final FeaturedSessionVideoRepository repo;
    private final FeaturedVideoKafkaProducer featuredVideoKafkaProducer;
    private final TranscriptGenerationService transcriptGenerationService;
    private final FeaturedVideoTranscriptRepository transcriptRepo;
    private final FeaturedTranscriptSegmentRepository segmentRepo;
    private final S3Service s3Service;

    // ✅ per-course folders instead of flat prefixes
    private static final String BASE_S3_PREFIX = "featured-courses/";
    private static final String VIDEO_SUBFOLDER = "/videos/";
    private static final String THUMB_SUBFOLDER = "/thumbnails/";

    public FeaturedSessionVideoService(FeaturedSessionVideoRepository repo,
                                        FeaturedVideoKafkaProducer featuredVideoKafkaProducer,
                                        TranscriptGenerationService transcriptGenerationService,
                                        FeaturedVideoTranscriptRepository transcriptRepo,
                                        FeaturedTranscriptSegmentRepository segmentRepo,
                                        S3Service s3Service) {
        this.repo = repo;
        this.featuredVideoKafkaProducer = featuredVideoKafkaProducer;
        this.transcriptGenerationService = transcriptGenerationService;
        this.transcriptRepo = transcriptRepo;
        this.segmentRepo = segmentRepo;
        this.s3Service = s3Service;
    }

    private String safeSlug(String courseSlug) {
        return (courseSlug == null || courseSlug.isBlank())
                ? "unassigned"
                : courseSlug.toLowerCase().replaceAll("[^a-z0-9-]+", "-");
    }

    private String buildVideoKey(String courseSlug, String fileName) {
        return BASE_S3_PREFIX + safeSlug(courseSlug) + VIDEO_SUBFOLDER + fileName;
    }

    private String buildThumbKey(String courseSlug, String fileName) {
        return BASE_S3_PREFIX + safeSlug(courseSlug) + THUMB_SUBFOLDER + fileName;
    }

    // ================= UPLOAD =================
    public FeaturedSessionVideo upload(MultipartFile file, Long sessionId,
                                        String title, String description,
                                        MultipartFile thumbnail, String courseSlug) throws IOException {
        try {
            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            String videoKey = buildVideoKey(courseSlug, fileName);
            s3Service.uploadFile(videoKey, file);

            String presignedUrl = s3Service.generatePresignedUrl(videoKey, Duration.ofHours(2));

            FeaturedSessionVideo video = new FeaturedSessionVideo();
            video.setSessionId(sessionId);
            video.setFileName(fileName);
            video.setS3Key(videoKey);
            video.setUrl(publicApiUrl + "/api/video/v1/featured/session/stream/" + fileName);
            video.setTitle(title);
            video.setDescription(description);

            if (thumbnail != null && !thumbnail.isEmpty()) {
                String thumbName = System.currentTimeMillis() + "_thumb_" + thumbnail.getOriginalFilename();
                String thumbKey = buildThumbKey(courseSlug, thumbName);
                s3Service.uploadFile(thumbKey, thumbnail);
                video.setThumbnailFileName(thumbName);
                video.setThumbnailS3Key(thumbKey);
                video.setThumbnailUrl(publicApiUrl + "/api/video/v1/featured/session/stream/" + thumbName);
            } else {
                video.setThumbnailUrl(null);
            }

            video.setStatus(FeaturedVideoStatus.READY);

            FeaturedSessionVideo saved = repo.save(video);

            try {
                transcriptGenerationService.generateAsync(sessionId, presignedUrl);
            } catch (Exception ignored) {
            }

            featuredVideoKafkaProducer.publishVideoReady(
                    sessionId,
                    saved.getId().toString(),
                    saved.getUrl(),
                    saved.getThumbnailUrl(),
                    saved.getDurationSeconds(),
                    saved.getTitle(),
                    saved.getDescription());

            return saved;

        } catch (IOException e) {
            featuredVideoKafkaProducer.publishVideoFailed(sessionId, e.getMessage());
            throw e;
        }
    }

    // ================= GET BY SESSION =================
    public FeaturedSessionVideo getBySession(Long sessionId) {
        return repo.findBySessionId(sessionId)
                .orElseThrow(() -> new NoSuchElementException("No video for session " + sessionId));
    }

    // ================= UPDATE =================
    public FeaturedSessionVideo update(Long id, String title, String description,
                                        MultipartFile thumbnail, MultipartFile newVideo,
                                        String courseSlug) throws IOException {
        FeaturedSessionVideo video = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Video not found: " + id));

        if (title != null) video.setTitle(title);
        if (description != null) video.setDescription(description);

        if (thumbnail != null && !thumbnail.isEmpty()) {
            if (video.getThumbnailS3Key() != null) {
                try {
                    s3Service.deleteFile(video.getThumbnailS3Key());
                } catch (Exception e) {
                    System.out.println("Could not delete old featured thumbnail from S3: " + e.getMessage());
                }
            }
            String thumbName = System.currentTimeMillis() + "_thumb_" + thumbnail.getOriginalFilename();
            String thumbKey = buildThumbKey(courseSlug, thumbName);
            s3Service.uploadFile(thumbKey, thumbnail);
            video.setThumbnailFileName(thumbName);
            video.setThumbnailS3Key(thumbKey);
            video.setThumbnailUrl(publicApiUrl + "/api/video/v1/featured/session/stream/" + thumbName);
        }

        if (newVideo != null && !newVideo.isEmpty()) {
            if (video.getS3Key() != null) {
                try {
                    s3Service.deleteFile(video.getS3Key());
                } catch (Exception e) {
                    System.out.println("Could not delete old featured video from S3: " + e.getMessage());
                }
            }
            String fileName = System.currentTimeMillis() + "_" + newVideo.getOriginalFilename();
            String videoKey = buildVideoKey(courseSlug, fileName);
            s3Service.uploadFile(videoKey, newVideo);
            video.setFileName(fileName);
            video.setS3Key(videoKey);
            video.setUrl(publicApiUrl + "/api/video/v1/featured/session/stream/" + fileName);
            video.setDurationSeconds(null);
            video.setStatus(FeaturedVideoStatus.READY);
        }

        return repo.save(video);
    }

    // ================= DELETE =================
    public void delete(Long id) {
        FeaturedSessionVideo video = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Video not found: " + id));

        if (video.getS3Key() != null) {
            try {
                s3Service.deleteFile(video.getS3Key());
            } catch (Exception ignored) {
            }
        }

        if (video.getThumbnailS3Key() != null) {
            try {
                s3Service.deleteFile(video.getThumbnailS3Key());
            } catch (Exception ignored) {
            }
        }

        repo.delete(video);
        transcriptRepo.findBySessionIdAndSourceType(id, TranscriptSourceType.FEATURED)
            .ifPresent(t -> {
                segmentRepo.deleteAll(segmentRepo.findByTranscriptIdOrderByOrderIndexAsc(t.getId()));
                transcriptRepo.delete(t);
            });
    }

    // ================= PLAYBACK / THUMBNAIL URL =================
    public String getPlaybackUrl(String fileName) {
        FeaturedSessionVideo byVideo = repo.findByFileName(fileName).orElse(null);
        if (byVideo != null) {
            return s3Service.generatePresignedUrl(byVideo.getS3Key(), Duration.ofHours(2));
        }
        FeaturedSessionVideo byThumb = repo.findByThumbnailFileName(fileName).orElse(null);
        if (byThumb != null) {
            return s3Service.generatePresignedUrl(byThumb.getThumbnailS3Key(), Duration.ofHours(2));
        }
        throw new NoSuchElementException("No S3 object found for fileName: " + fileName);
    }
}