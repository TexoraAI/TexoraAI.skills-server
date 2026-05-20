package com.lms.live_session.controller;

import com.lms.live_session.dto.AiWorkflowRequest;
import com.lms.live_session.dto.AiWorkflowResponse;
import com.lms.live_session.dto.AiWorkflowStatusRequest;
import com.lms.live_session.service.AiWorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/ai-companion/workflows")
public class AiWorkflowController {

    private final AiWorkflowService workflowService;

    public AiWorkflowController(AiWorkflowService workflowService) {
        this.workflowService = workflowService;
    }

    // POST /api/v1/ai-companion/workflows
    @PostMapping
    public ResponseEntity<AiWorkflowResponse> createWorkflow(
            @RequestBody AiWorkflowRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workflowService.createWorkflow(request));
    }

    // GET /api/v1/ai-companion/workflows?search=&status=
    @GetMapping
    public ResponseEntity<List<AiWorkflowResponse>> getMyWorkflows(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String status) {
        return ResponseEntity.ok(workflowService.getMyWorkflows(search, status));
    }

    // GET /api/v1/ai-companion/workflows/{id}
    @GetMapping("/{id}")
    public ResponseEntity<AiWorkflowResponse> getWorkflow(@PathVariable Long id) {
        return ResponseEntity.ok(workflowService.getWorkflow(id));
    }

    // PUT /api/v1/ai-companion/workflows/{id}
    @PutMapping("/{id}")
    public ResponseEntity<AiWorkflowResponse> updateWorkflow(
            @PathVariable Long id,
            @RequestBody AiWorkflowRequest request) {
        return ResponseEntity.ok(workflowService.updateWorkflow(id, request));
    }

    // DELETE /api/v1/ai-companion/workflows/{id}
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteWorkflow(@PathVariable Long id) {
        workflowService.deleteWorkflow(id);
        return ResponseEntity.noContent().build();
    }

    // POST /api/v1/ai-companion/workflows/{id}/duplicate
    @PostMapping("/{id}/duplicate")
    public ResponseEntity<AiWorkflowResponse> duplicateWorkflow(@PathVariable Long id) {
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(workflowService.duplicateWorkflow(id));
    }

    // PATCH /api/v1/ai-companion/workflows/{id}/status
    @PatchMapping("/{id}/status")
    public ResponseEntity<AiWorkflowResponse> updateStatus(
            @PathVariable Long id,
            @RequestBody AiWorkflowStatusRequest request) {
        return ResponseEntity.ok(workflowService.updateStatus(id, request));
    }
}