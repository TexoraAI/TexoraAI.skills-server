package com.lms.live_session.controller;
 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;
 
@RestController
@RequestMapping("/api/v1/ai-companion/activity")
public class AiActivityLogController {
 
    // GET /api/v1/ai-companion/activity
    @GetMapping
    public ResponseEntity<?> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        Principal principal
    ) {
        // TODO: return activityLogService.getByUser(principal.getName(), page, size)
        return ResponseEntity.ok(List.of());
    }
 
    // GET /api/v1/ai-companion/activity/{id}
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id, Principal principal) {
        // TODO: return activityLogService.getById(id, principal.getName())
        return ResponseEntity.ok(Map.of("id", id));
    }
}