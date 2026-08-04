package com.lms.chat.controller;

import com.lms.chat.dto.MeetingSummaryResponse;
import com.lms.chat.service.MeetingSummaryService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meeting-summaries")
public class MeetingSummaryController {

    private final MeetingSummaryService service;

    public MeetingSummaryController(MeetingSummaryService service) {
        this.service = service;
    }

    // Same pattern as NotebookController.organizationId(auth)
    private Long organizationId(Authentication auth) {
        Object details = auth.getDetails();
        if (details == null) return null;
        try {
            return Long.valueOf(details.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }

    // Same pattern as MeetingController.extractRole(auth)
    private String extractRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);
    }

    @GetMapping("/{meetingId}")
    public ResponseEntity<?> getSummary(@PathVariable Long meetingId, Authentication auth) {
        try {
            String role = extractRole(auth);
            Long orgId = organizationId(auth);
            return ResponseEntity.ok(service.getSummary(meetingId, auth.getName(), role, orgId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(Map.of("error", e.getMessage()));
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<MeetingSummaryResponse>> getMySummaries(Authentication auth) {
        return ResponseEntity.ok(service.listMySummaries(auth.getName()));
    }
}