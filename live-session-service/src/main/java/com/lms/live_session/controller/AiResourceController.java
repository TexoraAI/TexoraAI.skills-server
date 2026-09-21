package com.lms.live_session.controller;

import com.lms.live_session.dto.RecordingResponse;
import com.lms.live_session.entity.AiUploadedResource;
import com.lms.live_session.entity.WhiteboardSnapshot;
import com.lms.live_session.repository.AiUploadedResourceRepository;
import com.lms.live_session.repository.WhiteboardSnapshotRepository;
import com.lms.live_session.service.AiTextExtractionService;
import com.lms.live_session.service.RecordingService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials;
import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.regions.Region;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import com.lms.live_session.entity.ChatMessage;
import com.lms.live_session.repository.ChatMessageRepository;
import com.lms.live_session.entity.LiveSession;
import com.lms.live_session.service.LiveSessionService;

import java.security.Principal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;


@RestController
@RequestMapping("/api/v1/ai-companion/resources")
public class AiResourceController {
    private final AiUploadedResourceRepository uploadedResourceRepository;
    private final AiTextExtractionService textExtractionService;
    private final ChatMessageRepository chatMessageRepository;
    private final LiveSessionService liveSessionService;
    private final WhiteboardSnapshotRepository whiteboardSnapshotRepository;
    private final RecordingService recordingService;

    @Value("${aws.s3.bucket}")
    private String bucket;
    @Value("${aws.access-key}")
    private String awsAccessKey;
    @Value("${aws.secret-key}")
    private String awsSecretKey;
    @Value("${aws.region}")
    private String awsRegion;

    public AiResourceController(AiUploadedResourceRepository uploadedResourceRepository,
                                 AiTextExtractionService textExtractionService,
                                 ChatMessageRepository chatMessageRepository,
                                 LiveSessionService liveSessionService,
                                 WhiteboardSnapshotRepository whiteboardSnapshotRepository,
                                 RecordingService recordingService) {
        this.uploadedResourceRepository = uploadedResourceRepository;
        this.textExtractionService = textExtractionService;
        this.chatMessageRepository = chatMessageRepository;
        this.liveSessionService = liveSessionService;
        this.whiteboardSnapshotRepository = whiteboardSnapshotRepository;
        this.recordingService = recordingService;
    }

    // GET /api/v1/ai-companion/resources/meetings
    @GetMapping("/meetings")
    public ResponseEntity<?> getMeetings(Principal principal) {
        List<LiveSession> sessions = liveSessionService.getMySessionsAsTrainer(principal.getName());
        return ResponseEntity.ok(sessions);
    }

    // GET /api/v1/ai-companion/resources/docs
    @GetMapping("/docs")
    public ResponseEntity<?> getDocs(Principal principal) {
        List<AiUploadedResource> docs =
                uploadedResourceRepository.findByUploadedByOrderByCreatedAtDesc(principal.getName());
        return ResponseEntity.ok(docs);
    }

    // GET /api/v1/ai-companion/resources/chat
    @GetMapping("/chat")
    public ResponseEntity<?> getChatHistory(@RequestParam(required = false) Long sessionId, Principal principal) {
        if (sessionId == null) {
            return ResponseEntity.ok(List.of());
        }
        return ResponseEntity.ok(chatMessageRepository.findBySessionIdOrderByTimestampAsc(sessionId));
    }

    // GET /api/v1/ai-companion/resources/whiteboard
    @GetMapping("/whiteboard")
    public ResponseEntity<?> getWhiteboard(@RequestParam(required = false) Long sessionId, Principal principal) {
        if (sessionId == null) {
            return ResponseEntity.ok(List.of());
        }

        if (!callerOwnsSession(sessionId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(java.util.Map.of("error", "You do not have access to this session's whiteboard"));
        }

        // Same repository AiContextBuilderService.buildWhiteboardContext() already uses.
        Optional<WhiteboardSnapshot> snapshot = whiteboardSnapshotRepository.findBySessionId(sessionId);

        return ResponseEntity.ok(snapshot.map(List::of).orElseGet(List::of));
    }

    // GET /api/v1/ai-companion/resources/recordings
    @GetMapping("/recordings")
    public ResponseEntity<?> getRecordings(@RequestParam(required = false) Long sessionId, Principal principal) {
        if (sessionId == null) {
            return ResponseEntity.ok(List.of());
        }

        if (!callerOwnsSession(sessionId, principal)) {
            return ResponseEntity.status(HttpStatus.FORBIDDEN)
                    .body(java.util.Map.of("error", "You do not have access to this session's recordings"));
        }

        // Entity-level lookup by session (same call AiContextBuilderService.buildRecordingsContext()
        // uses), mapped to the response DTO for the client.
        List<RecordingResponse> recordings = recordingService.getEntitiesBySession(sessionId)
                .stream()
                .map(RecordingResponse::from)
                .collect(Collectors.toList());

        return ResponseEntity.ok(recordings);
    }

    /**
     * Confirms the given session belongs to the calling trainer.
     * Reuses the same source of truth as GET /meetings (getMySessionsAsTrainer)
     * rather than introducing a new "isTrainerForSession"-style method, since
     * that method doesn't currently exist on LiveSessionService.
     */
    private boolean callerOwnsSession(Long sessionId, Principal principal) {
        return liveSessionService.getMySessionsAsTrainer(principal.getName())
                .stream()
                .anyMatch(s -> sessionId.equals(s.getId()));
    }

    // POST /api/v1/ai-companion/resources/upload
    @PostMapping("/upload")
    public ResponseEntity<?> upload(@RequestParam("file") MultipartFile file,
                                     @RequestParam(required = false) Long sessionId,
                                     Principal principal) {
        if (file.isEmpty()) {
            return ResponseEntity.badRequest().body(java.util.Map.of("error", "File is empty"));
        }

        String originalName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "upload";
        String key = "ai-uploads/" + UUID.randomUUID() + "-" + originalName;

        try {
            uploadToS3(file, key);
        } catch (Exception e) {
            System.err.println("❌ S3 upload failed for " + originalName + ": " + e.getMessage());
            return ResponseEntity.internalServerError()
                    .body(java.util.Map.of("error", "Upload failed: " + e.getMessage()));
        }

        String s3Url = String.format("https://%s.s3.%s.amazonaws.com/%s", bucket, awsRegion, key);
        String extractedText = textExtractionService.extractText(file, originalName);

        AiUploadedResource resource = new AiUploadedResource();
        resource.setUploadedBy(principal.getName());
        resource.setSessionId(sessionId);
        resource.setFileName(originalName);
        resource.setFileType(file.getContentType());
        resource.setFileSize(file.getSize());
        resource.setS3Url(s3Url);
        resource.setExtractedText(extractedText);

        AiUploadedResource saved = uploadedResourceRepository.save(resource);
        return ResponseEntity.ok(saved);
    }

    private void uploadToS3(MultipartFile file, String key) throws Exception {
        S3Client s3 = S3Client.builder()
                .region(Region.of(awsRegion))
                .credentialsProvider(StaticCredentialsProvider.create(
                        AwsBasicCredentials.create(awsAccessKey, awsSecretKey)))
                .build();
        try {
            PutObjectRequest request = PutObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .contentType(file.getContentType())
                    .build();
            s3.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
        } finally {
            s3.close();
        }
    }
}