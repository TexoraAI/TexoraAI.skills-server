
package com.lms.progress.service;
 
import com.lms.progress.dto.FileProgressResponse;
import com.lms.progress.model.FileProgress;
import com.lms.progress.repository.FileProgressRepository;
import org.springframework.stereotype.Service;
 
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
 
@Service
public class FileProgressService {
 
    private final FileProgressRepository repo;
    private final KafkaProducerService   producer;
 
    public FileProgressService(FileProgressRepository repo,
                               KafkaProducerService producer) {
        this.repo     = repo;
        this.producer = producer;
    }
 
    // ============================
    // MARK FILE AS DOWNLOADED/PREVIEWED
    // Called by REST endpoint when student clicks Preview button
    // batchId used here — NOT courseId — because files belong to batch
    // ============================
    public FileProgressResponse markFileDownloaded(String email,
                                                   Long   batchId,
                                                   Long   fileId,
                                                   int    totalFileCount) {
 
        // Find existing OR create fresh record for this student + batch
        FileProgress p = repo.findByStudentEmailAndBatchId(email, batchId)
                .orElseGet(() -> {
                    FileProgress fresh = new FileProgress();
                    fresh.setStudentEmail(email);
                    fresh.setBatchId(batchId);
                    fresh.setDownloadedFileIds(new ArrayList<>());
                    fresh.setTotalFileCount(totalFileCount);
                    fresh.setDownloadPercentage(0);
                    fresh.setUpdatedAt(Instant.now());
                    return fresh;
                });
 
        double before = p.getDownloadPercentage();
 
        // Always sync latest total in case new files were uploaded
        p.setTotalFileCount(totalFileCount);
 
        List<Long> downloaded = p.getDownloadedFileIds();
        if (downloaded == null) downloaded = new ArrayList<>();
 
        // Add only if not already marked downloaded
        if (!downloaded.contains(fileId)) {
            downloaded.add(fileId);
        }
        p.setDownloadedFileIds(downloaded);
 
        // Calculate download percentage
        double percentage = totalFileCount > 0
                ? (double) downloaded.size() / totalFileCount * 100
                : 0;
        percentage = Math.min(percentage, 100.0);
 
        p.setDownloadPercentage(percentage);
        p.setUpdatedAt(Instant.now());
 
        FileProgress saved = repo.save(p);
 
        System.out.println("📁 markFileDownloaded → " + email
                + " | batch=" + batchId
                + " | file=" + fileId
                + " | " + downloaded.size() + "/" + totalFileCount
                + " = " + String.format("%.1f", percentage) + "%");
 
        // Fire Kafka event only when ALL files in batch are downloaded
        fireCompletionEventIfNeeded(saved, before);
        return toResponse(saved);
    }
 
    // ============================
    // GET FILE PROGRESS
    // Called on page load to show existing download state
    // Returns empty progress (0%) if student hasn't downloaded anything yet — no crash
    // ============================
    public FileProgressResponse getByStudentAndBatch(String email, Long batchId) {
        FileProgress p = repo.findByStudentEmailAndBatchId(email, batchId)
                .orElseGet(() -> {
                    // ✅ No crash — return empty progress if not started yet
                    FileProgress empty = new FileProgress();
                    empty.setStudentEmail(email);
                    empty.setBatchId(batchId);
                    empty.setDownloadedFileIds(new ArrayList<>());
                    empty.setTotalFileCount(0);
                    empty.setDownloadPercentage(0.0);
                    empty.setUpdatedAt(Instant.now());
                    return empty;
                });
        return toResponse(p);
    }
 
    // ============================
    // PRIVATE — fire Kafka only when ALL files downloaded (100%)
    // Same pattern as VideoProgressService and ProgressService
    // ============================
    private void fireCompletionEventIfNeeded(FileProgress p, double before) {
        if (before < 100 && p.getDownloadPercentage() >= 100) {
            producer.sendProgressEvent(
                "User " + p.getStudentEmail() +
                " downloaded all files in batch " + p.getBatchId()
            );
        }
    }
 
    // ============================
    // MAPPER — same style as VideoProgressService.toResponse
    // ============================
    private FileProgressResponse toResponse(FileProgress p) {
        FileProgressResponse r = new FileProgressResponse();
        r.setProgressId(p.getId());
        r.setStudentEmail(p.getStudentEmail());
        r.setBatchId(p.getBatchId());
        r.setDownloadedFileIds(p.getDownloadedFileIds());
        r.setTotalFileCount(p.getTotalFileCount());
        r.setDownloadPercentage(p.getDownloadPercentage());
        r.setUpdatedAt(p.getUpdatedAt());
        return r;
    }
}