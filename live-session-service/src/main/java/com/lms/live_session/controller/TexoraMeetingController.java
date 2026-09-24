package com.lms.live_session.controller;

import com.lms.live_session.exception.MeetingException;
import com.lms.live_session.dto.*;
import com.lms.live_session.service.TexoraMeetingService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;
import java.time.Instant;
import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.nio.charset.StandardCharsets;
import java.util.HexFormat;
@RestController
@RequestMapping("/api/v1/texorameetings")
public class TexoraMeetingController {

    private final TexoraMeetingService service;

    @Value("${texora.api-key}")
    private String configuredApiKey;
    
    @Value("${texora.transcript-signing-secret}") 
    private String transcriptSigningSecret;

    public TexoraMeetingController(TexoraMeetingService service) {
        this.service = service;
    }

    private boolean isUnauthorized(String apiKey) {
        return apiKey == null || !apiKey.equals(configuredApiKey);
    }

    @PostMapping
    public ResponseEntity<?> createMeeting(@RequestHeader(value = "X-API-Key", required = false) String apiKey,
                                            @RequestBody TexoraMeetingRequestDTO dto) {
        if (isUnauthorized(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TexoraErrorResponseDTO("UNAUTHORIZED", "Missing or invalid API key."));
        }
        try {
            return ResponseEntity.ok(service.createMeeting(dto));
        } catch (MeetingException e) {
            return ResponseEntity.badRequest().body(new TexoraErrorResponseDTO("INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError().body(new TexoraErrorResponseDTO("INTERNAL_ERROR", "Unexpected error occurred."));
        }
    }

    @DeleteMapping("/{meetingId}")
    public ResponseEntity<?> cancelMeeting(@RequestHeader(value = "X-API-Key", required = false) String apiKey,
                                            @PathVariable String meetingId) {
        if (isUnauthorized(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TexoraErrorResponseDTO("UNAUTHORIZED", "Missing or invalid API key."));
        }
        try {
            service.cancelMeeting(meetingId);
            return ResponseEntity.ok(Map.of("meetingId", meetingId, "status", "CANCELLED"));
        } catch (MeetingException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new TexoraErrorResponseDTO("MEETING_NOT_FOUND", e.getMessage()));
        }
    }

    @GetMapping("/{meetingId}/analysis")
    public ResponseEntity<?> getAnalysis(@RequestHeader(value = "X-API-Key", required = false) String apiKey,
                                          @PathVariable String meetingId) {
        if (isUnauthorized(apiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TexoraErrorResponseDTO("UNAUTHORIZED", "Missing or invalid API key."));
        }
        try {
            return ResponseEntity.ok(service.getAnalysis(meetingId));
        } catch (MeetingException e) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND).body(new TexoraErrorResponseDTO("MEETING_NOT_FOUND", e.getMessage()));
        }
    }

    @GetMapping("/join/{joinCode}")
    public ResponseEntity<?> validateJoinCode(@PathVariable String joinCode) {
        return ResponseEntity.ok(service.validateJoinCode(joinCode));
    }

    @PostMapping("/join/{joinCode}/token")
    public ResponseEntity<?> generateJoinToken(@PathVariable String joinCode,
                                                @RequestBody(required = false) TexoraJoinRequestDTO body) {
        try {
            String identity = body != null ? body.getIdentity() : null;
            String displayName = body != null ? body.getDisplayName() : null;
            String role = body != null ? body.getRole() : null;
            return ResponseEntity.ok(service.generateJoinToken(joinCode, identity, displayName, role));
        } catch (MeetingException e) {
            return ResponseEntity.badRequest().body(new TexoraErrorResponseDTO("NOT_JOINABLE", e.getMessage()));
        }
    }

    @PostMapping("/join/{joinCode}/leave")
    public ResponseEntity<?> leave(@PathVariable String joinCode,
                                    @RequestBody(required = false) Map<String, String> body) {
        String identity = body != null ? body.get("identity") : null;
        service.recordLeave(joinCode, identity);
        return ResponseEntity.ok().build();
    }
    
    @GetMapping("/transcripts/{meetingId}")
    public ResponseEntity<?> downloadTranscript(@PathVariable String meetingId,
                                                 @RequestParam("exp") long exp,
                                                 @RequestParam("sig") String sig) {
        if (Instant.now().getEpochSecond() > exp) {
            return ResponseEntity.status(HttpStatus.GONE)
                    .body(new TexoraErrorResponseDTO("EXPIRED", "This transcript link has expired."));
        }
        try {
            String signedString = meetingId + "." + exp;
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(transcriptSigningSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            byte[] expectedSig = mac.doFinal(signedString.getBytes(StandardCharsets.UTF_8));
            String expectedHex = HexFormat.of().formatHex(expectedSig);
            // Constant-time comparison, per spec 6.2's own guidance for Texora's side.
            if (!java.security.MessageDigest.isEqual(expectedHex.getBytes(StandardCharsets.UTF_8), sig.getBytes(StandardCharsets.UTF_8))) {
                return ResponseEntity.status(HttpStatus.FORBIDDEN)
                        .body(new TexoraErrorResponseDTO("INVALID_SIGNATURE", "Signature verification failed."));
            }
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).build();
        }

        String transcriptText = service.getTranscriptText(meetingId);
        if (transcriptText == null) {
            return ResponseEntity.status(HttpStatus.NOT_FOUND)
                    .body(new TexoraErrorResponseDTO("TRANSCRIPT_NOT_FOUND", "No transcript available for this meeting."));
        }
        return ResponseEntity.ok()
                .contentType(org.springframework.http.MediaType.TEXT_PLAIN)
                .body(transcriptText);
    }
}