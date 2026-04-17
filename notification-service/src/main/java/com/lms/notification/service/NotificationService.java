//package com.lms.notification.service;
//
//import com.lms.notification.dto.NotificationDTO;
//import com.lms.notification.model.Notification;
//import com.lms.notification.repository.NotificationRepository;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.List;
//import java.util.stream.Collectors;
//import org.springframework.transaction.annotation.Transactional;
//@Service
//public class NotificationService {
//
//    private final NotificationRepository repo;
//    private final SimpMessagingTemplate messagingTemplate;
//
//    public NotificationService(NotificationRepository repo,
//                                SimpMessagingTemplate messagingTemplate) {
//        this.repo = repo;
//        this.messagingTemplate = messagingTemplate;
//    }
//
//    // called by every Kafka consumer
//    public void createAndPush(NotificationDTO dto) {
//
//        if (dto.getTargetUserIds() != null && !dto.getTargetUserIds().isEmpty()) {
//            for (String userId : dto.getTargetUserIds()) {
//                Notification saved = saveToDb(userId, null, dto);
//                messagingTemplate.convertAndSend(
//                        "/topic/notifications/user/" + userId,
//                        toClientDTO(saved));
//            }
//        } else if (dto.getTargetRole() != null) {
//            Notification saved = saveToDb(null, dto.getTargetRole(), dto);
//            messagingTemplate.convertAndSend(
//                    "/topic/notifications/role/" + dto.getTargetRole().toUpperCase(),
//                    toClientDTO(saved));
//        }
//    }
//
//    public List<NotificationDTO> getMyNotifications(String userId) {
//        return repo.findByUserIdOrderByCreatedAtDesc(userId)
//                .stream().map(this::toClientDTO).collect(Collectors.toList());
//    }
//
//    public void markRead(Long id) {
//        repo.findById(id).ifPresent(n -> { n.setRead(true); repo.save(n); });
//    }
//
//    public void markAllRead(String userId) {
//        repo.markAllReadByUserId(userId);
//    }
//    @Transactional
//    public void clearAll(String email) {
//        repo.deleteAllByUserId(email);
//    }
//
//    public long getUnreadCount(String userId) {
//        return repo.countByUserIdAndReadFalse(userId);
//    }
//
//    private Notification saveToDb(String userId, String userRole,
//                                   NotificationDTO dto) {
//        Notification n = new Notification();
//        n.setUserId(userId);
//        n.setUserRole(userRole);
//        n.setType(dto.getType());
//        n.setTitle(dto.getTitle());
//        n.setMessage(dto.getMessage());
//        return repo.save(n);
//    }
//
//    private NotificationDTO toClientDTO(Notification n) {
//        NotificationDTO dto = new NotificationDTO();
//        dto.setId(n.getId());
//        dto.setType(n.getType());
//        dto.setTitle(n.getTitle());
//        dto.setMessage(n.getMessage());
//        dto.setRead(n.isRead());
//        dto.setCreatedAt(n.getCreatedAt());
//        return dto;
//    }
//}


package com.lms.notification.service;

import com.lms.notification.dto.NotificationDTO;
import com.lms.notification.model.Notification;
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

    public NotificationService(NotificationRepository repo,
                                SimpMessagingTemplate messagingTemplate) {
        this.repo = repo;
        this.messagingTemplate = messagingTemplate;
    }

    // called by every Kafka consumer
    public void createAndPush(NotificationDTO dto) {

        if (dto.getTargetUserIds() != null && !dto.getTargetUserIds().isEmpty()) {
            for (String userId : dto.getTargetUserIds()) {
                Notification saved = saveToDb(userId, null, dto);
                messagingTemplate.convertAndSend(
                        "/topic/notifications/user/" + userId,
                        toClientDTO(saved));
            }
        } else if (dto.getTargetRole() != null) {
            Notification saved = saveToDb(null, dto.getTargetRole(), dto);
            messagingTemplate.convertAndSend(
                    "/topic/notifications/role/" + dto.getTargetRole().toUpperCase(),
                    toClientDTO(saved));
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