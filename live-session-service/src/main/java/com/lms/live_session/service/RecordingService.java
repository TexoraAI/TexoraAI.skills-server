package com.lms.live_session.service;

import com.lms.live_session.dto.RecordingResponse;
import com.lms.live_session.entity.Recording;
import com.lms.live_session.repository.RecordingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Service
public class RecordingService {

    private final RecordingRepository repository;

    @Value("${file.upload-dir:./recordings}")
    private String uploadDir;

    public RecordingService(RecordingRepository repository) {
        this.repository = repository;
    }

    public RecordingResponse uploadRecording(
            MultipartFile file,
            Long sessionId,
            Long batchId,
            String trainerEmail,          // ✅ changed
            String title,
            String description,
            String batchName,
            Integer durationMinutes) {

        try {
            File dir = new File(uploadDir);
            if (!dir.exists()) dir.mkdirs();

            String originalName = file.getOriginalFilename();
            String extension    = originalName != null && originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf("."))
                : ".mp4";
            String uniqueName   = "rec_" + System.currentTimeMillis() + extension;

            Path savePath = Paths.get(uploadDir, uniqueName);
            Files.write(savePath, file.getBytes());

            Recording recording = new Recording();
            recording.setSessionId(sessionId);
            recording.setBatchId(batchId);
            recording.setTrainerEmail(trainerEmail);   // ✅ changed
            recording.setTitle(title);
            recording.setDescription(description);
            recording.setBatchName(batchName);
            recording.setFileName(uniqueName);
            recording.setFilePath("/recordings/" + uniqueName);
            recording.setFileType(file.getContentType());
            recording.setFileSizeBytes(file.getSize());
            recording.setRecordingType("UPLOADED");
            recording.setStatus("READY");
            recording.setDurationMinutes(durationMinutes);
            recording.setUploadedAt(LocalDateTime.now());

            Recording saved = repository.save(recording);
            return RecordingResponse.from(saved);

        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }

    public List<RecordingResponse> getAllRecordings() {
        return repository.findAllByOrderByCreatedAtDesc()
            .stream().map(RecordingResponse::from).collect(Collectors.toList());
    }

    public List<RecordingResponse> getByBatch(Long batchId) {
        return repository.findByBatchIdAndStatusOrderByCreatedAtDesc(batchId, "READY")
            .stream().map(RecordingResponse::from).collect(Collectors.toList());
    }

    public List<RecordingResponse> getBySession(Long sessionId) {
        return repository.findBySessionId(sessionId)
            .stream().map(RecordingResponse::from).collect(Collectors.toList());
    }

    // ✅ Changed: trainerEmail instead of trainerId
    public List<RecordingResponse> getByTrainerEmail(String trainerEmail) {
        return repository.findByTrainerEmailOrderByCreatedAtDesc(trainerEmail)
            .stream().map(RecordingResponse::from).collect(Collectors.toList());
    }

    public RecordingResponse getById(Long id) {
        return RecordingResponse.from(
            repository.findById(id)
                .orElseThrow(() -> new RuntimeException("Recording not found: " + id)));
    }

    public void incrementViews(Long id) {
        if (!repository.existsById(id))
            throw new RuntimeException("Recording not found: " + id);
        repository.incrementViewCount(id);
    }

    public RecordingResponse updateRecording(Long id, String title, String description) {
        Recording recording = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Recording not found: " + id));
        if (title != null && !title.isBlank()) recording.setTitle(title);
        if (description != null) recording.setDescription(description);
        return RecordingResponse.from(repository.save(recording));
    }

    public void deleteRecording(Long id) {
        Recording recording = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Recording not found: " + id));
        try {
            Path filePath = Paths.get(uploadDir, recording.getFileName());
            Files.deleteIfExists(filePath);
        } catch (Exception e) {
            System.err.println("⚠️ Could not delete file: " + e.getMessage());
        }
        repository.delete(recording);
    }

    public RecordingResponse markAsFailed(Long id) {
        Recording recording = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Recording not found: " + id));
        recording.setStatus("FAILED");
        return RecordingResponse.from(repository.save(recording));
    }

    public RecordingResponse markAsReady(Long id) {
        Recording recording = repository.findById(id)
            .orElseThrow(() -> new RuntimeException("Recording not found: " + id));
        recording.setStatus("READY");
        return RecordingResponse.from(repository.save(recording));
    }

//    public RecordingResponse createAutoRecordPlaceholder(
//            Long sessionId, Long batchId, String trainerEmail, String sessionTitle) {  // ✅ changed
//        Recording recording = new Recording();
//        recording.setSessionId(sessionId);
//        recording.setBatchId(batchId);
//        recording.setTrainerEmail(trainerEmail);   // ✅ changed
//        recording.setTitle(sessionTitle + " — Recording");
//        recording.setRecordingType("LIVE_AUTO");
//        recording.setStatus("PROCESSING");
//        recording.setUploadedAt(LocalDateTime.now());
//        return RecordingResponse.from(repository.save(recording));
//    }
    public RecordingResponse createAutoRecordPlaceholder(
            Long sessionId, Long batchId, String trainerEmail, 
            String sessionTitle, String s3Url) {
        Recording recording = new Recording();
        recording.setSessionId(sessionId);
        recording.setBatchId(batchId);
        recording.setTrainerEmail(trainerEmail);
        recording.setTitle(sessionTitle + " — Recording");
        recording.setRecordingType("LIVE_AUTO");
        recording.setStatus("READY");
        recording.setFilePath(s3Url);        // ✅ S3 URL stored here
        recording.setUploadedAt(LocalDateTime.now());
        return RecordingResponse.from(repository.save(recording));
    }
}