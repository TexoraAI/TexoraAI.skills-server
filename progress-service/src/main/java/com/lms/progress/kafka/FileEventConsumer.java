 
package com.lms.progress.kafka;
 
import com.lms.progress.model.FileProgress;
import com.lms.progress.repository.FileProgressRepository;
import com.lms.progress.repository.StudentBatchMapRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
 
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
 
@Component
public class FileEventConsumer {
 
    private final FileProgressRepository  fileProgressRepo;
    private final StudentBatchMapRepository studentBatchRepo;
 
    public FileEventConsumer(FileProgressRepository fileProgressRepo,
                             StudentBatchMapRepository studentBatchRepo) {
        this.fileProgressRepo  = fileProgressRepo;
        this.studentBatchRepo  = studentBatchRepo;
    }
 
    // ============================
    // Listens to "file-uploaded" topic
    // Sent by FileEventProducer.sendFileUploadedEvent()
    // When a new file is uploaded — increment totalFileCount for all students in that batch
    // Same logic as VideoEventConsumer.onVideoUploaded
    // ============================
    @KafkaListener(topics = "file-uploaded", groupId = "progress-service-group")
    public void onFileUploaded(Map<String, Object> event) {
        try {
            String type = (String) event.get("type");
            if (!"FILE_UPLOADED".equals(type)) return;
 
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");
 
            String title  = (String)  payload.get("title");
            Long batchId  = Long.valueOf(payload.get("batchId").toString());
 
            // ✅ Get all students in this batch — same as VideoEventConsumer
            List<String> studentEmails = studentBatchRepo
                    .findByBatchId(batchId)
                    .stream()
                    .map(m -> m.getStudentEmail())
                    .toList();
 
            if (studentEmails.isEmpty()) {
                System.out.println("⚠️ No students found for batchId=" + batchId);
                return;
            }
 
            // ✅ For each student — increment totalFileCount by 1
            // same as VideoEventConsumer incrementing totalVideoCount
            for (String email : studentEmails) {
                FileProgress p = fileProgressRepo
                        .findByStudentEmailAndBatchId(email, batchId)
                        .orElseGet(() -> {
                            // create fresh record if student has no file progress yet
                            FileProgress fresh = new FileProgress();
                            fresh.setStudentEmail(email);
                            fresh.setBatchId(batchId);
                            fresh.setDownloadedFileIds(new ArrayList<>());
                            fresh.setTotalFileCount(0);
                            fresh.setDownloadPercentage(0);
                            fresh.setUpdatedAt(Instant.now());
                            return fresh;
                        });
 
                // increment total file count
                p.setTotalFileCount(p.getTotalFileCount() + 1);
 
                // recalculate percentage since total changed
                int downloaded = p.getDownloadedFileIds() != null
                        ? p.getDownloadedFileIds().size() : 0;
 
                double pct = p.getTotalFileCount() > 0
                        ? (double) downloaded / p.getTotalFileCount() * 100
                        : 0;
 
                p.setDownloadPercentage(Math.min(pct, 100.0));
                p.setUpdatedAt(Instant.now());
                fileProgressRepo.save(p);
            }
 
            System.out.println("📁 FILE_UPLOADED → totalFileCount +1 | title="
                    + title + " | batchId=" + batchId);
 
        } catch (Exception e) {
            System.err.println("❌ FileEventConsumer error: " + e.getMessage());
        }
    }
}