


package com.lms.notification.service;

import com.lms.notification.dto.NotificationDTO;
import com.lms.notification.model.DeviceToken;
import com.lms.notification.model.Notification;
import com.lms.notification.repository.DeviceTokenRepository;
import com.lms.notification.repository.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.transaction.annotation.Transactional;

@Service
public class NotificationService {

    private final NotificationRepository repo;
    private final SimpMessagingTemplate messagingTemplate;
    private final FcmService fcmService;
    private final DeviceTokenRepository deviceTokenRepo;

//    public NotificationService(NotificationRepository repo,
//                                SimpMessagingTemplate messagingTemplateFcmService fcmService) {
//        this.repo = repo;
//        this.messagingTemplate = messagingTemplate;
//    }
    public NotificationService(NotificationRepository repo,
            SimpMessagingTemplate messagingTemplate,
            FcmService fcmService,DeviceTokenRepository deviceTokenRepo) {
this.repo = repo;
this.messagingTemplate = messagingTemplate;
this.fcmService = fcmService;
this.deviceTokenRepo = deviceTokenRepo;

}

    
    
    // called by every Kafka consumer
//    public void createAndPush(NotificationDTO dto) {
//
//        if (dto.getTargetUserIds() != null && !dto.getTargetUserIds().isEmpty()) {
//            for (String userId : dto.getTargetUserIds()) {
//                Notification saved = saveToDb(userId, null, dto);
//                messagingTemplate.convertAndSend(
//                        "/topic/notifications/user/" + userId,
//                        toClientDTO(saved));
//                fcmService.sendToUser(userId, dto.getTitle(), dto.getMessage(), dto.getType());
//            }
//        } else if (dto.getTargetRole() != null) {
//            Notification saved = saveToDb(null, dto.getTargetRole(), dto);
//            messagingTemplate.convertAndSend(
//                    "/topic/notifications/role/" + dto.getTargetRole().toUpperCase(),
//                    toClientDTO(saved));
//            // 3. FCM — fetch all tokens for this role from DB and send
//            // You need a query: deviceTokenRepo.findByUserRole(dto.getTargetRole())
//            // For now log it
//            System.out.println("📢 Role-based FCM push for: " + dto.getTargetRole());
//        }
//    }
    public void createAndPush(NotificationDTO dto) {

        if (dto.getTargetUserIds() != null && !dto.getTargetUserIds().isEmpty()) {
            // Per-user notifications
            for (String userId : dto.getTargetUserIds()) {
                Notification saved = saveToDb(userId, null, dto);
                messagingTemplate.convertAndSend(
                        "/topic/notifications/user/" + userId,
                        toClientDTO(saved));
                // FCM push to this specific user
                fcmService.sendToUser(userId, dto.getTitle(), dto.getMessage(), dto.getType());
            }

        } else if (dto.getTargetRole() != null) {
            // Role-based notification — save once to DB
            Notification saved = saveToDb(null, dto.getTargetRole(), dto);
            messagingTemplate.convertAndSend(
                    "/topic/notifications/role/" + dto.getTargetRole().toUpperCase(),
                    toClientDTO(saved));

            // ✅ FIX: Fetch ALL device tokens for this role and send FCM
//            List<DeviceToken> tokens = deviceTokenRepo.findAll(); // get all tokens
            List<DeviceToken> tokens = deviceTokenRepo.findByUserRole(dto.getTargetRole());
            List<String> fcmTokens = tokens.stream()
                    .map(DeviceToken::getFcmToken)
                    .collect(java.util.stream.Collectors.toList());

            if (!fcmTokens.isEmpty()) {
                fcmService.sendToTokenList(fcmTokens, dto.getTitle(), 
                                           dto.getMessage(), dto.getType());
                System.out.println("📢 Role-based FCM push sent to " 
                                   + fcmTokens.size() + " devices");
            } else {
                System.out.println("⚠️ No FCM tokens found for role: " + dto.getTargetRole());
            }
        }
    }
    public List<NotificationDTO> getMyNotifications(String userId, String userRole) {
        System.out.println("🔍 getMyNotifications called - userId: " + userId + ", userRole: " + userRole);
        
        List<Notification> all = new ArrayList<>();

        // Get notifications sent to this specific user
        List<Notification> byUser = repo.findByUserIdOrderByCreatedAtDesc(userId);
        System.out.println("📬 Notifications by userId: " + byUser.size());
        all.addAll(byUser);

        // Get notifications broadcast to their role (for admin/trainer)
        if (userRole != null && !userRole.isEmpty()) {
            List<Notification> roleNotifications = repo.findByUserRoleOrderByCreatedAtDesc(userRole);
            System.out.println("📢 Notifications by role (" + userRole + "): " + roleNotifications.size());
            
            for (Notification n : roleNotifications) {
                if (!all.stream().anyMatch(x -> x.getId().equals(n.getId()))) {
                    all.add(n);
                }
            }
        } else {
            System.out.println("⚠️ No userRole provided!");
        }

        // Sort by createdAt DESC
        all.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));

        System.out.println("✅ Total notifications: " + all.size());
        return all.stream()
                .map(this::toClientDTO)
                .collect(Collectors.toList());
    }

    public long getUnreadCount(String userId, String userRole) {
        long countByUser = repo.countByUserIdAndReadFalse(userId);
        long countByRole = 0;

        if (userRole != null && !userRole.isEmpty()) {
            countByRole = repo.countByUserRoleAndReadFalse(userRole);
        }

        return countByUser + countByRole;
    }

    public void markRead(Long id) {
        repo.findById(id).ifPresent(n -> {
            n.setRead(true);
            repo.save(n);
        });
    }

    public void markAllRead(String userId, String userRole) {
        repo.markAllReadByUserId(userId);

        if (userRole != null && !userRole.isEmpty()) {
            repo.markAllReadByUserRole(userRole);
        }
    }

    @Transactional
    public void clearAll(String userId, String userRole) {
        repo.deleteAllByUserId(userId);

        if (userRole != null && !userRole.isEmpty()) {
            repo.deleteAllByUserRole(userRole);
        }
    }
//    public void registerDeviceToken(String email, String fcmToken, String deviceType) {
//        // Avoid duplicates
//        deviceTokenRepo.findByFcmToken(fcmToken).ifPresentOrElse(
//            existing -> System.out.println("Token already registered"),
//            () -> {
//                DeviceToken dt = new DeviceToken();
//                dt.setUserEmail(email);
//                dt.setFcmToken(fcmToken);
//                dt.setDeviceType(deviceType);
//                deviceTokenRepo.save(dt);
//                System.out.println("✅ Device token registered for: " + email);
//            }
//        );
//    }
    public void registerDeviceToken(String email, String fcmToken, 
            String deviceType, String userRole) {
deviceTokenRepo.findByFcmToken(fcmToken).ifPresentOrElse(
existing -> System.out.println("Token already registered"),
() -> {
DeviceToken dt = new DeviceToken();
dt.setUserEmail(email);
dt.setFcmToken(fcmToken);
dt.setDeviceType(deviceType);
dt.setUserRole(userRole);   // ← save role
deviceTokenRepo.save(dt);
System.out.println("✅ Device token registered for: " + email);
}
);
}

    public void removeDeviceToken(String fcmToken) {
        deviceTokenRepo.deleteByFcmToken(fcmToken);
    }

    private Notification saveToDb(String userId, String userRole,
                                   NotificationDTO dto) {
        Notification n = new Notification();
        n.setUserId(userId);
        n.setUserRole(userRole);
        n.setType(dto.getType());
        n.setTitle(dto.getTitle());
        n.setMessage(dto.getMessage());
        return repo.save(n);
    }

    private NotificationDTO toClientDTO(Notification n) {
        NotificationDTO dto = new NotificationDTO();
        dto.setId(n.getId());
        dto.setType(n.getType());
        dto.setTitle(n.getTitle());
        dto.setMessage(n.getMessage());
        dto.setRead(n.isRead());
        dto.setCreatedAt(n.getCreatedAt());
        return dto;
    }
}