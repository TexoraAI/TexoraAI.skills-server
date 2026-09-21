

package com.lms.live_session.controller;

import com.lms.live_session.dto.EventRequestDTO;
import com.lms.live_session.dto.EventResponseDTO;
import com.lms.live_session.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

import jakarta.servlet.http.HttpServletRequest;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/events")
public class EventController {

    private final EventService eventService;

    @Autowired
    public EventController(EventService eventService) {
        this.eventService = eventService;
    }

    @PostMapping
    public ResponseEntity<?> createEvent(@RequestBody EventRequestDTO dto, Authentication auth, HttpServletRequest request) {
        try {
            String creatorId = auth.getName();
            String creatorRole = extractRole(auth);
            String token = extractToken(request);
            EventResponseDTO created = eventService.createEvent(dto, creatorId, creatorRole, token);
            return ResponseEntity.ok(created);
        } catch (com.lms.live_session.exception.MeetingLimitExceededException e) {
            throw e; // let GlobalExceptionHandler produce 429 + MEETING_LIMIT_EXCEEDED
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
   

    @GetMapping
    public ResponseEntity<List<EventResponseDTO>> getMyEvents(Authentication auth) {
        return ResponseEntity.ok(eventService.getMyEvents(auth.getName()));
    }

    @GetMapping("/{id}")
    public ResponseEntity<?> getEventById(@PathVariable Long id, Authentication auth) {
        try {
            return ResponseEntity.ok(eventService.getEventById(id, auth.getName()));
        } catch (Exception e) {
            return ResponseEntity.notFound().build();
        }
    }

    @PutMapping("/{id}")
    public ResponseEntity<?> updateEvent(@PathVariable Long id, @RequestBody EventRequestDTO dto, Authentication auth) {
        try {
            EventResponseDTO updated = eventService.updateEvent(id, dto, auth.getName());
            return ResponseEntity.ok(updated);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<?> deleteEvent(@PathVariable Long id, Authentication auth) {
        try {
            eventService.deleteEvent(id, auth.getName());
            return ResponseEntity.ok(Map.of("deleted", true));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/calendar")
    public ResponseEntity<?> getMyEventsCalendar(@RequestParam(required = false) String month, Authentication auth) {
        try {
            return ResponseEntity.ok(eventService.getMyEventsGroupedByDate(auth.getName(), month));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    @GetMapping("/range")
    public ResponseEntity<?> getEventsByRange(@RequestParam String startDate,
                                               @RequestParam String endDate,
                                               Authentication auth) {
        try {
            return ResponseEntity.ok(eventService.getEventsByDateRange(
                    auth.getName(), LocalDate.parse(startDate), LocalDate.parse(endDate)));
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }
    
    @PutMapping("/{id}/restore")
    public ResponseEntity<?> restoreEvent(@PathVariable Long id, Authentication auth) {
        try {
            EventResponseDTO restored = eventService.restoreEvent(id, auth.getName());
            return ResponseEntity.ok(restored);
        } catch (Exception e) {
            return ResponseEntity.badRequest().body(new ErrorResponse(e.getMessage()));
        }
    }

    private String extractRole(Authentication auth) {
        return auth.getAuthorities().stream()
                .findFirst()
                .map(GrantedAuthority::getAuthority)
                .orElse(null);
    }

    // NEW — extracts the raw bearer token from the Authorization header so
    // the service layer can resolve organizationId server-side from the JWT
    // claim, instead of trusting a client-supplied value in the request body.
    // Only createEvent needs this — updateEvent never touches organizationId.
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
}