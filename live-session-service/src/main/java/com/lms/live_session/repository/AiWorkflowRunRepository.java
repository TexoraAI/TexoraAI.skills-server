 package com.lms.live_session.repository;
 import com.lms.live_session.entity.AiWorkflowRun;
 import org.springframework.data.jpa.repository.JpaRepository;
 import org.springframework.stereotype.Repository;
 import java.util.List;

 @Repository
 public interface AiWorkflowRunRepository extends JpaRepository<AiWorkflowRun, Long> {
     List<AiWorkflowRun> findByWorkflowIdOrderByCreatedAtDesc(Long workflowId);
     List<AiWorkflowRun> findByTriggeredByOrderByCreatedAtDesc(String triggeredBy);
     List<AiWorkflowRun> findAllByOrderByCreatedAtDesc();
 }