package com.lms.live_session.service;

import com.lms.live_session.dto.RecordingResponse;
import com.lms.live_session.entity.Recording;
import com.lms.live_session.repository.RecordingRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.ResponseBytes;
import software.amazon.awssdk.core.sync.ResponseTransformer;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.stream.Collectors;

@Service
public class RecordingService {

    private final RecordingRepository repository;
    private final OpenAiClientService openAiClientService;

    @Value("${file.upload-dir:./recordings}")
    private String uploadDir;

    @Value("${aws.s3.bucket}")
    private String bucket;
    @Value("${aws.access-key}")
    private String awsAccessKey;
    @Value("${aws.secret-key}")
    private String awsSecretKey;
    @Value("${aws.region}")
    private String awsRegion;

    public RecordingService(RecordingRepository repository, OpenAiClientService openAiClientService) {
        this.repository = repository;
        this.openAiClientService = openAiClientService;
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
            transcribeRecording(saved.getId());   // ✅ NEW — auto-transcribe manual uploads too
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

    /**
     * Entity-level accessor (not DTO) for internal services like
     * AiContextBuilderService that need transcriptText/transcriptStatus.
     */
    public List<Recording> getEntitiesBySession(Long sessionId) {
        return repository.findBySessionId(sessionId);
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
        Recording saved = repository.save(recording);
        transcribeRecording(saved.getId());   // ✅ NEW
        return RecordingResponse.from(saved);
    }

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
        Recording saved = repository.save(recording);
        transcribeRecording(saved.getId());   // ✅ NEW
        return RecordingResponse.from(saved);
    }

    // ────────────────────────────────────────────────────────────
    // Transcription (Phase 2.3)
    // ────────────────────────────────────────────────────────────

    /**
     * Kicks off transcription for a recording asynchronously (does not
     * block the calling request thread). Safe to call multiple times —
     * it will simply re-transcribe and overwrite the previous result.
     */
    public void transcribeRecording(Long recordingId) {
        Recording recording = repository.findById(recordingId)
            .orElseThrow(() -> new RuntimeException("Recording not found: " + recordingId));

        recording.setTranscriptStatus("PROCESSING");
        repository.save(recording);

        CompletableFuture.runAsync(() -> {
            try {
                byte[] audioBytes;
                String filename;

                if ("LIVE_AUTO".equals(recording.getRecordingType())) {
                    audioBytes = fetchAudioFromS3(recording.getFilePath());
                    filename = extractFileNameFromS3Url(recording.getFilePath());
                } else {
                    Path filePath = Paths.get(uploadDir, recording.getFileName());
                    audioBytes = Files.readAllBytes(filePath);
                    filename = recording.getFileName();
                }

                String transcript = openAiClientService.transcribeAudio(audioBytes, filename);

                Recording toUpdate = repository.findById(recordingId)
                    .orElseThrow(() -> new RuntimeException("Recording not found: " + recordingId));
                toUpdate.setTranscriptText(transcript);
                toUpdate.setTranscriptStatus("DONE");
                repository.save(toUpdate);

            } catch (Exception e) {
                System.err.println("⚠️ Transcription failed for recording " + recordingId + ": " + e.getMessage());
                repository.findById(recordingId).ifPresent(r -> {
                    r.setTranscriptStatus("FAILED");
                    repository.save(r);
                });
            }
        });
    }

    private byte[] fetchAudioFromS3(String s3Url) {
        String key = extractS3KeyFromUrl(s3Url);

        S3Client s3 = S3Client.builder()
            .region(Region.of(awsRegion))
            .credentialsProvider(StaticCredentialsProvider.create(
                AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
            .build();

        try {
            GetObjectRequest request = GetObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .build();
            ResponseBytes<GetObjectResponse> objectBytes = s3.getObject(request, ResponseTransformer.toBytes());
            return objectBytes.asByteArray();
        } finally {
            s3.close();
        }
    }

    private String extractS3KeyFromUrl(String s3Url) {
        String marker = ".amazonaws.com/";
        int idx = s3Url.indexOf(marker);
        if (idx == -1) {
            throw new RuntimeException("Unrecognized S3 URL format: " + s3Url);
        }
        return s3Url.substring(idx + marker.length());
    }

    private String extractFileNameFromS3Url(String s3Url) {
        String key = extractS3KeyFromUrl(s3Url);
        int slash = key.lastIndexOf('/');
        return slash == -1 ? key : key.substring(slash + 1);
    }
}