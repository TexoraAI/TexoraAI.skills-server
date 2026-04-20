// com/lms/notification/service/FcmService.java
package com.lms.notification.service;

import com.google.firebase.messaging.*;
import com.lms.notification.model.DeviceToken;
import com.lms.notification.repository.DeviceTokenRepository;
import org.springframework.stereotype.Service;
import java.util.List;

@Service
public class FcmService {

    private final DeviceTokenRepository deviceTokenRepo;

    public FcmService(DeviceTokenRepository deviceTokenRepo) {
        this.deviceTokenRepo = deviceTokenRepo;
    }

    // Send push to a specific user (by email)
    public void sendToUser(String userEmail, String title, String body, String type) {
        List<DeviceToken> tokens = deviceTokenRepo.findByUserEmail(userEmail);
        if (tokens.isEmpty()) {
            System.out.println("⚠️ No FCM tokens for user: " + userEmail);
            return;
        }

        for (DeviceToken dt : tokens) {
            sendToToken(dt.getFcmToken(), title, body, type, userEmail);
        }
    }

    // Send push to all users of a role — you'll need to look up emails by role
    public void sendToTokenList(List<String> fcmTokens, String title, String body, String type) {
        for (String token : fcmTokens) {
            sendToToken(token, title, body, type, null);
        }
    }

    private void sendToToken(String token, String title, String body,
                              String type, String userEmail) {
        try {
            Message message = Message.builder()
                .setToken(token)
                .setNotification(
                    Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build()
                )
                .putData("type", type != null ? type : "DEFAULT")
                .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
                .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("✅ FCM sent: " + response);

        } catch (FirebaseMessagingException e) {
            System.out.println("❌ FCM failed for token: " + token + " → " + e.getMessage());

            // If token is invalid/expired, remove it from DB
            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                    || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                deviceTokenRepo.deleteByFcmToken(token);
                System.out.println("🗑️ Removed stale token");
            }
        }
    }
}