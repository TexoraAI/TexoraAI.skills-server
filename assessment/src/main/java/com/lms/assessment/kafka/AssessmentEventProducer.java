package com.lms.assessment.kafka;

import com.lms.assessment.dto.AssessmentEvent;
import com.lms.assessment.model.CodeSubmission.ExecutionStatus;

import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;
import java.util.HashMap;
import java.util.Map;

@Service
public class AssessmentEventProducer {

    // ✅ Object not String — same as batch producer
    private final KafkaTemplate<String, Object> kafkaTemplate;

    private static final String TOPIC = "assessment-events";

    public AssessmentEventProducer(KafkaTemplate<String, Object> kafkaTemplate) {
        this.kafkaTemplate = kafkaTemplate;
    }

    // ✅ send AssessmentEvent object directly — no ObjectMapper
    public void sendEvent(AssessmentEvent event) {
        kafkaTemplate.send(TOPIC, event);
        System.out.println("✔ Sent Assessment Kafka Event: " + event);
    }

    // ✅ ASSIGNMENT_CREATED — send Map directly
    public void publishAssignmentCreated(Long assignmentId,
                                          String title,
                                          Long batchId,
                                          String trainerEmail) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("assignmentId", assignmentId);
        payload.put("title",        title);
        payload.put("batchId",      batchId);
        payload.put("trainerEmail", trainerEmail);

        Map<String, Object> event = new HashMap<>();
        event.put("type",    "ASSIGNMENT_CREATED");
        event.put("payload", payload);

        kafkaTemplate.send(TOPIC, event); // ✅ send object directly
        System.out.println("✔ ASSIGNMENT_CREATED sent → batchId=" + batchId);
    }

    // ✅ QUIZ_CREATED — send Map directly
    public void publishQuizCreated(Long quizId,
                                    String title,
                                    Long batchId,
                                    String trainerEmail) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("quizId",       quizId);
        payload.put("title",        title);
        payload.put("batchId",      batchId);
        payload.put("trainerEmail", trainerEmail);

        Map<String, Object> event = new HashMap<>();
        event.put("type",    "QUIZ_CREATED");
        event.put("payload", payload);

        kafkaTemplate.send(TOPIC, event); // ✅ send object directly
        System.out.println("✔ QUIZ_CREATED sent → batchId=" + batchId);
    }
//    // ✅ NEW — CODING_PROBLEM_ASSIGNED → students get notified
//    public void publishCodingProblemAssigned(Long problemId,
//                                              String title,
//                                              String batchId,
//                                              String trainerEmail) {
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("problemId",    problemId);
//        payload.put("title",        title);
//        payload.put("batchId",      batchId);
//        payload.put("trainerEmail", trainerEmail);
// 
//        Map<String, Object> event = new HashMap<>();
//        event.put("type",    "CODING_PROBLEM_ASSIGNED");
//        event.put("payload", payload);
// 
//        kafkaTemplate.send(TOPIC, event);
//        System.out.println("✔ CODING_PROBLEM_ASSIGNED sent → batchId=" + batchId);
//    }
// 
//    // ✅ NEW — CODE_SUBMITTED → trainer gets notified
//    public void publishCodeSubmitted(Long submissionId,
//                                      String studentEmail,
//                                      String batchId,
//                                      ExecutionStatus executionStatus) {
//        Map<String, Object> payload = new HashMap<>();
//        payload.put("submissionId",  submissionId);
//        payload.put("studentEmail",  studentEmail);
//        payload.put("batchId",       batchId);
//        payload.put("status",        executionStatus);
// 
//        Map<String, Object> event = new HashMap<>();
//        event.put("type",    "CODE_SUBMITTED");
//        event.put("payload", payload);
// 
//        kafkaTemplate.send(TOPIC, event);
//        System.out.println("✔ CODE_SUBMITTED sent → student=" + studentEmail + " batch=" + batchId);
//    }
    public void publishCodingProblemAssigned(Long problemId, String title, String batchId, String trainerEmail) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("problemId",    problemId);
        payload.put("title",        title);
        payload.put("batchId",      batchId);
        payload.put("trainerEmail", trainerEmail);
 
        Map<String, Object> event = new HashMap<>();
        event.put("type",    "CODING_PROBLEM_ASSIGNED");
        event.put("payload", payload);
 
        kafkaTemplate.send(TOPIC, event);
        System.out.println("✔ CODING_PROBLEM_ASSIGNED sent → batchId=" + batchId);
    }
 
    // ✅ FIX: .name() converts enum → plain String so consumer can cast it correctly
    public void publishCodeSubmitted(Long submissionId, String studentEmail, String batchId, ExecutionStatus executionStatus) {
        Map<String, Object> payload = new HashMap<>();
        payload.put("submissionId", submissionId);
        payload.put("studentEmail", studentEmail);
        payload.put("batchId",      batchId);
        payload.put("status",       executionStatus.name()); // ✅ "SUCCESS" not ExecutionStatus{...}
 
        Map<String, Object> event = new HashMap<>();
        event.put("type",    "CODE_SUBMITTED");
        event.put("payload", payload);
 
        kafkaTemplate.send(TOPIC, event);
        System.out.println("✔ CODE_SUBMITTED sent → student=" + studentEmail + " batch=" + batchId);
    }
    
}