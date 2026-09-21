
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

import jakarta.servlet.http.HttpServletRequest;

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

//    @PostMapping("/instant")
//    public ResponseEntity<?> createInstantMeeting(@RequestBody(required = false) MeetingRequestDTO dto,
//                                                   Authentication auth,
//                                                   HttpServletRequest request) {
//        try {
//            MeetingRequestDTO body = dto != null ? dto : new MeetingRequestDTO();
//            String creatorId = auth.getName();
//            String creatorRole = extractRole(auth);
//            String token = extractToken(request);
//
//            MeetingResponseDTO created = service.createInstantMeeting(body, creatorId, creatorRole, token);
//            return ResponseEntity.ok(created);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to start instant meeting: " + e.getMessage()));
//        }
//    }
//
//    @PostMapping("/scheduled")
//    public ResponseEntity<?> createScheduledMeeting(@RequestBody MeetingRequestDTO dto, Authentication auth, HttpServletRequest request) {
//        try {
//            String creatorId = auth.getName();
//            String creatorRole = extractRole(auth);
//            String token = extractToken(request);
//
//            MeetingResponseDTO created = service.createScheduledMeeting(dto, creatorId, creatorRole, token);
//            return ResponseEntity.ok(created);
//        } catch (Exception e) {
//            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to schedule meeting: " + e.getMessage()));
//        }
//    }
    
    @PostMapping("/instant")
    public ResponseEntity<?> createInstantMeeting(@RequestBody(required = false) MeetingRequestDTO dto,
                                                   Authentication auth,
                                                   HttpServletRequest request) {
        try {
            MeetingRequestDTO body = dto != null ? dto : new MeetingRequestDTO();
            String creatorId = auth.getName();
            String creatorRole = extractRole(auth);
            String token = extractToken(request);

            MeetingResponseDTO created = service.createInstantMeeting(body, creatorId, creatorRole, token);
            return ResponseEntity.ok(created);
        } catch (com.lms.live_session.exception.MeetingLimitExceededException e) {
            throw e; // let GlobalExceptionHandler produce 429 + MEETING_LIMIT_EXCEEDED
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to start instant meeting: " + e.getMessage()));
        }
    }

    @PostMapping("/scheduled")
    public ResponseEntity<?> createScheduledMeeting(@RequestBody MeetingRequestDTO dto, Authentication auth, HttpServletRequest request) {
        try {
            String creatorId = auth.getName();
            String creatorRole = extractRole(auth);
            String token = extractToken(request);

            MeetingResponseDTO created = service.createScheduledMeeting(dto, creatorId, creatorRole, token);
            return ResponseEntity.ok(created);
        } catch (com.lms.live_session.exception.MeetingLimitExceededException e) {
            throw e; // let GlobalExceptionHandler produce 429 + MEETING_LIMIT_EXCEEDED
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

    @GetMapping("/usage")
    public ResponseEntity<?> getMeetingUsage(Authentication auth, HttpServletRequest request) {
        try {
            String creatorId = auth.getName();
            String token = extractToken(request);
            return ResponseEntity.ok(service.getMeetingUsage(creatorId, token));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
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
            // ✅ NEW: Generate unique sessionId for this token request
            String sessionId = java.util.UUID.randomUUID().toString();
            return ResponseEntity.ok(service.generateJoinToken(id, identity, displayName, sessionId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to generate token: " + e.getMessage()));
        }
    }

    // ═══════════════════════════════════════════════════════
    // LOBBY — GUEST JOIN REQUESTS (all public, guarded by guestIdentity)
    // ═══════════════════════════════════════════════════════


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
            // ✅ NEW: Generate unique sessionId for this guest token request
            String sessionId = java.util.UUID.randomUUID().toString();
            return ResponseEntity.ok(service.generateGuestToken(id, requestId, guestIdentity, displayName, sessionId));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to generate guest token: " + e.getMessage()));
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

    // NEW — extracts the raw bearer token from the Authorization header so
    // the service layer can resolve organizationId server-side from the JWT
    // claim, instead of trusting a client-supplied value in the request body.
    private String extractToken(HttpServletRequest request) {
        String header = request.getHeader("Authorization");
        if (header != null && header.startsWith("Bearer ")) {
            return header.substring(7);
        }
        return null;
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
    @PostMapping("/permanent")
    public ResponseEntity<?> createPermanentMeeting(@RequestBody MeetingRequestDTO dto, Authentication auth, HttpServletRequest request) {
        try {
            String creatorId = auth.getName();
            String creatorRole = extractRole(auth);
            String token = extractToken(request);
            MeetingResponseDTO created = service.createPermanentMeeting(dto, creatorId, creatorRole, token);
            return ResponseEntity.ok(created);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse("Failed to create Task Orbit meeting: " + e.getMessage()));
        }
    }

    @GetMapping("/permanent/my")
    public ResponseEntity<?> getMyPermanentMeetings(Authentication auth) {
        try {
            return ResponseEntity.ok(service.getMyPermanentMeetings(auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    
 // ═══════════════════════════════════════════════════════
 // TOKEN REFRESH — HOST (keeps a long-running connection alive
 // past its current token's expiry, without disconnecting)
 // ═══════════════════════════════════════════════════════

 @GetMapping("/{id}/token/refresh")
 public ResponseEntity<?> refreshHostToken(@PathVariable Long id,
                                            @RequestParam(required = false) String displayName,
                                            Authentication auth) {
     try {
         String identity = auth.getName();
         String sessionId = java.util.UUID.randomUUID().toString();
         return ResponseEntity.ok(service.generateJoinToken(id, identity, displayName, sessionId));
     } catch (Exception e) {
         return ResponseEntity.badRequest().body(new ErrorResponse("Failed to refresh token: " + e.getMessage()));
     }
 }

 // ═══════════════════════════════════════════════════════
 // TOKEN REFRESH — GUEST
 // ═══════════════════════════════════════════════════════

 @GetMapping("/{id}/token/guest/{requestId}/refresh")
 public ResponseEntity<?> refreshGuestToken(@PathVariable Long id,
                                             @PathVariable Long requestId,
                                             @RequestParam String guestIdentity,
                                             @RequestParam(required = false) String displayName) {
     try {
         String sessionId = java.util.UUID.randomUUID().toString();
         return ResponseEntity.ok(service.generateGuestToken(id, requestId, guestIdentity, displayName, sessionId));
     } catch (Exception e) {
         return ResponseEntity.badRequest().body(new ErrorResponse("Failed to refresh guest token: " + e.getMessage()));
     }
 }
}