package com.lms.live_session.controller;

import com.lms.live_session.dto.TexoraErrorResponseDTO;
import com.lms.live_session.dto.TexoraMeetingRequestDTO;
import com.lms.live_session.dto.TexoraMeetingResponseDTO;
import com.lms.live_session.exception.MeetingException;
import com.lms.live_session.service.MeetingService;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/texorameetings")
public class TexoraMeetingController {

    private final MeetingService service;

    @Value("${texora.api-key}")
    private String configuredApiKey;

    public TexoraMeetingController(MeetingService service) {
        this.service = service;
    }

    @PostMapping
    public ResponseEntity<?> createMeeting(@RequestHeader(value = "X-API-Key", required = false) String apiKey,
                                            @RequestBody TexoraMeetingRequestDTO dto) {
        if (apiKey == null || !apiKey.equals(configuredApiKey)) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body(new TexoraErrorResponseDTO("UNAUTHORIZED", "Missing or invalid API key."));
        }

        try {
            TexoraMeetingResponseDTO response = service.createTexoraMeeting(dto);
            return ResponseEntity.ok(response);
        } catch (MeetingException e) {
            return ResponseEntity.badRequest()
                    .body(new TexoraErrorResponseDTO("INVALID_REQUEST", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.internalServerError()
                    .body(new TexoraErrorResponseDTO("INTERNAL_ERROR", "Unexpected error occurred."));
        }
    }
}