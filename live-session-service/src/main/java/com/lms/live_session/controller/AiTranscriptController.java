package com.lms.live_session.controller;
import com.lms.live_session.dto.AiTranscriptAskRequest;
import com.lms.live_session.dto.AiTranscriptSegmentRequest;
import com.lms.live_session.dto.AiTranscriptStartRequest;
import com.lms.live_session.entity.AiTranscriptSegment;
import com.lms.live_session.entity.AiTranscriptSession;
import com.lms.live_session.service.AiTranscriptService;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.security.Principal;
import java.util.Map;

/**
 * REST controller for In-Person Notes transcription.
 * Base path: /api/v1/ai-companion/transcripts
 *
 * Every endpoint below except /start takes a transcriptId that now goes
 * through AiTranscriptService.verifyOwnership() — mismatched/missing owner
 * results in a ResponseStatusException(403/404) thrown from the service,
 * which Spring resolves to the correct HTTP status automatically (no
 * generic 500, no manual @ExceptionHandler needed here).
 */
@RestController
@RequestMapping("/api/v1/ai-companion/transcripts")
public class AiTranscriptController {

    private final AiTranscriptService transcriptService;

    public AiTranscriptController(AiTranscriptService transcriptService) {
        this.transcriptService = transcriptService;
    }

    /**
     * POST /api/v1/ai-companion/transcripts/start
     * Creates a new transcript session and returns it with the generated ID.
     * No ownership check — this creates a resource owned by the caller.
     */
    @PostMapping("/start")
    public ResponseEntity<AiTranscriptSession> startTranscript(
        @RequestBody(required = false) AiTranscriptStartRequest req,
        Principal principal
    ) {
        if (req == null) req = new AiTranscriptStartRequest();
        String email = principal != null ? principal.getName() : "unknown";
        AiTranscriptSession session = transcriptService.startSession(req, email);
        return ResponseEntity.ok(session);
    }

    /**
     * POST /api/v1/ai-companion/transcripts/{transcriptId}/segment
     * Appends a speech recognition result to the transcript.
     * CHANGED — now requires Principal so ownership can be verified.
     */
//    @PostMapping("/{transcriptId}/segment")
//    public ResponseEntity<AiTranscriptSegment> addSegment(
//        @PathVariable Long transcriptId,
//        @RequestBody AiTranscriptSegmentRequest req,
//        Principal principal
//    ) {
//        String email = principal != null ? principal.getName() : "unknown";
//        AiTranscriptSegment segment = transcriptService.addSegment(transcriptId, req, email);
//        return ResponseEntity.ok(segment);
//    }
    @PostMapping("/{transcriptId}/segment")
    public ResponseEntity<AiTranscriptSegment> addSegment(
        @PathVariable Long transcriptId,
        @RequestBody AiTranscriptSegmentRequest req,
        Principal principal
    ) {
        String email = principal != null ? principal.getName() : "unknown";
        AiTranscriptSegment segment = transcriptService.addSegment(transcriptId, req, email);
        return ResponseEntity.ok(segment);
    }

    /**
     * POST /api/v1/ai-companion/transcripts/{transcriptId}/audio-chunk
     * Multipart upload of one rolling MediaRecorder chunk from In-Person
     * Notes. Transcribes it via Whisper (OpenAiClientService) and saves the
     * result as the authoritative AiTranscriptSegment for that time window.
     * Returns 200 with null body if the chunk was silence (nothing to save);
     * a transcription failure propagates as 502 — the frontend retries once
     * and, on a second failure, marks that chunk failed locally without
     * calling this endpoint a third time or blocking the rest of the session.
     */
    @PostMapping(value = "/{transcriptId}/audio-chunk", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<AiTranscriptSegment> uploadAudioChunk(
        @PathVariable Long transcriptId,
        @RequestParam("file") MultipartFile file,
        @RequestParam(value = "chunkIndex", required = false) Integer chunkIndex,
        @RequestParam(value = "startedAtSecond", required = false) Integer startedAtSecond,
        @RequestParam(value = "language", required = false) String language,
        Principal principal
    ) {
        String email = principal != null ? principal.getName() : "unknown";
        AiTranscriptSegment segment = transcriptService.addAudioChunkSegment(
            transcriptId, file, chunkIndex, startedAtSecond, language, email);
        return ResponseEntity.ok(segment);
    }

    /**
     * POST /api/v1/ai-companion/transcripts/{transcriptId}/stop
     * Marks the transcript session as COMPLETED.
     * CHANGED — now requires Principal so ownership can be verified.
     */
    @PostMapping("/{transcriptId}/stop")
    public ResponseEntity<AiTranscriptSession> stopTranscript(
        @PathVariable Long transcriptId,
        Principal principal
    ) {
        String email = principal != null ? principal.getName() : "unknown";
        AiTranscriptSession session = transcriptService.stopSession(transcriptId, email);
        return ResponseEntity.ok(session);
    }

    /**
     * GET /api/v1/ai-companion/transcripts/{transcriptId}
     * Returns session metadata + all segments.
     * CHANGED — now requires Principal so ownership can be verified.
     */
    @GetMapping("/{transcriptId}")
    public ResponseEntity<Map<String, Object>> getTranscript(
        @PathVariable Long transcriptId,
        Principal principal
    ) {
        String email = principal != null ? principal.getName() : "unknown";
        Map<String, Object> result = transcriptService.getTranscript(transcriptId, email);
        return ResponseEntity.ok(result);
    }

    /**
     * GET /api/v1/ai-companion/transcripts/by-session/{liveSessionId}
     * Returns the Whisper-derived transcript for a virtual/LiveKit meeting.
     * 404 means "not ready yet" — the Meetings "View Transcript" button
     * handles that status specifically.
     */
    @GetMapping("/by-session/{liveSessionId}")
    public ResponseEntity<Map<String, Object>> getTranscriptByLiveSession(
        @PathVariable Long liveSessionId,
        Principal principal
    ) {
        String email = principal != null ? principal.getName() : "unknown";
        Map<String, Object> result = transcriptService.getTranscriptByLiveSessionId(liveSessionId, email);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/ai-companion/transcripts/{transcriptId}/summary
     * Generates an AI summary of all transcript segments.
     */
    @PostMapping("/{transcriptId}/summary")
    public ResponseEntity<Map<String, String>> generateSummary(
        @PathVariable Long transcriptId,
        Principal principal
    ) {
        // AiTranscriptService.generateSummary() requires the caller's email
        // both to thread through to AiCompanionService.processRequest() and,
        // as of this change, to verify ownership before doing any work.
        String email = principal != null ? principal.getName() : "unknown";
        String summary = transcriptService.generateSummary(transcriptId, email);
        return ResponseEntity.ok(Map.of("summary", summary));
    }

    /**
     * POST /api/v1/ai-companion/transcripts/{transcriptId}/ask
     * Answers a user question about the transcript using AI.
     */
    @PostMapping("/{transcriptId}/ask")
    public ResponseEntity<Map<String, String>> askAboutTranscript(
        @PathVariable Long transcriptId,
        @RequestBody AiTranscriptAskRequest req,
        Principal principal
    ) {
        // Same userEmail threading + ownership verification as generateSummary() above.
        String email = principal != null ? principal.getName() : "unknown";
        String answer = transcriptService.askAboutTranscript(transcriptId, req, email);
        return ResponseEntity.ok(Map.of("answer", answer));
    }
}