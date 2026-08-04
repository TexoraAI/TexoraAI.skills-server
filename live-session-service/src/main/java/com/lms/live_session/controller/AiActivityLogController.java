package com.lms.live_session.controller;
 
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.security.Principal;
import java.util.List;
import java.util.Map;
import com.lms.live_session.entity.AiActivityLog;
import com.lms.live_session.repository.AiActivityLogRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
@RestController
@RequestMapping("/api/v1/ai-companion/activity")
public class AiActivityLogController {
	private final AiActivityLogRepository activityLogRepository;

    public AiActivityLogController(AiActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }
//    // GET /api/v1/ai-companion/activity
//    @GetMapping
//    public ResponseEntity<?> list(
//        @RequestParam(defaultValue = "0") int page,
//        @RequestParam(defaultValue = "20") int size,
//        Principal principal
//    ) {
//        // TODO: return activityLogService.getByUser(principal.getName(), page, size)
//        return ResponseEntity.ok(List.of());
//    }
    @GetMapping
    public ResponseEntity<?> list(
        @RequestParam(defaultValue = "0") int page,
        @RequestParam(defaultValue = "20") int size,
        Principal principal
    ) {
        Page<AiActivityLog> result = activityLogRepository.findByUserEmailOrderByCreatedAtDesc(
            principal.getName(), PageRequest.of(page, size)
        );
        return ResponseEntity.ok(result);
    }
    
    
//    // GET /api/v1/ai-companion/activity/{id}
//    @GetMapping("/{id}")
//    public ResponseEntity<?> get(@PathVariable Long id, Principal principal) {
//        // TODO: return activityLogService.getById(id, principal.getName())
//        return ResponseEntity.ok(Map.of("id", id));
//    }
    @GetMapping("/{id}")
    public ResponseEntity<?> get(@PathVariable Long id, Principal principal) {
        return activityLogRepository.findById(id)
            .filter(log -> log.getUserEmail().equals(principal.getName()))
            .<ResponseEntity<?>>map(ResponseEntity::ok)
            .orElseGet(() -> ResponseEntity.notFound().build());
    }
}