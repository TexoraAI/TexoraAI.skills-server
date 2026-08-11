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
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Base64;
import java.util.NoSuchElementException;

@Service
public class FeaturedSessionVideoService {

    private final FeaturedSessionVideoRepository repo;
    private final FeaturedVideoKafkaProducer featuredVideoKafkaProducer;
    private final TranscriptGenerationService transcriptGenerationService;
    private final FeaturedVideoTranscriptRepository transcriptRepo;
    private final FeaturedTranscriptSegmentRepository segmentRepo;
    private static final String VIDEO_DIR =
            System.getProperty("user.dir") + "/videos/featured-content/";

    public FeaturedSessionVideoService(FeaturedSessionVideoRepository repo,
                                        FeaturedVideoKafkaProducer featuredVideoKafkaProducer,
                                        TranscriptGenerationService transcriptGenerationService,
                                        FeaturedVideoTranscriptRepository transcriptRepo,
                                        FeaturedTranscriptSegmentRepository segmentRepo) {
        this.repo = repo;
        this.featuredVideoKafkaProducer = featuredVideoKafkaProducer;
        this.transcriptGenerationService = transcriptGenerationService;
        this.transcriptRepo = transcriptRepo;
        this.segmentRepo = segmentRepo;
    }

    // ================= UPLOAD =================
    public FeaturedSessionVideo upload(MultipartFile file, Long sessionId,
                                        String title, String description,
                                        MultipartFile thumbnail) throws IOException {
        try {
            File directory = new File(VIDEO_DIR);
            if (!directory.exists()) {
                directory.mkdirs();
            }

            String fileName = System.currentTimeMillis() + "_" + file.getOriginalFilename();
            Path path = Paths.get(VIDEO_DIR + fileName);
            Files.copy(file.getInputStream(), path);

            FeaturedSessionVideo video = new FeaturedSessionVideo();
            video.setSessionId(sessionId);
            video.setFileName(fileName);
            video.setUrl("http://localhost:9000/api/video/v1/featured/session/stream/" + fileName);
            video.setTitle(title);
            video.setDescription(description);
            video.setThumbnailUrl(toDataUri(thumbnail));
            video.setStatus(FeaturedVideoStatus.READY);

            FeaturedSessionVideo saved = repo.save(video);

            // Fire-and-forget: transcript generation is fully decoupled from
            // VIDEO_READY and must never affect the upload response. generateAsync()
            // is @Async, so this call returns immediately; the try/catch is just
            // extra insurance against anything (e.g. a DI issue) throwing synchronously.
            try {
                transcriptGenerationService.generateAsync(sessionId, path.toString());
            } catch (Exception ignored) {
                // transcript generation failures are tracked on the transcript
                // row itself (status FAILED) — never surfaced to the uploader
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
    // Any of title/description/thumbnail/newVideo may be null — only the
    // provided ones are changed. Replacing the video file deletes the old
    // file from disk and writes a fresh filename+url, same as a first upload.
    public FeaturedSessionVideo update(Long id, String title, String description,
                                        MultipartFile thumbnail, MultipartFile newVideo) throws IOException {
        FeaturedSessionVideo video = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Video not found: " + id));

        if (title != null) video.setTitle(title);
        if (description != null) video.setDescription(description);
        if (thumbnail != null && !thumbnail.isEmpty()) {
            video.setThumbnailUrl(toDataUri(thumbnail));
        }

        if (newVideo != null && !newVideo.isEmpty()) {
            // delete old file
            if (video.getFileName() != null) {
                Files.deleteIfExists(Paths.get(VIDEO_DIR + video.getFileName()));
            }
            String fileName = System.currentTimeMillis() + "_" + newVideo.getOriginalFilename();
            Files.copy(newVideo.getInputStream(), Paths.get(VIDEO_DIR + fileName));
            video.setFileName(fileName);
            video.setUrl("http://localhost:9000/api/video/v1/featured/session/stream/" + fileName);
            video.setDurationSeconds(null); // stale until re-derived; wire in your duration step here if used
            video.setStatus(FeaturedVideoStatus.READY);
        }

        return repo.save(video);
    }

    // ================= DELETE =================
    public void delete(Long id) {
        FeaturedSessionVideo video = repo.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Video not found: " + id));
        if (video.getFileName() != null) {
            try {
                Files.deleteIfExists(Paths.get(VIDEO_DIR + video.getFileName()));
            } catch (IOException ignored) {
                // file already gone / locked — don't block the DB delete over it
            }
        }
        repo.delete(video);
        transcriptRepo.findBySessionIdAndSourceType(id, TranscriptSourceType.FEATURED)
        .ifPresent(t -> {
            segmentRepo.deleteAll(segmentRepo.findByTranscriptIdOrderByOrderIndexAsc(t.getId()));
            transcriptRepo.delete(t);
        });
    }

    private String toDataUri(MultipartFile thumbnail) throws IOException {
        if (thumbnail == null || thumbnail.isEmpty()) return null;
        String base64 = Base64.getEncoder().encodeToString(thumbnail.getBytes());
        String contentType = thumbnail.getContentType() != null
                ? thumbnail.getContentType() : "image/jpeg";
        return "data:" + contentType + ";base64," + base64;
    }
}