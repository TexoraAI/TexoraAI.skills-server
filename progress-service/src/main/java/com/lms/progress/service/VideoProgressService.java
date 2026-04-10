
package com.lms.progress.service;
 
import com.lms.progress.dto.VideoProgressResponse;
import com.lms.progress.model.VideoProgress;
import com.lms.progress.repository.VideoProgressRepository;
import org.springframework.stereotype.Service;
 
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
 
@Service
public class VideoProgressService {
 
    private final VideoProgressRepository repo;
    private final KafkaProducerService    producer;
 
    public VideoProgressService(VideoProgressRepository repo,
                                KafkaProducerService producer) {
        this.repo     = repo;
        this.producer = producer;
    }
 
    // ============================
    // MARK VIDEO AS WATCHED
    // Called by REST endpoint when student clicks "Mark as Watched"
    // batchId used here — NOT courseId — because videos belong to batch
    // ============================
    public VideoProgressResponse markVideoWatched(String email,
                                                  Long   batchId,
                                                  Long   videoId,
                                                  int    totalVideoCount) {
 
        // Find existing OR create fresh record for this student + batch
        VideoProgress p = repo.findByStudentEmailAndBatchId(email, batchId)
                .orElseGet(() -> {
                    VideoProgress fresh = new VideoProgress();
                    fresh.setStudentEmail(email);
                    fresh.setBatchId(batchId);
                    fresh.setWatchedVideoIds(new ArrayList<>());
                    fresh.setTotalVideoCount(totalVideoCount);
                    fresh.setWatchPercentage(0);
                    fresh.setUpdatedAt(Instant.now());
                    return fresh;
                });
 
        double before = p.getWatchPercentage();
 
        // Always sync latest total in case new videos were uploaded
        p.setTotalVideoCount(totalVideoCount);
 
        List<Long> watched = p.getWatchedVideoIds();
        if (watched == null) watched = new ArrayList<>();
 
        // Add only if not already marked watched
        if (!watched.contains(videoId)) {
            watched.add(videoId);
        }
        p.setWatchedVideoIds(watched);
 
        // Calculate watch percentage
        double percentage = totalVideoCount > 0
                ? (double) watched.size() / totalVideoCount * 100
                : 0;
        percentage = Math.min(percentage, 100.0);
 
        p.setWatchPercentage(percentage);
        p.setUpdatedAt(Instant.now());
 
        VideoProgress saved = repo.save(p);
 
        System.out.println("🎥 markVideoWatched → " + email
                + " | batch=" + batchId
                + " | video=" + videoId
                + " | " + watched.size() + "/" + totalVideoCount
                + " = " + String.format("%.1f", percentage) + "%");
 
        // Fire Kafka event only when ALL videos in batch are watched
        fireCompletionEventIfNeeded(saved, before);
        return toResponse(saved);
    }
 
    // ============================
    // GET VIDEO PROGRESS
    // Called on page load to show existing watch state
    // Returns empty progress (0%) if student hasn't watched anything yet
    // ============================
    public VideoProgressResponse getByStudentAndBatch(String email, Long batchId) {
        VideoProgress p = repo.findByStudentEmailAndBatchId(email, batchId)
                .orElseGet(() -> {
                    // ✅ No crash — return empty progress if not started yet
                    VideoProgress empty = new VideoProgress();
                    empty.setStudentEmail(email);
                    empty.setBatchId(batchId);
                    empty.setWatchedVideoIds(new ArrayList<>());
                    empty.setTotalVideoCount(0);
                    empty.setWatchPercentage(0.0);
                    empty.setUpdatedAt(Instant.now());
                    return empty;
                });
        return toResponse(p);
    }
 
    // ============================
    // PRIVATE — fire Kafka only when ALL videos watched (100%)
    // Same pattern as ProgressService.fireCompletionEventIfNeeded
    // ============================
    private void fireCompletionEventIfNeeded(VideoProgress p, double before) {
        if (before < 100 && p.getWatchPercentage() >= 100) {
            producer.sendProgressEvent(
                "User " + p.getStudentEmail() +
                " watched all videos in batch " + p.getBatchId()
            );
        }
    }
 
    // ============================
    // MAPPER — same style as ProgressService.toResponse
    // ============================
    private VideoProgressResponse toResponse(VideoProgress p) {
        VideoProgressResponse r = new VideoProgressResponse();
        r.setProgressId(p.getId());
        r.setStudentEmail(p.getStudentEmail());
        r.setBatchId(p.getBatchId());
        r.setWatchedVideoIds(p.getWatchedVideoIds());
        r.setTotalVideoCount(p.getTotalVideoCount());
        r.setWatchPercentage(p.getWatchPercentage());
        r.setUpdatedAt(p.getUpdatedAt());
        return r;
    }
}
 