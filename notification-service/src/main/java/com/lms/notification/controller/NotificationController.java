package com.lms.notification.controller;

import com.lms.notification.dto.NotificationDTO;
import com.lms.notification.service.NotificationService;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.*;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {

    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    @GetMapping("/health")
    public String health() { return "Notification Service OK"; }

    @GetMapping("/my")
    public ResponseEntity<List<NotificationDTO>> getMyNotifications() {
        return ResponseEntity.ok(
                notificationService.getMyNotifications(getEmail()));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        return ResponseEntity.ok(
                Map.of("count", notificationService.getUnreadCount(getEmail())));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        notificationService.markAllRead(getEmail());
        return ResponseEntity.noContent().build();
    }
 // In NotificationController.java

    @DeleteMapping("/clear-all")
    public ResponseEntity<Void> clearAll() {
        notificationService.clearAll(getEmail());
        return ResponseEntity.noContent().build();
    }

    private String getEmail() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null || auth.getName() == null)
            throw new RuntimeException("Not authenticated");
        return auth.getName();
    }
}