//package com.lms.notification.service;
//
//import com.lms.notification.dto.NotificationDTO;
//import com.lms.notification.model.DeviceToken;
//import com.lms.notification.model.Notification;
//import com.lms.notification.repository.DeviceTokenRepository;
//import com.lms.notification.repository.NotificationRepository;
//import org.springframework.messaging.simp.SimpMessagingTemplate;
//import org.springframework.stereotype.Service;
//
//import java.util.ArrayList;
//import java.util.List;
//import java.util.stream.Collectors;
//import org.springframework.transaction.annotation.Transactional;
//
//@Service
//public class NotificationService {
//
//    private final NotificationRepository repo;
//    private final SimpMessagingTemplate messagingTemplate;
//    private final FcmService fcmService;
//    private final DeviceTokenRepository deviceTokenRepo;
//
//    public NotificationService(NotificationRepository repo,
//            SimpMessagingTemplate messagingTemplate,
//            FcmService fcmService,
//            DeviceTokenRepository deviceTokenRepo) {
//        this.repo = repo;
//        this.messagingTemplate = messagingTemplate;
//        this.fcmService = fcmService;
//        this.deviceTokenRepo = deviceTokenRepo;
//    }
//
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
//
//        } else if (dto.getTargetRole() != null) {
//            Notification saved = saveToDb(null, dto.getTargetRole(), dto);
//            messagingTemplate.convertAndSend(
//                    "/topic/notifications/role/" + dto.getTargetRole().toUpperCase(),
//                    toClientDTO(saved));
//
//            List<DeviceToken> tokens = deviceTokenRepo.findByUserRole(dto.getTargetRole());
//            List<String> fcmTokens = tokens.stream()
//                    .map(DeviceToken::getFcmToken)
//                    .collect(Collectors.toList());
//
//            if (!fcmTokens.isEmpty()) {
//                fcmService.sendToTokenList(fcmTokens, dto.getTitle(),
//                                           dto.getMessage(), dto.getType());
//                System.out.println("📢 Role-based FCM push sent to "
//                                   + fcmTokens.size() + " devices");
//            } else {
//                System.out.println("⚠️ No FCM tokens found for role: " + dto.getTargetRole());
//            }
//        }
//    }
//
//    public List<NotificationDTO> getMyNotifications(String userId, String userRole) {
//        System.out.println("🔍 getMyNotifications called - userId: " + userId + ", userRole: " + userRole);
//
//        List<Notification> all = new ArrayList<>();
//
//        List<Notification> byUser = repo.findByUserIdOrderByCreatedAtDesc(userId);
//        System.out.println("📬 Notifications by userId: " + byUser.size());
//        all.addAll(byUser);
//
//        if (userRole != null && !userRole.isEmpty()) {
//            List<Notification> roleNotifications = repo.findByUserRoleOrderByCreatedAtDesc(userRole);
//            System.out.println("📢 Notifications by role (" + userRole + "): " + roleNotifications.size());
//
//            for (Notification n : roleNotifications) {
//                if (!all.stream().anyMatch(x -> x.getId().equals(n.getId()))) {
//                    all.add(n);
//                }
//            }
//        } else {
//            System.out.println("⚠️ No userRole provided!");
//        }
//
//        all.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
//
//        System.out.println("✅ Total notifications: " + all.size());
//        return all.stream()
//                .map(this::toClientDTO)
//                .collect(Collectors.toList());
//    }
//
//    public long getUnreadCount(String userId, String userRole) {
//        long countByUser = repo.countByUserIdAndReadFalse(userId);
//        long countByRole = 0;
//        if (userRole != null && !userRole.isEmpty()) {
//            countByRole = repo.countByUserRoleAndReadFalse(userRole);
//        }
//        return countByUser + countByRole;
//    }
//
//    public void markRead(Long id) {
//        repo.findById(id).ifPresent(n -> {
//            n.setRead(true);
//            repo.save(n);
//        });
//    }
//
//    public void markAllRead(String userId, String userRole) {
//        repo.markAllReadByUserId(userId);
//        if (userRole != null && !userRole.isEmpty()) {
//            repo.markAllReadByUserRole(userRole);
//        }
//    }
//
//    @Transactional
//    public void clearAll(String userId, String userRole) {
//        repo.deleteAllByUserId(userId);
//        if (userRole != null && !userRole.isEmpty()) {
//            repo.deleteAllByUserRole(userRole);
//        }
//    }
//
//    public void registerDeviceToken(String email, String fcmToken,
//            String deviceType, String userRole) {
//
//        deviceTokenRepo.findByFcmToken(fcmToken).ifPresentOrElse(
//            existing -> {
//                // ✅ FIX: token exists but may belong to old email/role
//                // Always update to current logged-in user's email and role
//                boolean changed = false;
//
//                if (email != null && !email.equalsIgnoreCase(existing.getUserEmail())) {
//                    existing.setUserEmail(email);
//                    changed = true;
//                    System.out.println("🔄 FCM token email updated: "
//                        + existing.getUserEmail() + " → " + email);
//                }
//
//                if (userRole != null && !userRole.equalsIgnoreCase(existing.getUserRole())) {
//                    existing.setUserRole(userRole);
//                    changed = true;
//                    System.out.println("🔄 FCM token role updated: "
//                        + existing.getUserRole() + " → " + userRole);
//                }
//
//                if (changed) {
//                    deviceTokenRepo.save(existing);
//                    System.out.println("✅ FCM token updated for: " + email);
//                } else {
//                    System.out.println("✅ Token already registered and up to date for: " + email);
//                }
//            },
//            () -> {
//                // New token — save fresh
//                DeviceToken dt = new DeviceToken();
//                dt.setUserEmail(email);
//                dt.setFcmToken(fcmToken);
//                dt.setDeviceType(deviceType);
//                dt.setUserRole(userRole);
//                deviceTokenRepo.save(dt);
//                System.out.println("✅ New FCM token registered for: " + email
//                    + " role: " + userRole);
//            }
//        );
//    }
//
//    public void removeDeviceToken(String fcmToken) {
//        deviceTokenRepo.deleteByFcmToken(fcmToken);
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

    public NotificationService(NotificationRepository repo,
            SimpMessagingTemplate messagingTemplate,
            FcmService fcmService,
            DeviceTokenRepository deviceTokenRepo) {
        this.repo = repo;
        this.messagingTemplate = messagingTemplate;
        this.fcmService = fcmService;
        this.deviceTokenRepo = deviceTokenRepo;
    }

    public void createAndPush(NotificationDTO dto) {

        if (dto.getTargetUserIds() != null && !dto.getTargetUserIds().isEmpty()) {
            // Per-user notifications (video, file, quiz, assignment etc.)
            for (String userId : dto.getTargetUserIds()) {
                Notification saved = saveToDb(userId, null, dto);
                messagingTemplate.convertAndSend(
                        "/topic/notifications/user/" + userId,
                        toClientDTO(saved));
                // ✅ FcmService uses case-insensitive lookup now
                fcmService.sendToUser(userId, dto.getTitle(), dto.getMessage(), dto.getType());
            }

        } else if (dto.getTargetRole() != null) {
            // ✅ FIX: always uppercase role before saving and querying
            String role = dto.getTargetRole().toUpperCase();

            Notification saved = saveToDb(null, role, dto);
            messagingTemplate.convertAndSend(
                    "/topic/notifications/role/" + role,
                    toClientDTO(saved));

            // ✅ FIX: use case-insensitive JPQL query
            List<DeviceToken> tokens = deviceTokenRepo.findByUserRoleCaseInsensitive(role);
            List<String> fcmTokens = tokens.stream()
                    .map(DeviceToken::getFcmToken)
                    .collect(Collectors.toList());

            if (!fcmTokens.isEmpty()) {
                fcmService.sendToTokenList(fcmTokens, dto.getTitle(),
                                           dto.getMessage(), dto.getType());
                System.out.println("📢 Role FCM sent to "
                                   + fcmTokens.size() + " devices, role=" + role);
            } else {
                System.out.println("⚠️ No FCM tokens for role: " + role);
            }
        }
    }

    public List<NotificationDTO> getMyNotifications(String userId, String userRole) {
        System.out.println("🔍 getMyNotifications userId=" + userId + " role=" + userRole);

        List<Notification> all = new ArrayList<>();

        List<Notification> byUser = repo.findByUserIdOrderByCreatedAtDesc(userId);
        System.out.println("📬 By userId: " + byUser.size());
        all.addAll(byUser);

        if (userRole != null && !userRole.isEmpty()) {
            // ✅ FIX: normalize role
            String normalizedRole = userRole.toUpperCase();
            List<Notification> roleNotifs =
                    repo.findByUserRoleOrderByCreatedAtDesc(normalizedRole);
            System.out.println("📢 By role (" + normalizedRole + "): " + roleNotifs.size());

            for (Notification n : roleNotifs) {
                if (all.stream().noneMatch(x -> x.getId().equals(n.getId()))) {
                    all.add(n);
                }
            }
        }

        all.sort((a, b) -> b.getCreatedAt().compareTo(a.getCreatedAt()));
        System.out.println("✅ Total: " + all.size());

        return all.stream().map(this::toClientDTO).collect(Collectors.toList());
    }

    public long getUnreadCount(String userId, String userRole) {
        long countByUser = repo.countByUserIdAndReadFalse(userId);
        long countByRole = 0;
        if (userRole != null && !userRole.isEmpty()) {
            countByRole = repo.countByUserRoleAndReadFalse(userRole.toUpperCase());
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
            repo.markAllReadByUserRole(userRole.toUpperCase());
        }
    }

    @Transactional
    public void clearAll(String userId, String userRole) {
        repo.deleteAllByUserId(userId);
        if (userRole != null && !userRole.isEmpty()) {
            repo.deleteAllByUserRole(userRole.toUpperCase());
        }
    }

    public void registerDeviceToken(String email, String fcmToken,
            String deviceType, String userRole) {

        // ✅ FIX: always normalize — lowercase email, uppercase role
        String normEmail = (email != null) ? email.toLowerCase().trim() : null;
        String normRole  = (userRole != null) ? userRole.toUpperCase().trim() : null;

        deviceTokenRepo.findByFcmToken(fcmToken).ifPresentOrElse(
            existing -> {
                // ✅ FIX: update email+role if changed (handles switching accounts)
                boolean changed = false;

                if (normEmail != null
                        && !normEmail.equalsIgnoreCase(existing.getUserEmail())) {
                    System.out.println("🔄 Token email: "
                            + existing.getUserEmail() + " → " + normEmail);
                    existing.setUserEmail(normEmail);
                    changed = true;
                }

                if (normRole != null
                        && !normRole.equalsIgnoreCase(existing.getUserRole())) {
                    System.out.println("🔄 Token role: "
                            + existing.getUserRole() + " → " + normRole);
                    existing.setUserRole(normRole);
                    changed = true;
                }

                if (changed) {
                    deviceTokenRepo.save(existing);
                    System.out.println("✅ Token updated: " + normEmail + " role=" + normRole);
                } else {
                    System.out.println("✅ Token up to date: " + normEmail);
                }
            },
            () -> {
                DeviceToken dt = new DeviceToken();
                dt.setUserEmail(normEmail);
                dt.setFcmToken(fcmToken);
                dt.setDeviceType(deviceType);
                dt.setUserRole(normRole);
                deviceTokenRepo.save(dt);
                System.out.println("✅ New token saved: "
                        + normEmail + " role=" + normRole);
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