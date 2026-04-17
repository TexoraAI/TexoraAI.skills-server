//package com.lms.notification.controller;
//
//import com.lms.notification.dto.NotificationDTO;
//import com.lms.notification.service.NotificationService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//import java.util.List;
//import java.util.Map;
//
//@RestController
//@RequestMapping("/api/notification")
//public class NotificationController {
//
//    private final NotificationService notificationService;
//
//    public NotificationController(NotificationService notificationService) {
//        this.notificationService = notificationService;
//    }
//
//    @GetMapping("/health")
//    public String health() { return "Notification Service OK"; }
//
//    @GetMapping("/my")
//    public ResponseEntity<List<NotificationDTO>> getMyNotifications() {
//        return ResponseEntity.ok(
//                notificationService.getMyNotifications(getEmail()));
//    }
//
//    @GetMapping("/unread-count")
//    public ResponseEntity<Map<String, Long>> getUnreadCount() {
//        return ResponseEntity.ok(
//                Map.of("count", notificationService.getUnreadCount(getEmail())));
//    }
//
//    @PutMapping("/{id}/read")
//    public ResponseEntity<Void> markRead(@PathVariable Long id) {
//        notificationService.markRead(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    @PutMapping("/read-all")
//    public ResponseEntity<Void> markAllRead() {
//        notificationService.markAllRead(getEmail());
//        return ResponseEntity.noContent().build();
//    }
// // In NotificationController.java
//
//    @DeleteMapping("/clear-all")
//    public ResponseEntity<Void> clearAll() {
//        notificationService.clearAll(getEmail());
//        return ResponseEntity.noContent().build();
//    }
//
//    private String getEmail() {
//        Authentication auth = SecurityContextHolder
//                .getContext().getAuthentication();
//        if (auth == null || auth.getName() == null)
//            throw new RuntimeException("Not authenticated");
//        return auth.getName();
//    }
//}

package com.lms.notification.controller;

import com.lms.notification.dto.NotificationDTO;
import com.lms.notification.security.JwtUtil;
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
    private final JwtUtil jwtUtil;

    public NotificationController(NotificationService notificationService, JwtUtil jwtUtil) {
        this.notificationService = notificationService;
        this.jwtUtil = jwtUtil;
    }

    @GetMapping("/health")
    public String health() {
        return "Notification Service OK";
    }

    @GetMapping("/my")
    public ResponseEntity<List<NotificationDTO>> getMyNotifications() {
        String email = getEmail();
        String userRole = getUserRole();
        
        System.out.println("🔍 DEBUG: email=" + email + ", role=" + userRole);
        
        return ResponseEntity.ok(
                notificationService.getMyNotifications(email, userRole));
    }

    @GetMapping("/unread-count")
    public ResponseEntity<Map<String, Long>> getUnreadCount() {
        String email = getEmail();
        String userRole = getUserRole();
        return ResponseEntity.ok(
                Map.of("count", notificationService.getUnreadCount(email, userRole)));
    }

    @PutMapping("/{id}/read")
    public ResponseEntity<Void> markRead(@PathVariable Long id) {
        notificationService.markRead(id);
        return ResponseEntity.noContent().build();
    }

    @PutMapping("/read-all")
    public ResponseEntity<Void> markAllRead() {
        String email = getEmail();
        String userRole = getUserRole();
        notificationService.markAllRead(email, userRole);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/clear-all")
    public ResponseEntity<Void> clearAll() {
        String email = getEmail();
        String userRole = getUserRole();
        notificationService.clearAll(email, userRole);
        return ResponseEntity.noContent().build();
    }

    private String getEmail() {
        Authentication auth = SecurityContextHolder
                .getContext().getAuthentication();
        if (auth == null || auth.getName() == null)
            throw new RuntimeException("Not authenticated");
        return auth.getName();
    }

    private String getUserRole() {
        try {
            Authentication auth = SecurityContextHolder
                    .getContext().getAuthentication();
            
            if (auth == null) {
                System.out.println("⚠️ No authentication found");
                return null;
            }

            // Extract role from authorities set by JwtFilter
            String role = auth.getAuthorities().stream()
                    .map(a -> a.getAuthority())
                    .filter(a -> a.startsWith("ROLE_"))
                    .map(a -> a.replace("ROLE_", "").toUpperCase())
                    .findFirst()
                    .orElse(null);

            if (role != null) {
                System.out.println("✅ Role extracted: " + role);
                return role;
            }

            System.out.println("⚠️ Could not extract role");
            return null;
            
        } catch (Exception e) {
            System.out.println("❌ Error extracting role: " + e.getMessage());
            e.printStackTrace();
            return null;
        }
    }
}