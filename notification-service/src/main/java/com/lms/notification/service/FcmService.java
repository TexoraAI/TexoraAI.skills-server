//
//package com.lms.notification.service;
//
//import com.google.firebase.messaging.*;
//import com.lms.notification.model.DeviceToken;
//import com.lms.notification.repository.DeviceTokenRepository;
//import org.springframework.stereotype.Service;
//import java.util.List;
//
//@Service
//public class FcmService {
//
//    private final DeviceTokenRepository deviceTokenRepo;
//
//    public FcmService(DeviceTokenRepository deviceTokenRepo) {
//        this.deviceTokenRepo = deviceTokenRepo;
//    }
//
//    // Send push to a specific user (by email)
//    public void sendToUser(String userEmail, String title, String body, String type) {
//        List<DeviceToken> tokens = deviceTokenRepo.findByUserEmail(userEmail);
//        if (tokens.isEmpty()) {
//            System.out.println("⚠️ No FCM tokens for user: " + userEmail);
//            return;
//        }
//        for (DeviceToken dt : tokens) {
//            // ✅ FIX: pass dt.getUserRole() so SW opens correct URL per role
//            sendToToken(dt.getFcmToken(), title, body, type, dt.getUserRole());
//        }
//    }
//
//    // Send push to a list of tokens (role-based)
//    public void sendToTokenList(List<String> fcmTokens, String title,
//                                 String body, String type) {
//        for (String fcmToken : fcmTokens) {
//            // ✅ FIX: look up role from DB instead of passing null
//            DeviceToken dt = deviceTokenRepo.findByFcmToken(fcmToken).orElse(null);
//            String role = (dt != null) ? dt.getUserRole() : null;
//            sendToToken(fcmToken, title, body, type, role);
//        }
//    }
//
//    private void sendToToken(String token, String title, String body,
//                              String type, String userRole) {
//        try {
//            Message message = Message.builder()
//                .setToken(token)
//                .setNotification(
//                    Notification.builder()
//                        .setTitle(title)
//                        .setBody(body)
//                        .build()
//                )
//                .putData("type",     type     != null ? type     : "DEFAULT")
//                // ✅ FIX: send userRole so firebase-messaging-sw.js opens
//                //         correct URL — /trainer/notifications or /student/notifications
//                .putData("userRole", userRole != null ? userRole.toLowerCase() : "student")
//                .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
//                .build();
//
//            String response = FirebaseMessaging.getInstance().send(message);
//            System.out.println("✅ FCM sent to " + userRole + ": " + response);
//
//        } catch (FirebaseMessagingException e) {
//            System.out.println("❌ FCM failed for token: " + token
//                               + " → " + e.getMessage());
//
//            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
//                    || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
//                deviceTokenRepo.deleteByFcmToken(token);
//                System.out.println("🗑️ Removed stale token");
//            }
//        }
//    }
//}


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

    public void sendToUser(String userEmail, String title, String body, String type) {
        if (userEmail == null) return;

        // ✅ FIX: case-insensitive email lookup — MySQL is case-sensitive by default
        // findByUserEmailIgnoreCase ensures student@gmail.com == Student@gmail.com
        List<DeviceToken> tokens = deviceTokenRepo.findByUserEmailIgnoreCase(userEmail);

        if (tokens.isEmpty()) {
            System.out.println("⚠️ No FCM tokens for user: " + userEmail);
            return;
        }

        System.out.println("📱 Sending FCM to " + tokens.size()
            + " device(s) for: " + userEmail);

        for (DeviceToken dt : tokens) {
            sendToToken(dt.getFcmToken(), title, body, type, dt.getUserRole());
        }
    }

    public void sendToTokenList(List<String> fcmTokens, String title,
                                 String body, String type) {
        if (fcmTokens == null || fcmTokens.isEmpty()) return;

        System.out.println("📢 Sending FCM to " + fcmTokens.size() + " role-based token(s)");

        for (String fcmToken : fcmTokens) {
            DeviceToken dt = deviceTokenRepo.findByFcmToken(fcmToken).orElse(null);
            String role = (dt != null) ? dt.getUserRole() : null;
            sendToToken(fcmToken, title, body, type, role);
        }
    }

    private void sendToToken(String token, String title, String body,
                              String type, String userRole) {
        try {
            Message message = Message.builder()
                .setToken(token)
                .setNotification(
                    Notification.builder()
                        .setTitle(title)
                        .setBody(body)
                        .build()
                )
                .putData("type",         type     != null ? type                      : "DEFAULT")
                .putData("userRole",     userRole != null ? userRole.toLowerCase()    : "student")
                .putData("click_action", "FLUTTER_NOTIFICATION_CLICK")
                .build();

            String response = FirebaseMessaging.getInstance().send(message);
            System.out.println("✅ FCM sent to role=" + userRole + " → " + response);

        } catch (FirebaseMessagingException e) {
            System.out.println("❌ FCM failed for token: " + token
                               + " → " + e.getMessage());

            if (e.getMessagingErrorCode() == MessagingErrorCode.UNREGISTERED
                    || e.getMessagingErrorCode() == MessagingErrorCode.INVALID_ARGUMENT) {
                deviceTokenRepo.deleteByFcmToken(token);
                System.out.println("🗑️ Removed stale token");
            }
        }
    }
}