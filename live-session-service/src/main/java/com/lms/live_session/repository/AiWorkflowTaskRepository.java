package com.lms.live_session.repository;

import com.lms.live_session.entity.AiWorkflowTask;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface AiWorkflowTaskRepository extends JpaRepository<AiWorkflowTask, Long> {
    List<AiWorkflowTask> findByTrainerEmailOrderByCreatedAtDesc(String trainerEmail);
    List<AiWorkflowTask> findBySessionIdOrderByCreatedAtDesc(Long sessionId);
    List<AiWorkflowTask> findByWorkflowRunIdOrderByCreatedAtDesc(Long workflowRunId);
}