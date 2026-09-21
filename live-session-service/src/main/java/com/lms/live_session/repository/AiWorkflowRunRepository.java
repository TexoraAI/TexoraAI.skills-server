package com.lms.live_session.repository;

import com.lms.live_session.entity.AiWorkflowRun;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface AiWorkflowRunRepository extends JpaRepository<AiWorkflowRun, Long> {
    List<AiWorkflowRun> findByWorkflowIdOrderByCreatedAtDesc(Long workflowId);
    List<AiWorkflowRun> findByTriggeredByOrderByCreatedAtDesc(String triggeredBy);
    List<AiWorkflowRun> findAllByOrderByCreatedAtDesc();

    // c2: runs whose delay has elapsed and are ready to resume.
    List<AiWorkflowRun> findByStatusAndResumeAtBefore(String status, LocalDateTime cutoff);
}