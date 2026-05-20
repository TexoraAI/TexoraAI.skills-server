package com.lms.live_session.controller;

import com.lms.live_session.dto.AiTranscriptAskRequest;
import com.lms.live_session.dto.AiTranscriptSegmentRequest;
import com.lms.live_session.dto.AiTranscriptStartRequest;
import com.lms.live_session.entity.AiTranscriptSegment;
import com.lms.live_session.entity.AiTranscriptSession;
import com.lms.live_session.service.AiTranscriptService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.Map;

/**
 * REST controller for In-Person Notes transcription.
 * Base path: /api/v1/ai-companion/transcripts
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
     */
    @PostMapping("/{transcriptId}/segment")
    public ResponseEntity<AiTranscriptSegment> addSegment(
        @PathVariable Long transcriptId,
        @RequestBody AiTranscriptSegmentRequest req
    ) {
        AiTranscriptSegment segment = transcriptService.addSegment(transcriptId, req);
        return ResponseEntity.ok(segment);
    }

    /**
     * POST /api/v1/ai-companion/transcripts/{transcriptId}/stop
     * Marks the transcript session as COMPLETED.
     */
    @PostMapping("/{transcriptId}/stop")
    public ResponseEntity<AiTranscriptSession> stopTranscript(
        @PathVariable Long transcriptId
    ) {
        AiTranscriptSession session = transcriptService.stopSession(transcriptId);
        return ResponseEntity.ok(session);
    }

    /**
     * GET /api/v1/ai-companion/transcripts/{transcriptId}
     * Returns session metadata + all segments.
     */
    @GetMapping("/{transcriptId}")
    public ResponseEntity<Map<String, Object>> getTranscript(
        @PathVariable Long transcriptId
    ) {
        Map<String, Object> result = transcriptService.getTranscript(transcriptId);
        return ResponseEntity.ok(result);
    }

    /**
     * POST /api/v1/ai-companion/transcripts/{transcriptId}/summary
     * Generates an AI summary of all transcript segments.
     */
    @PostMapping("/{transcriptId}/summary")
    public ResponseEntity<Map<String, String>> generateSummary(
        @PathVariable Long transcriptId
    ) {
        String summary = transcriptService.generateSummary(transcriptId);
        return ResponseEntity.ok(Map.of("summary", summary));
    }

    /**
     * POST /api/v1/ai-companion/transcripts/{transcriptId}/ask
     * Answers a user question about the transcript using AI.
     */
    @PostMapping("/{transcriptId}/ask")
    public ResponseEntity<Map<String, String>> askAboutTranscript(
        @PathVariable Long transcriptId,
        @RequestBody AiTranscriptAskRequest req
    ) {
        String answer = transcriptService.askAboutTranscript(transcriptId, req);
        return ResponseEntity.ok(Map.of("answer", answer));
    }
}