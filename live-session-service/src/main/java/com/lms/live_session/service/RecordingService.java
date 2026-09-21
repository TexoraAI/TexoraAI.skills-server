package com.lms.live_session.service;
import com.lms.live_session.dto.AiChatRequest;
import com.lms.live_session.dto.AiChatResponse;
import com.lms.live_session.dto.RecordingResponse;
import com.lms.live_session.entity.Recording;
import com.lms.live_session.entity.SessionAiNote;
import com.lms.live_session.event.SessionNotificationEvent;
import com.lms.live_session.kafka.NotificationProducer;
import com.lms.live_session.repository.RecordingRepository;
import com.lms.live_session.repository.SessionAiNoteRepository;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Lazy;
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
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

@Service
public class RecordingService {

    private final RecordingRepository repository;
    private final OpenAiClientService openAiClientService;
    private final AiTranscriptLinkService aiTranscriptLinkService;
    private final AiCompanionService aiCompanionService;
    private final SessionAiNoteRepository sessionAiNoteRepository;
    private final NotificationProducer notificationProducer;
    private final LiveSessionUsageService usageService;
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

    // Whisper's hard cap is 25MB; stay comfortably under it.
    private static final long WHISPER_MAX_BYTES = 24_000_000L;
    // Generous cap so a very long session doesn't hang the async thread forever.
    private static final long FFMPEG_TIMEOUT_SECONDS = 600;
    // Target size per chunk when splitting oversized audio (keeps real margin under WHISPER_MAX_BYTES).
    private static final long CHUNK_TARGET_BYTES = 20L * 1024 * 1024;

    public RecordingService(
            RecordingRepository repository,
            OpenAiClientService openAiClientService,
            AiTranscriptLinkService aiTranscriptLinkService,
            @Lazy AiCompanionService aiCompanionService,
            SessionAiNoteRepository sessionAiNoteRepository,
            NotificationProducer notificationProducer,
            LiveSessionUsageService usageService) {
    	
        this.repository = repository;
        this.openAiClientService = openAiClientService;
        this.aiTranscriptLinkService = aiTranscriptLinkService;
        this.aiCompanionService = aiCompanionService;
        this.sessionAiNoteRepository = sessionAiNoteRepository;
        this.notificationProducer = notificationProducer;
        this.usageService=usageService;
    }

    public RecordingResponse uploadRecording(
            MultipartFile file,
            Long sessionId,
            Long batchId,
            String trainerEmail,
            String title,
            String description,
            String batchName,
            Integer durationMinutes,
            Long organizationId) {

        // Run the plan-limit check OUTSIDE the generic try/catch below, so
        // RecordingStorageLimitExceededException / RecordingDurationLimitExceededException
        // propagate to the controller with their real type intact instead of
        // being flattened into a generic RuntimeException("Upload failed: ...").
        usageService.checkRecordingLimits(organizationId, trainerEmail, durationMinutes, file.getSize());

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
            recording.setTrainerEmail(trainerEmail);
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
            recording.setOrganizationId(organizationId);

            Recording saved = repository.save(recording);
            transcribeRecording(saved.getId());
            return RecordingResponse.from(saved);

        } catch (Exception e) {
            throw new RuntimeException("Upload failed: " + e.getMessage());
        }
    }

    public List<RecordingResponse> getAllRecordings(Long callerOrgId) {
        return repository.findAllForOrgOrderByCreatedAtDesc(callerOrgId)
            .stream().map(RecordingResponse::from).collect(Collectors.toList());
    }

    public List<RecordingResponse> getByBatch(Long batchId, Long callerOrgId) {
        return repository.findByBatchIdAndStatusForOrg(batchId, "READY", callerOrgId)
            .stream().map(RecordingResponse::from).collect(Collectors.toList());
    }

    public List<RecordingResponse> getBySession(Long sessionId, Long callerOrgId) {
        return repository.findBySessionIdForOrg(sessionId, callerOrgId)
            .stream().map(RecordingResponse::from).collect(Collectors.toList());
    }

    public List<Recording> getEntitiesBySession(Long sessionId) {
        return repository.findBySessionId(sessionId);
    }

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
        transcribeRecording(saved.getId());
        return RecordingResponse.from(saved);
    }

    public RecordingResponse createAutoRecordPlaceholder(
            Long sessionId, Long batchId, String trainerEmail,
            String sessionTitle, String s3Url, Long organizationId) {
        // Deliberate scope decision: no usageService.checkRecordingLimits() here.
        // The recording already happened via LiveKit egress by the time this runs,
        // so it can't be blocked retroactively, and file size isn't known at this
        // call site (only an S3 URL). LIVE_AUTO recordings are intentionally not
        // size/duration-gated — only UPLOADED (manual) recordings go through the check.
        Recording recording = new Recording();
        recording.setSessionId(sessionId);
        recording.setBatchId(batchId);
        recording.setTrainerEmail(trainerEmail);
        recording.setTitle(sessionTitle + " — Recording");
        recording.setRecordingType("LIVE_AUTO");
        recording.setStatus("READY");
        recording.setFilePath(s3Url);
        recording.setUploadedAt(LocalDateTime.now());
        recording.setOrganizationId(organizationId);
        Recording saved = repository.save(recording);
        transcribeRecording(saved.getId());
        return RecordingResponse.from(saved);
    }

    // ────────────────────────────────────────────────────────────
    // Transcription (Phase 2.3+) — audio-only extraction & chunking
    // ────────────────────────────────────────────────────────────

    public void transcribeRecording(Long recordingId) {
        Recording recording = repository.findById(recordingId)
            .orElseThrow(() -> new RuntimeException("Recording not found: " + recordingId));

        recording.setTranscriptStatus("PROCESSING");
        repository.save(recording);

        CompletableFuture.runAsync(() -> {
            Path workDir = null;
            try {
                workDir = Files.createTempDirectory("transcribe_" + recordingId + "_");

                // Step 1: get the source container (video+audio) onto local disk.
                Path sourceMediaPath;
                if ("LIVE_AUTO".equals(recording.getRecordingType())) {
                    byte[] mediaBytes = fetchRecordingBytesFromS3(recording.getFilePath());
                    String remoteName = extractFileNameFromS3Url(recording.getFilePath());
                    sourceMediaPath = workDir.resolve("source_" + remoteName);
                    Files.write(sourceMediaPath, mediaBytes);
                } else {
                    sourceMediaPath = Paths.get(uploadDir, recording.getFileName());
                }

                // Step 2: extract audio-only (compressed) so we stay well under Whisper's 25MB limit.
                Path audioPath = workDir.resolve("audio.mp3");
                extractAudioOnly(sourceMediaPath, audioPath);

                long audioSize = Files.size(audioPath);
                String transcript;

                if (audioSize > WHISPER_MAX_BYTES) {
                    // Step 3 (long sessions only): split into sequential chunks and transcribe each in order.
                    List<Path> chunks = splitAudioIntoChunks(audioPath, workDir, audioSize);
                    StringBuilder combined = new StringBuilder();
                    for (Path chunk : chunks) {
                        byte[] chunkBytes = Files.readAllBytes(chunk);
                        String chunkText = openAiClientService.transcribeAudio(chunkBytes, chunk.getFileName().toString());
                        if (combined.length() > 0) combined.append(' ');
                        combined.append(chunkText == null ? "" : chunkText.trim());
                    }
                    transcript = combined.toString();
                } else {
                    byte[] audioBytes = Files.readAllBytes(audioPath);
                    transcript = openAiClientService.transcribeAudio(audioBytes, audioPath.getFileName().toString());
                }

                Recording toUpdate = repository.findById(recordingId)
                        .orElseThrow(() -> new RuntimeException("Recording not found: " + recordingId));
                    toUpdate.setTranscriptText(transcript);
                    toUpdate.setTranscriptStatus("DONE");
                    Recording savedRecording = repository.save(toUpdate);

                    // Event-driven hook: as soon as this recording's transcript is DONE,
                    // mirror it into the AiTranscriptSession/segment model used by the
                    // Meetings "Ask About Transcript" panel. Never allowed to break
                    // transcription itself if linking fails.
                    try {
                        aiTranscriptLinkService.linkRecordingTranscript(savedRecording);
                    } catch (Exception linkEx) {
                        System.err.println("⚠️ Failed to link transcript to AiTranscriptSession for recording "
                            + recordingId + ": " + linkEx.getMessage());
                    }

                    // Prompt 11: transcription succeeded — auto-generate and save a
                    // RECORDING_SUMMARY note, then notify the trainer. Never allowed
                    // to break transcription itself if summary generation fails.
                    try {
                        generateAndSaveSummary(savedRecording);
                    } catch (Exception summaryEx) {
                        System.err.println("⚠️ Failed to auto-generate summary for recording "
                            + recordingId + ": " + summaryEx.getMessage());
                    }

                } catch (FfmpegExtractionException fex) {
                System.err.println("⚠️ ffmpeg audio extraction failed for recording " + recordingId + ": " + fex.getMessage());
                repository.findById(recordingId).ifPresent(r -> {
                    r.setTranscriptStatus("FAILED");
                    repository.save(r);
                });
                notifyTrainerTranscriptionFailed(recording);
            } catch (Exception e) {
                System.err.println("⚠️ Transcription failed for recording " + recordingId + ": " + e.getMessage());
                repository.findById(recordingId).ifPresent(r -> {
                    r.setTranscriptStatus("FAILED");
                    repository.save(r);
                });
                notifyTrainerTranscriptionFailed(recording);
            } finally {
                cleanupWorkDir(workDir);
            }
        });
    }

    
    
    
    // ────────────────────────────────────────────────────────────
    // Prompt 11 — auto-generate recording summary + trainer notifications
    // ────────────────────────────────────────────────────────────

    /**
     * Builds a RECORDING_SUMMARY AiChatRequest scoped to this recording's
     * session, calls AiCompanionService directly (saveToHistory=false —
     * this is a background job, not a user-initiated chat turn), saves the
     * result as a SessionAiNote (SUMMARY), and notifies the trainer that
     * it's ready. Never throws — caller treats this as best-effort so a
     * summary failure never breaks transcription.
     */
    private void generateAndSaveSummary(Recording recording) {
        if (recording.getSessionId() == null) {
            System.err.println("⚠️ Skipping auto-summary for recording " + recording.getId()
                + ": recording has no sessionId.");
            return;
        }

        AiChatRequest req = new AiChatRequest();
        req.setSessionId(recording.getSessionId());
        req.setMode("RECORDING_SUMMARY");
        req.setSources(List.of("RECORDINGS"));
        req.setSaveToHistory(false);
        req.setSkipUsageCheck(true);
        AiChatResponse response = aiCompanionService.processRequest(req, recording.getTrainerEmail(), null);

        if (!response.isSuccess()) {
            System.err.println("⚠️ Auto-summary generation failed for recording " + recording.getId()
                + ": " + response.getError());
            return;
        }

        SessionAiNote note = new SessionAiNote();
        note.setSessionId(recording.getSessionId());
        note.setContent(response.getResponse());
        note.setNoteType("SUMMARY");
        note.setGeneratedBy(recording.getTrainerEmail());
        sessionAiNoteRepository.save(note);

        notifyTrainerSummaryReady(recording);
    }

    /**
     * Notification pattern mirrors LiveSessionService.sendStudentLiveNowNotification():
     * build a SessionNotificationEvent, publish it via the existing NotificationProducer.
     * Never allowed to throw out of the async transcription pipeline.
     */
    private void notifyTrainerSummaryReady(Recording recording) {
        try {
            SessionNotificationEvent event = new SessionNotificationEvent(
                recording.getSessionId(),
                recording.getTrainerEmail(),
                recording.getBatchId(),
                recording.getTitle(),
                null,
                null,
                null,
                "RECORDING_SUMMARY_READY",
                recording.getTrainerEmail(),
                recording.getTrainerEmail(),
                "TRAINER",
                null
            );
            event.setWorkflowOutputText("Your session recording has been transcribed and summarized.");
            notificationProducer.sendWorkflowTrainerEmail(event);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to notify trainer of ready summary for recording "
                + recording.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Notifies the trainer when transcription itself fails, so they aren't
     * left wondering why no summary/notes ever appeared.
     */
    private void notifyTrainerTranscriptionFailed(Recording recording) {
        try {
            SessionNotificationEvent event = new SessionNotificationEvent(
                recording.getSessionId(),
                recording.getTrainerEmail(),
                recording.getBatchId(),
                recording.getTitle(),
                null,
                null,
                null,
                "RECORDING_TRANSCRIPTION_FAILED",
                recording.getTrainerEmail(),
                recording.getTrainerEmail(),
                "TRAINER",
                null
            );
            event.setWorkflowOutputText("We couldn't transcribe your session recording. Please try again or contact support.");
            notificationProducer.sendWorkflowTrainerEmail(event);
        } catch (Exception e) {
            System.err.println("⚠️ Failed to notify trainer of transcription failure for recording "
                + recording.getId() + ": " + e.getMessage());
        }
    }

    /**
     * Extracts a mono, 16kHz, 64kbps MP3 track from the given source media file.
    /**
     * Extracts a mono, 16kHz, 64kbps MP3 track from the given source media file.
     * NOTE: adjust the ffmpeg args below to match whatever invocation pattern/config
     * is already used elsewhere in this codebase (e.g. if LiveKit egress already
     * produces audio at a specific sample rate/bitrate convention) — flagged as an
     * assumption since that call site wasn't in front of me.
     */
    private void extractAudioOnly(Path input, Path output) {
        List<String> cmd = List.of(
            "ffmpeg", "-y",
            "-i", input.toString(),
            "-vn",                     // drop video stream entirely
            "-ac", "1",                // mono
            "-ar", "16000",            // 16kHz — plenty for speech, keeps size down
            "-codec:a", "libmp3lame",
            "-b:a", "64k",             // compressed, well under Whisper's cap for hours of audio
            output.toString()
        );
        runFfmpeg(cmd, "audio extraction");
    }

    /**
     * Splits an oversized audio file into sequential, naturally-ordered chunks
     * (chunk_000.mp3, chunk_001.mp3, ...) each targeting CHUNK_TARGET_BYTES.
     */
    private List<Path> splitAudioIntoChunks(Path audioPath, Path workDir, long audioSizeBytes) throws IOException {
        double durationSeconds = probeDurationSeconds(audioPath);
        double bytesPerSecond = audioSizeBytes / Math.max(durationSeconds, 1.0);
        long segmentSeconds = Math.max(60L, (long) (CHUNK_TARGET_BYTES / bytesPerSecond));

        Path chunkPattern = workDir.resolve("chunk_%03d.mp3");
        List<String> cmd = List.of(
            "ffmpeg", "-y",
            "-i", audioPath.toString(),
            "-f", "segment",
            "-segment_time", String.valueOf(segmentSeconds),
            "-c", "copy",
            chunkPattern.toString()
        );
        runFfmpeg(cmd, "audio chunk splitting");

        try (var stream = Files.list(workDir)) {
            List<Path> chunks = stream
                .filter(p -> p.getFileName().toString().startsWith("chunk_"))
                .sorted(Comparator.comparing(p -> p.getFileName().toString()))
                .collect(Collectors.toList());
            if (chunks.isEmpty()) {
                throw new FfmpegExtractionException("segment split produced no chunk files");
            }
            return chunks;
        }
    }

    private double probeDurationSeconds(Path audioPath) throws IOException {
        List<String> cmd = List.of(
            "ffprobe", "-v", "error",
            "-show_entries", "format=duration",
            "-of", "default=noprint_wrappers=1:nokey=1",
            audioPath.toString()
        );
        String out;
        Process process;
        try {
            process = new ProcessBuilder(cmd).start();
        } catch (IOException e) {
            throw new FfmpegExtractionException("could not start ffprobe: " + e.getMessage());
        }
        try (var in = process.getInputStream()) {
            out = new String(in.readAllBytes()).trim();
        }
        try {
            boolean finished = process.waitFor(30, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new FfmpegExtractionException("ffprobe timed out");
            }
        } catch (InterruptedException ie) {
            Thread.currentThread().interrupt();
            throw new FfmpegExtractionException("ffprobe interrupted: " + ie.getMessage());
        }
        try {
            return Double.parseDouble(out);
        } catch (NumberFormatException nfe) {
            throw new FfmpegExtractionException("could not parse ffprobe duration output: '" + out + "'");
        }
    }

    private void runFfmpeg(List<String> command, String stepDescription) {
        Process process;
        try {
            process = new ProcessBuilder(command).start();
        } catch (IOException e) {
            throw new FfmpegExtractionException(stepDescription + " could not start ffmpeg process: " + e.getMessage());
        }

        String stderr;
        try (var errStream = process.getErrorStream()) {
            stderr = new String(errStream.readAllBytes());
        } catch (IOException e) {
            stderr = "(could not read ffmpeg stderr: " + e.getMessage() + ")";
        }

        try {
            boolean finished = process.waitFor(FFMPEG_TIMEOUT_SECONDS, TimeUnit.SECONDS);
            if (!finished) {
                process.destroyForcibly();
                throw new FfmpegExtractionException(stepDescription + " timed out after " + FFMPEG_TIMEOUT_SECONDS + "s");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new FfmpegExtractionException(stepDescription + " was interrupted: " + e.getMessage());
        }

        int exitCode = process.exitValue();
        if (exitCode != 0) {
            throw new FfmpegExtractionException(stepDescription + " failed (exit " + exitCode + "): " + stderr.trim());
        }
    }

    private void cleanupWorkDir(Path workDir) {
        if (workDir == null) return;
        try (var stream = Files.walk(workDir)) {
            stream.sorted(Comparator.reverseOrder()).forEach(p -> {
                try {
                    Files.deleteIfExists(p);
                } catch (IOException ignored) {
                    // best-effort cleanup
                }
            });
        } catch (IOException e) {
            System.err.println("⚠️ Could not fully clean up temp dir " + workDir + ": " + e.getMessage());
        }
    }

    private static class FfmpegExtractionException extends RuntimeException {
        FfmpegExtractionException(String message) { super(message); }
    }

    // Renamed from fetchAudioFromS3 — this actually pulls the full source media
    // (video+audio container) from S3; audio is separated locally via ffmpeg.
    private byte[] fetchRecordingBytesFromS3(String s3Url) {
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