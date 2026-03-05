package com.lms.notification.controller;

import com.lms.notification.model.NotificationRequest;
import com.lms.notification.service.NotificationService;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/notification")
public class NotificationController {
//jkfnfdnf
    private final NotificationService notificationService;

    public NotificationController(NotificationService notificationService) {
        this.notificationService = notificationService;
    }

    // Health check
    @GetMapping("/health")
    public String health() {
        return "Notification Service OK";
    }

    // Send notification
    @PostMapping("/send")
    public String sendNotification(@RequestBody NotificationRequest request) {
        return notificationService.sendNotification(request.getMessage());
    }
}
