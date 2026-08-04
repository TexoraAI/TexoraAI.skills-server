package com.lms.live_session.controller;

import com.lms.live_session.entity.AiWorkflowTask;
import com.lms.live_session.repository.AiWorkflowTaskRepository;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.security.Principal;
import java.util.List;

/**
 * Read-only access to follow-up tasks created by the ai-workflow engine's
 * ac4 action node. No create/update/delete endpoints — tasks are only
 * ever created by AiWorkflowExecutionService itself.
 * Base path: /api/v1/ai-companion/workflow-tasks
 */
@RestController
@RequestMapping("/api/v1/ai-companion/workflow-tasks")
public class AiWorkflowTaskController {

    private final AiWorkflowTaskRepository taskRepository;

    public AiWorkflowTaskController(AiWorkflowTaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    /**
     * GET /api/v1/ai-companion/workflow-tasks
     * All follow-up tasks for the current trainer, newest first.
     */
    @GetMapping
    public ResponseEntity<List<AiWorkflowTask>> getMyTasks(Principal principal) {
        String email = principal != null ? principal.getName() : "unknown";
        return ResponseEntity.ok(taskRepository.findByTrainerEmailOrderByCreatedAtDesc(email));
    }

    /**
     * GET /api/v1/ai-companion/workflow-tasks/session/{sessionId}
     * Follow-up tasks created for a specific live session.
     */
    @GetMapping("/session/{sessionId}")
    public ResponseEntity<List<AiWorkflowTask>> getTasksForSession(@PathVariable Long sessionId) {
        return ResponseEntity.ok(taskRepository.findBySessionIdOrderByCreatedAtDesc(sessionId));
    }
}