package com.lms.live_session.controller;

import com.lms.live_session.dto.EventRequestDTO;
import com.lms.live_session.dto.EventResponseDTO;
import com.lms.live_session.service.EventService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.web.bind.annotation.*;

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
    public ResponseEntity<?> createEvent(@RequestBody EventRequestDTO dto, Authentication auth) {
        try {
            String creatorId = auth.getName();
            String creatorRole = extractRole(auth);
            EventResponseDTO created = eventService.createEvent(dto, creatorId, creatorRole);
            return ResponseEntity.ok(created);
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

    static class ErrorResponse {
        public String error;
        public ErrorResponse(String error) { this.error = error; }
    }
}