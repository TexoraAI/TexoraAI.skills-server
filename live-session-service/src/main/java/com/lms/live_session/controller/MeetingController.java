package com.lms.live_session.controller;

import com.lms.live_session.dto.MeetingJoinRequestDTO;
import com.lms.live_session.dto.MeetingRequestDTO;
import com.lms.live_session.dto.MeetingResponseDTO;
import com.lms.live_session.dto.MeetingSummaryRequestDTO;
import com.lms.live_session.service.MeetingService;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/meetings")
public class MeetingController {

    private final MeetingService service;

    public MeetingController(MeetingService service) {
        this.service = service;
    }

    // ═══════════════════════════════════════════════════════
    // CREATE
    // ═══════════════════════════════════════════════════════

    @PostMapping("/instant")
    public ResponseEntity<?> createInstantMeeting(@RequestBody(required = false) MeetingRequestDTO dto,
                                                   Authentication auth) {
        try {
            MeetingRequestDTO body = dto != null ? dto : new MeetingRequestDTO();
            String creatorId = auth.getName();
            String creatorRole = extractRole(auth);

            MeetingResponseDTO created = service.createInstantMeeting(body, creatorId, creatorRole);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to start instant meeting: " + e.getMessage()));
        }
    }

    @PostMapping("/scheduled")
    public ResponseEntity<?> createScheduledMeeting(@RequestBody MeetingRequestDTO dto, Authentication auth) {
        try {
            String creatorId = auth.getName();
            String creatorRole = extractRole(auth);

            MeetingResponseDTO created = service.createScheduledMeeting(dto, creatorId, creatorRole);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to schedule meeting: " + e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════
    // JOIN CODE LOOKUP (public — anonymous guests land here)
    // ═══════════════════════════════════════════════════════

    @GetMapping("/validate/{joinCode}")
    public ResponseEntity<Map<String, Object>> validateJoinCode(@PathVariable String joinCode) {
        return ResponseEntity.ok(service.validateJoinCode(joinCode));
    }

    @GetMapping("/join/{joinCode}")
    public ResponseEntity<?> getMeetingByJoinCode(@PathVariable String joinCode, Authentication auth) {
        try {
            String requesterId = auth != null ? auth.getName() : null;
            return ResponseEntity.ok(service.getMeetingByJoinCode(joinCode, requesterId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════
    // QUERIES
    // ═══════════════════════════════════════════════════════

    @GetMapping("/{id}")
    public ResponseEntity<?> getMeetingById(@PathVariable Long id, Authentication auth) {
        try {
            String requesterId = auth != null ? auth.getName() : null;
            return ResponseEntity.ok(service.getMeetingById(id, requesterId));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @GetMapping("/my")
    public ResponseEntity<List<MeetingResponseDTO>> getMyMeetings(Authentication auth) {
        return ResponseEntity.ok(service.getMyMeetings(auth.getName()));
    }

    // ═══════════════════════════════════════════════════════
    // START / END
    // ═══════════════════════════════════════════════════════

    @PostMapping("/{id}/start")
    public ResponseEntity<?> startScheduledMeeting(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.startScheduledMeeting(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/end")
    public ResponseEntity<?> endMeeting(@PathVariable Long id) {
        try {
            return ResponseEntity.ok(service.endMeeting(id));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════
    // LIVEKIT TOKEN — HOST ONLY
    // ═══════════════════════════════════════════════════════

    @GetMapping("/{id}/token")
    public ResponseEntity<?> getJoinToken(@PathVariable Long id,
                                           @RequestParam(required = false) String displayName,
                                           Authentication auth) {
        try {
            String identity = auth.getName();
            return ResponseEntity.ok(service.generateJoinToken(id, identity, displayName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════
    // LOBBY — GUEST JOIN REQUESTS (all public, guarded by guestIdentity)
    // ═══════════════════════════════════════════════════════

//    @PostMapping("/{id}/join-requests")
//    public ResponseEntity<?> requestToJoin(@PathVariable Long id, @RequestBody Map<String, String> body) {
//        try {
//            return ResponseEntity.ok(service.requestToJoin(id, body.get("guestName")));
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
//        }
//    }
    @PostMapping("/{id}/join-requests")
    public ResponseEntity<?> requestToJoin(@PathVariable Long id, @RequestBody Map<String, String> body) {
        try {
            return ResponseEntity.ok(service.requestToJoin(id, body.get("guestName"), body.get("guestEmail")));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}/join-requests/{requestId}")
    public ResponseEntity<?> getJoinRequestStatus(@PathVariable Long id,
                                                   @PathVariable Long requestId,
                                                   @RequestParam String guestIdentity) {
        try {
            return ResponseEntity.ok(service.getJoinRequestStatus(id, requestId, guestIdentity));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/{id}/token/guest/{requestId}")
    public ResponseEntity<?> getGuestToken(@PathVariable Long id,
                                            @PathVariable Long requestId,
                                            @RequestParam String guestIdentity,
                                            @RequestParam(required = false) String displayName) {
        try {
            return ResponseEntity.ok(service.generateGuestToken(id, requestId, guestIdentity, displayName));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════
    // LOBBY — HOST-ONLY WAITING ROOM CONTROLS
    // ═══════════════════════════════════════════════════════

    @GetMapping("/{id}/join-requests")
    public ResponseEntity<?> listPendingJoinRequests(@PathVariable Long id, Authentication auth) {
        try {
            return ResponseEntity.ok(service.listPendingJoinRequests(id, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    
    @GetMapping("/{id}/join-requests/all")
    public ResponseEntity<?> listAllJoinRequests(@PathVariable Long id, Authentication auth) {
        try {
            return ResponseEntity.ok(service.listAllJoinRequests(id, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    
    @PostMapping("/{id}/join-requests/{requestId}/admit")
    public ResponseEntity<?> admitJoinRequest(@PathVariable Long id, @PathVariable Long requestId, Authentication auth) {
        try {
            return ResponseEntity.ok(service.admitJoinRequest(id, requestId, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/join-requests/{requestId}/deny")
    public ResponseEntity<?> denyJoinRequest(@PathVariable Long id, @PathVariable Long requestId, Authentication auth) {
        try {
            return ResponseEntity.ok(service.denyJoinRequest(id, requestId, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @PostMapping("/{id}/join-requests/admit-all")
    public ResponseEntity<?> admitAll(@PathVariable Long id, Authentication auth) {
        try {
            return ResponseEntity.ok(service.admitAll(id, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    
    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteMeeting(@PathVariable Long id, Authentication auth) {
        try {
            service.deleteMeeting(id, auth.getName());
            return ResponseEntity.ok(Map.of("deleted", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @GetMapping("/my/calendar")
    public ResponseEntity<?> getMyMeetingsCalendar(@RequestParam(required = false) String month,
                                                    Authentication auth) {
        try {
            return ResponseEntity.ok(service.getMyMeetingsGroupedByDate(auth.getName(), month));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    // ═══════════════════════════════════════════════════════
    // HELPERS
    // ═══════════════════════════════════════════════════════

    private String extractRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);
    }

    static class ErrorResponse {
        public String error;
        public ErrorResponse(String error) { this.error = error; }
    }
    
    @PostMapping("/{id}/summary/request")
    public ResponseEntity<?> requestSummary(@PathVariable Long id,
                                             @RequestBody MeetingSummaryRequestDTO dto,
                                             Authentication auth) {
        try {
            return ResponseEntity.ok(service.requestSummary(id, dto, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
}