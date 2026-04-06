package com.lms.notification.service;

import com.lms.notification.dto.NotificationDTO;
import com.lms.notification.model.Notification;
import com.lms.notification.repository.NotificationRepository;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

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

    public List<NotificationDTO> getMyNotifications(String userId) {
        return repo.findByUserIdOrderByCreatedAtDesc(userId)
                .stream().map(this::toClientDTO).collect(Collectors.toList());
    }

    public void markRead(Long id) {
        repo.findById(id).ifPresent(n -> { n.setRead(true); repo.save(n); });
    }

    public void markAllRead(String userId) {
        repo.markAllReadByUserId(userId);
    }
    @Transactional
    public void clearAll(String email) {
        repo.deleteAllByUserId(email);
    }

    public long getUnreadCount(String userId) {
        return repo.countByUserIdAndReadFalse(userId);
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