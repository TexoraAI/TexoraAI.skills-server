 

package com.lms.chat.kafka;

import com.lms.chat.entity.Feedback;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class FeedbackEventProducer {

    public static final String TOPIC_FEEDBACK_SUBMITTED = "feedback.submitted";
    public static final String TOPIC_FEEDBACK_SUMMARY_UPDATED = "feedback.summary.updated";
    public static final String TOPIC_ALERT_CONFIG_UPDATED = "alert.config.updated";

    // ✅ Same pattern as ChatProducer
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public FeedbackEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // =====================================================
    // 🔥 FEEDBACK SUBMITTED EVENT
    // =====================================================
    public void publishFeedbackSubmitted(Feedback f) {

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("feedbackId", f.getId());
            payload.put("batchId", f.getBatchId());
            payload.put("studentEmail", f.isAnonymous() ? null : f.getStudentEmail());
            payload.put("trainerEmail", f.getTrainerEmail());

            payload.put("moodRating", f.getMoodRating());
            payload.put("clarity", f.getTrainerClarityRating());
            payload.put("doubt", f.getTrainerDoubtClearingRating());
            payload.put("energy", f.getTrainerEnergyRating());
            payload.put("depth", f.getTrainerTechnicalDepthRating());

            payload.put("anonymous", f.isAnonymous());
            // ✅ Convert LocalDateTime to String (ISO format) instead of sending the object
            payload.put("submittedAt", f.getCreatedAt() != null ? f.getCreatedAt().toString() : null);

            Map<String, Object> event = new HashMap<>();
            event.put("type", "FEEDBACK_SUBMITTED");
            event.put("payload", payload);

            // ✅ Send object directly - same as ChatEventProducer
            kafkaTemplate.send(TOPIC_FEEDBACK_SUBMITTED, event);

            // ✅ Debug log
            System.out.println("🔥 FEEDBACK EVENT SENT");
            System.out.println("   Topic       : " + TOPIC_FEEDBACK_SUBMITTED);
            System.out.println("   Trainer     : " + f.getTrainerEmail());
            System.out.println("   Batch       : " + f.getBatchId());
            System.out.println("   Feedback ID : " + f.getId());

        } catch (Exception e) {
            System.out.println("❌ ERROR sending FEEDBACK_SUBMITTED event");
            e.printStackTrace();
        }
    }

    // =====================================================
    // 🔥 SUMMARY UPDATED EVENT
    // =====================================================
    public void publishSummaryUpdated(String trainerEmail, Long batchId) {

        try {
            Map<String, Object> payload = new HashMap<>();
            payload.put("trainerEmail", trainerEmail);
            payload.put("batchId", batchId);

            Map<String, Object> event = new HashMap<>();
            event.put("type", "FEEDBACK_SUMMARY_UPDATED");
            event.put("payload", payload);

            // ✅ Send object directly - same as ChatEventProducer
            kafkaTemplate.send(TOPIC_FEEDBACK_SUMMARY_UPDATED, event);

            // ✅ Debug log
            System.out.println("🔥 SUMMARY EVENT SENT");
            System.out.println("   Topic   : " + TOPIC_FEEDBACK_SUMMARY_UPDATED);
            System.out.println("   Trainer : " + trainerEmail);
            System.out.println("   Batch   : " + batchId);

        } catch (Exception e) {
            System.out.println("❌ ERROR sending FEEDBACK_SUMMARY_UPDATED event");
            e.printStackTrace();
        }
    }
    
    
 // ✅ ADD these 3 methods to your existing FeedbackEventProducer class


//    // =====================================================
//    // 🔥 ALERT CONFIG CREATED/UPDATED EVENT
//    // =====================================================
//    public void publishAlertConfigUpdated(Long batchId, String trainerEmail, 
//                                           boolean sendToTrainer, 
//                                           boolean sendToStudent,
//                                           boolean sendToAdmin, 
//                                           boolean alertLowRatings,
//                                           Double lowRatingThreshold) {
//        try {
//            Map<String, Object> payload = new HashMap<>();
//            payload.put("batchId", batchId);
//            payload.put("trainerEmail", trainerEmail);
//            payload.put("sendToTrainer", sendToTrainer);
//            payload.put("sendToStudent", sendToStudent);
//            payload.put("sendToAdmin", sendToAdmin);
//            payload.put("alertLowRatings", alertLowRatings);
//            payload.put("lowRatingThreshold", lowRatingThreshold);
//
//            Map<String, Object> event = new HashMap<>();
//            event.put("type", "ALERT_CONFIG_UPDATED");
//            event.put("payload", payload);
//
//            kafkaTemplate.send(TOPIC_ALERT_CONFIG_UPDATED, event);
//
//            System.out.println("🔔 ALERT CONFIG EVENT SENT");
//            System.out.println("   Topic       : " + TOPIC_ALERT_CONFIG_UPDATED);
//            System.out.println("   Batch       : " + batchId);
//            System.out.println("   Trainer     : " + trainerEmail);
//            System.out.println("   sendToTrainer: " + sendToTrainer);
//            System.out.println("   sendToStudent: " + sendToStudent);
//            System.out.println("   sendToAdmin  : " + sendToAdmin);
//
//        } catch (Exception e) {
//            System.out.println("❌ ERROR sending ALERT_CONFIG_UPDATED event");
//            e.printStackTrace();
//        }
//    }
//
//    // =====================================================
//    // 🔔 SEND ALERT NOTIFICATION (Admin configures and sends)
//    // =====================================================
//    public void sendConfiguredAlert(String title, String message, 
//                                     String targetRole, java.util.List<String> targetUserIds) {
//        try {
//            Map<String, Object> payload = new HashMap<>();
//            payload.put("title", title);
//            payload.put("message", message);
//            payload.put("targetRole", targetRole);
//            payload.put("targetUserIds", targetUserIds);
//
//            Map<String, Object> event = new HashMap<>();
//            event.put("type", "CONFIGURED_ALERT");
//            event.put("payload", payload);
//
//            kafkaTemplate.send("feedback.submitted", event);
//
//            System.out.println("📢 CONFIGURED ALERT SENT");
//            System.out.println("   Title : " + title);
//            System.out.println("   Role  : " + targetRole);
//
//        } catch (Exception e) {
//            System.out.println("❌ ERROR sending CONFIGURED_ALERT");
//            e.printStackTrace();
//        }
//    }
 // =====================================================
 // 🔔 ALERT CONFIG CREATED/UPDATED EVENT
 // =====================================================
 public void publishAlertConfigUpdated(Long batchId, String trainerEmail,
                                        boolean sendToTrainer,
                                        boolean sendToStudent,
                                        boolean sendToAdmin,
                                        boolean alertLowRatings,
                                        Double lowRatingThreshold,
                                        String trainerMessage,   // ✅ NEW
                                        String studentMessage,   // ✅ NEW
                                        String adminMessage) {   // ✅ NEW
     try {
         Map<String, Object> payload = new HashMap<>();
         payload.put("batchId", batchId);
         payload.put("trainerEmail", trainerEmail);
         payload.put("sendToTrainer", sendToTrainer);
         payload.put("sendToStudent", sendToStudent);
         payload.put("sendToAdmin", sendToAdmin);
         payload.put("alertLowRatings", alertLowRatings);
         payload.put("lowRatingThreshold", lowRatingThreshold);
         // ✅ NEW: Custom messages per recipient
         payload.put("trainerMessage", trainerMessage != null ? trainerMessage :
             "Dear Trainer, your recent session ratings have dropped. Please improve your performance.");
         payload.put("studentMessage", studentMessage != null ? studentMessage :
             "Dear Student, we have received your feedback and are working to resolve your concerns.");
         payload.put("adminMessage", adminMessage != null ? adminMessage :
             "Alert: Batch feedback ratings require your attention.");

         Map<String, Object> event = new HashMap<>();
         event.put("type", "ALERT_CONFIG_UPDATED");
         event.put("payload", payload);

         kafkaTemplate.send(TOPIC_ALERT_CONFIG_UPDATED, event);

         System.out.println("🔔 ALERT CONFIG EVENT SENT");
         System.out.println("   Topic        : " + TOPIC_ALERT_CONFIG_UPDATED);
         System.out.println("   Batch        : " + batchId);
         System.out.println("   Trainer      : " + trainerEmail);
         System.out.println("   sendToTrainer: " + sendToTrainer);
         System.out.println("   sendToStudent: " + sendToStudent);
         System.out.println("   sendToAdmin  : " + sendToAdmin);

     } catch (Exception e) {
         System.out.println("❌ ERROR sending ALERT_CONFIG_UPDATED event");
         e.printStackTrace();
     }
 }

 // =====================================================
 // 🔔 SEND ALERT NOTIFICATION (Admin configures and sends)
 // =====================================================
 public void sendConfiguredAlert(String title, String message,
                                  String targetRole,
                                  java.util.List<String> targetUserIds) {
     try {
         Map<String, Object> payload = new HashMap<>();
         payload.put("title", title);
         payload.put("message", message);
         payload.put("targetRole", targetRole);
         payload.put("targetUserIds", targetUserIds);

         Map<String, Object> event = new HashMap<>();
         event.put("type", "CONFIGURED_ALERT");
         event.put("payload", payload);

         kafkaTemplate.send("feedback.submitted", event);

         System.out.println("📢 CONFIGURED ALERT SENT");
         System.out.println("   Title : " + title);
         System.out.println("   Role  : " + targetRole);

     } catch (Exception e) {
         System.out.println("❌ ERROR sending CONFIGURED_ALERT");
         e.printStackTrace();
     }
 }
} 
//fully working man but aded belwo ialertconfig just 




