//package com.lms.live_session.controller;
//
//import com.lms.live_session.dto.AiWorkflowRequest;
//import com.lms.live_session.dto.AiWorkflowResponse;
//import com.lms.live_session.dto.AiWorkflowStatusRequest;
//import com.lms.live_session.service.AiWorkflowService;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/v1/ai-companion/workflows")
//public class AiWorkflowController {
//
//    private final AiWorkflowService workflowService;
//
//    public AiWorkflowController(AiWorkflowService workflowService) {
//        this.workflowService = workflowService;
//    }
//
//    // POST /api/v1/ai-companion/workflows
//    @PostMapping
//    public ResponseEntity<AiWorkflowResponse> createWorkflow(
//            @RequestBody AiWorkflowRequest request) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(workflowService.createWorkflow(request));
//    }
//
//    // GET /api/v1/ai-companion/workflows?search=&status=
//    @GetMapping
//    public ResponseEntity<List<AiWorkflowResponse>> getMyWorkflows(
//            @RequestParam(required = false) String search,
//            @RequestParam(required = false) String status) {
//        return ResponseEntity.ok(workflowService.getMyWorkflows(search, status));
//    }
//
//    // GET /api/v1/ai-companion/workflows/{id}
//    @GetMapping("/{id}")
//    public ResponseEntity<AiWorkflowResponse> getWorkflow(@PathVariable Long id) {
//        return ResponseEntity.ok(workflowService.getWorkflow(id));
//    }
//
//    // PUT /api/v1/ai-companion/workflows/{id}
//    @PutMapping("/{id}")
//    public ResponseEntity<AiWorkflowResponse> updateWorkflow(
//            @PathVariable Long id,
//            @RequestBody AiWorkflowRequest request) {
//        return ResponseEntity.ok(workflowService.updateWorkflow(id, request));
//    }
//
//    // DELETE /api/v1/ai-companion/workflows/{id}
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteWorkflow(@PathVariable Long id) {
//        workflowService.deleteWorkflow(id);
//        return ResponseEntity.noContent().build();
//    }
//
//    // POST /api/v1/ai-companion/workflows/{id}/duplicate
//    @PostMapping("/{id}/duplicate")
//    public ResponseEntity<AiWorkflowResponse> duplicateWorkflow(@PathVariable Long id) {
//        return ResponseEntity.status(HttpStatus.CREATED)
//                .body(workflowService.duplicateWorkflow(id));
//    }
//
//    // PATCH /api/v1/ai-companion/workflows/{id}/status
//    @PatchMapping("/{id}/status")
//    public ResponseEntity<AiWorkflowResponse> updateStatus(
//            @PathVariable Long id,
//            @RequestBody AiWorkflowStatusRequest request) {
//        return ResponseEntity.ok(workflowService.updateStatus(id, request));
//    }
//}
//
//
package com.lms.live_session.controller;

import com.lms.live_session.dto.AiWorkflowRequest;
import com.lms.live_session.dto.AiWorkflowResponse;
import com.lms.live_session.dto.AiWorkflowRunRequest;
import com.lms.live_session.dto.AiWorkflowRunResponse;
import com.lms.live_session.dto.AiWorkflowStatusRequest;
import com.lms.live_session.entity.AiWorkflowRun;
import com.lms.live_session.repository.AiWorkflowRunRepository;
import com.lms.live_session.service.AiWorkflowExecutionService;
import com.lms.live_session.service.AiWorkflowService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/v1/ai-companion/workflows")
public class AiWorkflowController {

    private final AiWorkflowService workflowService;
    private final AiWorkflowExecutionService workflowExecutionService;
    private final AiWorkflowRunRepository workflowRunRepository;

    public AiWorkflowController(
            AiWorkflowService workflowService,
            AiWorkflowExecutionService workflowExecutionService,
            AiWorkflowRunRepository workflowRunRepository) {
        this.workflowService = workflowService;
        this.workflowExecutionService = workflowExecutionService;
        this.workflowRunRepository = workflowRunRepository;
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

    // GET /api/v1/ai-companion/workflows/runs
    // NOTE: declared before /{id} so "runs" is not captured as a path variable.
    @GetMapping("/runs")
    public ResponseEntity<List<AiWorkflowRunResponse>> getMyWorkflowRuns(Authentication authentication) {
        String trainerEmail = authentication.getName();
        List<AiWorkflowRun> runs = workflowRunRepository.findByTriggeredByOrderByCreatedAtDesc(trainerEmail);
        return ResponseEntity.ok(runs.stream().map(this::mapRunToResponse).collect(Collectors.toList()));
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

    // POST /api/v1/ai-companion/workflows/{id}/run
    @PostMapping("/{id}/run")
    public ResponseEntity<AiWorkflowRunResponse> runWorkflow(
            @PathVariable Long id,
            @RequestBody(required = false) AiWorkflowRunRequest request) {
        Long sessionId = request != null ? request.getSessionId() : null;
        AiWorkflowRun run = workflowExecutionService.runWorkflow(id, sessionId);
        return ResponseEntity.status(HttpStatus.CREATED).body(mapRunToResponse(run));
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------
    private AiWorkflowRunResponse mapRunToResponse(AiWorkflowRun run) {
        AiWorkflowRunResponse response = new AiWorkflowRunResponse();
        response.setId(run.getId());
        response.setWorkflowId(run.getWorkflowId());
        response.setSessionId(run.getSessionId());
        response.setTriggeredBy(run.getTriggeredBy());
        response.setStatus(run.getStatus());
        response.setResultJson(run.getResultJson());
        response.setCreatedAt(run.getCreatedAt());
        response.setCompletedAt(run.getCompletedAt());
        return response;
    }
}
