//package com.lms.live_session.repository;
//
//import com.lms.live_session.entity.AiWorkflow;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//
//@Repository
//public interface AiWorkflowRepository extends JpaRepository<AiWorkflow, Long> {
//
//    // All workflows for a trainer, newest first
//    List<AiWorkflow> findByTrainerEmailOrderByUpdatedAtDesc(String trainerEmail);
//
//    // Filter by status only
//    List<AiWorkflow> findByTrainerEmailAndStatusOrderByUpdatedAtDesc(
//            String trainerEmail, String status);
//
//    // Filter by name search only
//    List<AiWorkflow> findByTrainerEmailAndNameContainingIgnoreCaseOrderByUpdatedAtDesc(
//            String trainerEmail, String name);
//
//    // Filter by both status and name search
//    List<AiWorkflow> findByTrainerEmailAndStatusAndNameContainingIgnoreCaseOrderByUpdatedAtDesc(
//            String trainerEmail, String status, String name);
//}


package com.lms.live_session.repository;
import com.lms.live_session.entity.AiWorkflow;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
@Repository
public interface AiWorkflowRepository extends JpaRepository<AiWorkflow, Long> {
    // All workflows for a trainer, newest first
    List<AiWorkflow> findByTrainerEmailOrderByUpdatedAtDesc(String trainerEmail);
    // Filter by status only
    List<AiWorkflow> findByTrainerEmailAndStatusOrderByUpdatedAtDesc(
            String trainerEmail, String status);
    // Filter by name search only
    List<AiWorkflow> findByTrainerEmailAndNameContainingIgnoreCaseOrderByUpdatedAtDesc(
            String trainerEmail, String name);
    // Filter by both status and name search
    List<AiWorkflow> findByTrainerEmailAndStatusAndNameContainingIgnoreCaseOrderByUpdatedAtDesc(
            String trainerEmail, String status, String name);
    // NEW — used by AiWorkflowExecutionService.fireTrigger() to find ACTIVE
    // workflows owned by a given trainer whose triggerType matches the
    // label of the session/transcript event that just occurred.
    List<AiWorkflow> findByStatusAndTriggerTypeAndTrainerEmail(
            String status, String triggerType, String trainerEmail);
}