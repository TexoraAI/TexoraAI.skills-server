//package com.lms.notification.consumer;
//
//import com.fasterxml.jackson.databind.ObjectMapper;
//import com.lms.notification.dto.NotificationDTO;
//import com.lms.notification.repository.StudentBatchMapRepository;
//import com.lms.notification.service.NotificationService;
//
//import org.springframework.kafka.annotation.KafkaListener;
//import org.springframework.stereotype.Component;
//
//import java.util.List;
//import java.util.Map;
//
//@Component
//public class FeedbackEventConsumer {
//
//    private final NotificationService notificationService;
//    private final StudentBatchMapRepository studentBatchMapRepository;
//    private final ObjectMapper mapper = new ObjectMapper();
//
//    public FeedbackEventConsumer(NotificationService notificationService,
//                                 StudentBatchMapRepository studentBatchMapRepository) {
//        this.notificationService = notificationService;
//        this.studentBatchMapRepository = studentBatchMapRepository;
//    }
//
//    // =====================================================
//    // 🔥 FEEDBACK SUBMITTED EVENT CONSUMER
//    // =====================================================
//    @KafkaListener(topics = "feedback.submitted", groupId = "notification-service-group")
//    public void onFeedbackSubmitted(String message) {
//
//        try {
//            // ✅ Convert JSON String → Map
//            Map<String, Object> event = mapper.readValue(message, Map.class);
//            Map<String, Object> payload = (Map<String, Object>) event.get("payload");
//
//            // Extract payload data
//            Long feedbackId = Long.valueOf(payload.get("feedbackId").toString());
//            Long batchId = Long.valueOf(payload.get("batchId").toString());
//            String trainerEmail = (String) payload.get("trainerEmail");
//            String studentEmail = (String) payload.get("studentEmail");
//            boolean isAnonymous = (boolean) payload.get("anonymous");
//            String moodRating = (String) payload.get("moodRating");
//
//            // =====================================================
//            // 📢 NOTIFY TRAINER
//            // =====================================================
//            NotificationDTO trainerNotif = new NotificationDTO();
//            trainerNotif.setType("FEEDBACK_SUBMITTED");
//            trainerNotif.setTitle("New Feedback Received");
//            
//            if (isAnonymous) {
//                trainerNotif.setMessage("You received anonymous feedback for batch " + batchId + " (Mood: " + moodRating + ")");
//            } else {
//                trainerNotif.setMessage("You received feedback from " + studentEmail + " for batch " + batchId + " (Mood: " + moodRating + ")");
//            }
//            
//            trainerNotif.setTargetUserIds(List.of(trainerEmail));
//            notificationService.createAndPush(trainerNotif);
//
//            System.out.println("✅ FEEDBACK notification sent to Trainer: " + trainerEmail);
//
//            // =====================================================
//            // 📢 NOTIFY ADMIN (broadcast to ADMIN role)
//            // =====================================================
//            NotificationDTO adminNotif = new NotificationDTO();
//            adminNotif.setType("FEEDBACK_SUBMITTED");
//            adminNotif.setTitle("New Feedback - Batch " + batchId);
//            
//            if (isAnonymous) {
//                adminNotif.setMessage("Anonymous feedback submitted (ID: " + feedbackId + ", Mood: " + moodRating + ")");
//            } else {
//                adminNotif.setMessage("Feedback from " + studentEmail + " submitted (ID: " + feedbackId + ")");
//            }
//            
//            adminNotif.setTargetRole("ADMIN");
//            notificationService.createAndPush(adminNotif);
//
//            System.out.println("✅ FEEDBACK notification sent to ADMIN role");
//
//        } catch (Exception e) {
//            System.err.println("❌ FeedbackEventConsumer error: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    // =====================================================
//    // 🔥 FEEDBACK SUMMARY UPDATED EVENT CONSUMER
//    // =====================================================
//    @KafkaListener(topics = "feedback.summary.updated", groupId = "notification-service-group")
//    public void onFeedbackSummaryUpdated(String message) {
//
//        try {
//            // ✅ Convert JSON String → Map
//            Map<String, Object> event = mapper.readValue(message, Map.class);
//            Map<String, Object> payload = (Map<String, Object>) event.get("payload");
//
//            String trainerEmail = (String) payload.get("trainerEmail");
//            Long batchId = Long.valueOf(payload.get("batchId").toString());
//
//            // =====================================================
//            // 📢 NOTIFY TRAINER - Summary Updated
//            // =====================================================
//            NotificationDTO trainerNotif = new NotificationDTO();
//            trainerNotif.setType("FEEDBACK_SUMMARY_UPDATED");
//            trainerNotif.setTitle("Your Feedback Summary Updated");
//            trainerNotif.setMessage("Your feedback metrics for batch " + batchId + " have been updated. Check your dashboard for detailed insights.");
//            trainerNotif.setTargetUserIds(List.of(trainerEmail));
//            
//            notificationService.createAndPush(trainerNotif);
//
//            System.out.println("✅ SUMMARY notification sent to Trainer: " + trainerEmail);
//
//            // =====================================================
//            // 📢 NOTIFY ADMIN - Summary Updated
//            // =====================================================
//            NotificationDTO adminNotif = new NotificationDTO();
//            adminNotif.setType("FEEDBACK_SUMMARY_UPDATED");
//            adminNotif.setTitle("Feedback Summary - Batch " + batchId);
//            adminNotif.setMessage("Trainer " + trainerEmail + " has an updated feedback summary for batch " + batchId);
//            adminNotif.setTargetRole("ADMIN");
//            
//            notificationService.createAndPush(adminNotif);
//
//            System.out.println("✅ SUMMARY notification sent to ADMIN role");
//
//        } catch (Exception e) {
//            System.err.println("❌ FeedbackEventConsumer (Summary) error: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//}wroking belwo added the alerts alos 


package com.lms.notification.consumer;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lms.notification.dto.NotificationDTO;
import com.lms.notification.repository.StudentBatchMapRepository;
import com.lms.notification.service.NotificationService;

import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Map;

@Component
public class FeedbackEventConsumer {

    private final NotificationService notificationService;
    private final StudentBatchMapRepository studentBatchMapRepository;
    private final ObjectMapper mapper = new ObjectMapper();

    public FeedbackEventConsumer(NotificationService notificationService,
                                 StudentBatchMapRepository studentBatchMapRepository) {
        this.notificationService = notificationService;
        this.studentBatchMapRepository = studentBatchMapRepository;
    }

    // =====================================================
    // 🔥 FEEDBACK SUBMITTED EVENT CONSUMER
    // =====================================================
    @KafkaListener(topics = "feedback.submitted", groupId = "notification-service-group")
    public void onFeedbackSubmitted(String message) {

        try {
            Map<String, Object> event = mapper.readValue(message, Map.class);
            String eventType = (String) event.get("type");
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");

            // ✅ Route CONFIGURED_ALERT type separately
            if ("CONFIGURED_ALERT".equals(eventType)) {
                handleConfiguredAlert(payload);
                return;
            }

            // =====================================================
            // existing FEEDBACK_SUBMITTED logic — untouched
            // =====================================================
            Long feedbackId = Long.valueOf(payload.get("feedbackId").toString());
            Long batchId = Long.valueOf(payload.get("batchId").toString());
            String trainerEmail = (String) payload.get("trainerEmail");
            String studentEmail = (String) payload.get("studentEmail");
            boolean isAnonymous = (boolean) payload.get("anonymous");
            String moodRating = (String) payload.get("moodRating");

            NotificationDTO trainerNotif = new NotificationDTO();
            trainerNotif.setType("FEEDBACK_SUBMITTED");
            trainerNotif.setTitle("New Feedback Received");

            if (isAnonymous) {
                trainerNotif.setMessage("You received anonymous feedback for batch " + batchId + " (Mood: " + moodRating + ")");
            } else {
                trainerNotif.setMessage("You received feedback from " + studentEmail + " for batch " + batchId + " (Mood: " + moodRating + ")");
            }

            trainerNotif.setTargetUserIds(List.of(trainerEmail));
            notificationService.createAndPush(trainerNotif);

            System.out.println("✅ FEEDBACK notification sent to Trainer: " + trainerEmail);

            NotificationDTO adminNotif = new NotificationDTO();
            adminNotif.setType("FEEDBACK_SUBMITTED");
            adminNotif.setTitle("New Feedback - Batch " + batchId);

            if (isAnonymous) {
                adminNotif.setMessage("Anonymous feedback submitted (ID: " + feedbackId + ", Mood: " + moodRating + ")");
            } else {
                adminNotif.setMessage("Feedback from " + studentEmail + " submitted (ID: " + feedbackId + ")");
            }

            adminNotif.setTargetRole("ADMIN");
            notificationService.createAndPush(adminNotif);

            System.out.println("✅ FEEDBACK notification sent to ADMIN role");

        } catch (Exception e) {
            System.err.println("❌ FeedbackEventConsumer error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =====================================================
    // 🔥 FEEDBACK SUMMARY UPDATED EVENT CONSUMER
    // =====================================================
    @KafkaListener(topics = "feedback.summary.updated", groupId = "notification-service-group")
    public void onFeedbackSummaryUpdated(String message) {

        try {
            Map<String, Object> event = mapper.readValue(message, Map.class);
            Map<String, Object> payload = (Map<String, Object>) event.get("payload");

            String trainerEmail = (String) payload.get("trainerEmail");
            Long batchId = Long.valueOf(payload.get("batchId").toString());

            NotificationDTO trainerNotif = new NotificationDTO();
            trainerNotif.setType("FEEDBACK_SUMMARY_UPDATED");
            trainerNotif.setTitle("Your Feedback Summary Updated");
            trainerNotif.setMessage("Your feedback metrics for batch " + batchId + " have been updated. Check your dashboard for detailed insights.");
            trainerNotif.setTargetUserIds(List.of(trainerEmail));

            notificationService.createAndPush(trainerNotif);

            System.out.println("✅ SUMMARY notification sent to Trainer: " + trainerEmail);

            NotificationDTO adminNotif = new NotificationDTO();
            adminNotif.setType("FEEDBACK_SUMMARY_UPDATED");
            adminNotif.setTitle("Feedback Summary - Batch " + batchId);
            adminNotif.setMessage("Trainer " + trainerEmail + " has an updated feedback summary for batch " + batchId);
            adminNotif.setTargetRole("ADMIN");

            notificationService.createAndPush(adminNotif);

            System.out.println("✅ SUMMARY notification sent to ADMIN role");

        } catch (Exception e) {
            System.err.println("❌ FeedbackEventConsumer (Summary) error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // =====================================================
//    // 🔔 NEW — ALERT CONFIG UPDATED EVENT CONSUMER
//    // =====================================================
//    @KafkaListener(topics = "alert.config.updated", groupId = "notification-service-group")
//    public void onAlertConfigUpdated(String message) {
//
//        try {
//            Map<String, Object> event = mapper.readValue(message, Map.class);
//            Map<String, Object> payload = (Map<String, Object>) event.get("payload");
//
//            Long batchId = Long.valueOf(payload.get("batchId").toString());
//            String trainerEmail = (String) payload.get("trainerEmail");
//            boolean sendToTrainer = (boolean) payload.get("sendToTrainer");
//            boolean sendToAdmin   = (boolean) payload.get("sendToAdmin");
//
//            // Notify trainer if configured
//            if (sendToTrainer) {
//                NotificationDTO trainerNotif = new NotificationDTO();
//                trainerNotif.setType("ALERT_CONFIG_UPDATED");
//                trainerNotif.setTitle("Alert Configuration Updated");
//                trainerNotif.setMessage("Alert settings for batch " + batchId + " have been updated.");
//                trainerNotif.setTargetUserIds(List.of(trainerEmail));
//                notificationService.createAndPush(trainerNotif);
//                System.out.println("✅ ALERT CONFIG notification sent to Trainer: " + trainerEmail);
//            }
//
//            // Notify admin if configured
//            if (sendToAdmin) {
//                NotificationDTO adminNotif = new NotificationDTO();
//                adminNotif.setType("ALERT_CONFIG_UPDATED");
//                adminNotif.setTitle("Alert Config - Batch " + batchId);
//                adminNotif.setMessage("Alert configuration updated by trainer " + trainerEmail + " for batch " + batchId);
//                adminNotif.setTargetRole("ADMIN");
//                notificationService.createAndPush(adminNotif);
//                System.out.println("✅ ALERT CONFIG notification sent to ADMIN role");
//            }
//
//        } catch (Exception e) {
//            System.err.println("❌ FeedbackEventConsumer (AlertConfig) error: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
//
//    // =====================================================
//    // 🔔 NEW — CONFIGURED ALERT handler (routed from feedback.submitted)
//    // =====================================================
//    private void handleConfiguredAlert(Map<String, Object> payload) {
//
//        try {
//            String title      = (String) payload.get("title");
//            String message    = (String) payload.get("message");
//            String targetRole = (String) payload.get("targetRole");
//            List<String> targetUserIds = (List<String>) payload.get("targetUserIds");
//
//            NotificationDTO notif = new NotificationDTO();
//            notif.setType("CONFIGURED_ALERT");
//            notif.setTitle(title);
//            notif.setMessage(message);
//
//            // Role-based broadcast OR specific user list
//            if (targetRole != null && !targetRole.isBlank()) {
//                notif.setTargetRole(targetRole);
//                System.out.println("✅ CONFIGURED ALERT sent to role: " + targetRole);
//            } else if (targetUserIds != null && !targetUserIds.isEmpty()) {
//                notif.setTargetUserIds(targetUserIds);
//                System.out.println("✅ CONFIGURED ALERT sent to users: " + targetUserIds);
//            }
//
//            notificationService.createAndPush(notif);
//
//        } catch (Exception e) {
//            System.err.println("❌ FeedbackEventConsumer (ConfiguredAlert) error: " + e.getMessage());
//            e.printStackTrace();
//        }
//    }
 // =====================================================
 // 🔔 ALERT CONFIG UPDATED EVENT CONSUMER
 // =====================================================
 @KafkaListener(topics = "alert.config.updated", groupId = "notification-service-group")
 public void onAlertConfigUpdated(String message) {
     try {
         Map<String, Object> event = mapper.readValue(message, Map.class);
         Map<String, Object> payload = (Map<String, Object>) event.get("payload");

         Long batchId         = Long.valueOf(payload.get("batchId").toString());
         String trainerEmail  = (String) payload.get("trainerEmail");
         boolean sendToTrainer = (boolean) payload.get("sendToTrainer");
         boolean sendToStudent = (boolean) payload.get("sendToStudent");
         boolean sendToAdmin   = (boolean) payload.get("sendToAdmin");

         // ✅ NEW: Read custom messages from payload (with sensible fallbacks)
         String trainerMessage = payload.getOrDefault("trainerMessage",
             "Dear Trainer, your recent session ratings have dropped. Please improve your performance.").toString();
         String studentMessage = payload.getOrDefault("studentMessage",
             "Dear Student, we have received your feedback and are working to resolve your concerns.").toString();
         String adminMessage   = payload.getOrDefault("adminMessage",
             "Alert: Batch " + batchId + " feedback ratings require your attention.").toString();

         // ✅ Notify trainer with trainer-specific message
         if (sendToTrainer) {
             NotificationDTO trainerNotif = new NotificationDTO();
             trainerNotif.setType("ALERT_CONFIG_UPDATED");
             trainerNotif.setTitle("⚠️ Performance Alert — Batch " + batchId);
             trainerNotif.setMessage(trainerMessage);   // ✅ Custom message
             trainerNotif.setTargetUserIds(List.of(trainerEmail));
             notificationService.createAndPush(trainerNotif);
             System.out.println("✅ ALERT CONFIG notification sent to Trainer: " + trainerEmail);
         }

         // ✅ Notify students with student-specific message
         if (sendToStudent) {
             NotificationDTO studentNotif = new NotificationDTO();
             studentNotif.setType("ALERT_CONFIG_UPDATED");
             studentNotif.setTitle("📢 Update from Your Batch — Batch " + batchId);
             studentNotif.setMessage(studentMessage);   // ✅ Custom message
             studentNotif.setTargetRole("STUDENT");     // broadcasts to all students in batch
             notificationService.createAndPush(studentNotif);
             System.out.println("✅ ALERT CONFIG notification sent to STUDENT role");
         }

         // ✅ Notify admin with admin-specific message
         if (sendToAdmin) {
             NotificationDTO adminNotif = new NotificationDTO();
             adminNotif.setType("ALERT_CONFIG_UPDATED");
             adminNotif.setTitle("🔔 Alert Config — Batch " + batchId);
             adminNotif.setMessage(adminMessage);       // ✅ Custom message
             adminNotif.setTargetRole("ADMIN");
             notificationService.createAndPush(adminNotif);
             System.out.println("✅ ALERT CONFIG notification sent to ADMIN role");
         }

     } catch (Exception e) {
         System.err.println("❌ FeedbackEventConsumer (AlertConfig) error: " + e.getMessage());
         e.printStackTrace();
     }
 }

 // =====================================================
 // 🔔 CONFIGURED ALERT handler (routed from feedback.submitted)
 // =====================================================
 private void handleConfiguredAlert(Map<String, Object> payload) {
     try {
         String title           = (String) payload.get("title");
         String message         = (String) payload.get("message");
         String targetRole      = (String) payload.get("targetRole");
         List<String> targetUserIds = (List<String>) payload.get("targetUserIds");

         NotificationDTO notif = new NotificationDTO();
         notif.setType("CONFIGURED_ALERT");
         notif.setTitle(title);
         notif.setMessage(message);   // ✅ Already carries the right message from producer

         if (targetRole != null && !targetRole.isBlank()) {
             notif.setTargetRole(targetRole);
             System.out.println("✅ CONFIGURED ALERT sent to role: " + targetRole);
         } else if (targetUserIds != null && !targetUserIds.isEmpty()) {
             notif.setTargetUserIds(targetUserIds);
             System.out.println("✅ CONFIGURED ALERT sent to users: " + targetUserIds);
         }

         notificationService.createAndPush(notif);

     } catch (Exception e) {
         System.err.println("❌ FeedbackEventConsumer (ConfiguredAlert) error: " + e.getMessage());
         e.printStackTrace();
     }
 }
}