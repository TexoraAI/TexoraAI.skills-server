package com.lms.live_session.service;

import com.lms.live_session.dto.AiWorkflowRequest;
import com.lms.live_session.dto.AiWorkflowResponse;
import com.lms.live_session.dto.AiWorkflowStatusRequest;
import com.lms.live_session.entity.AiWorkflow;
import com.lms.live_session.repository.AiWorkflowRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class AiWorkflowService {

    private final AiWorkflowRepository workflowRepository;

    public AiWorkflowService(AiWorkflowRepository workflowRepository) {
        this.workflowRepository = workflowRepository;
    }

    // ----------------------------------------------------------------
    // Extract trainer email from JWT — same pattern as LiveSession service
    // ----------------------------------------------------------------
    private String getCurrentTrainerEmail() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        if (auth == null || auth.getName() == null) {
            throw new RuntimeException("Unauthorized: no authenticated user found");
        }
        return auth.getName(); // JWT subject = trainerEmail
    }

    // ----------------------------------------------------------------
    // Create
    // ----------------------------------------------------------------
    public AiWorkflowResponse createWorkflow(AiWorkflowRequest request) {
        AiWorkflow workflow = new AiWorkflow();

        workflow.setTrainerEmail(getCurrentTrainerEmail());
        workflow.setName(valueOrDefault(request.getName(), "Untitled Workflow"));
        workflow.setDescription(request.getDescription());
        workflow.setCategory(request.getCategory());
        workflow.setTriggerType(request.getTriggerType());
        workflow.setStatus(valueOrDefault(request.getStatus(), "DRAFT"));
        workflow.setSourceType(valueOrDefault(request.getSourceType(), "CUSTOM"));
        workflow.setTemplateKey(request.getTemplateKey());
        workflow.setNodesJson(request.getNodesJson());
        workflow.setConfigJson(request.getConfigJson());
        workflow.setLastRunStatus("NEVER_RUN");
        workflow.setLastRunAt(null);

        return mapToResponse(workflowRepository.save(workflow));
    }

    // ----------------------------------------------------------------
    // List my workflows — scoped to current trainer email
    // ----------------------------------------------------------------
    public List<AiWorkflowResponse> getMyWorkflows(String search, String status) {
        String trainerEmail = getCurrentTrainerEmail();
        boolean hasSearch = search != null && !search.isBlank();
        boolean hasStatus = status != null && !status.isBlank();

        List<AiWorkflow> results;

        if (hasSearch && hasStatus) {
            results = workflowRepository
                    .findByTrainerEmailAndStatusAndNameContainingIgnoreCaseOrderByUpdatedAtDesc(
                            trainerEmail, status, search);
        } else if (hasSearch) {
            results = workflowRepository
                    .findByTrainerEmailAndNameContainingIgnoreCaseOrderByUpdatedAtDesc(
                            trainerEmail, search);
        } else if (hasStatus) {
            results = workflowRepository
                    .findByTrainerEmailAndStatusOrderByUpdatedAtDesc(trainerEmail, status);
        } else {
            results = workflowRepository
                    .findByTrainerEmailOrderByUpdatedAtDesc(trainerEmail);
        }

        return results.stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ----------------------------------------------------------------
    // Get by ID — validates ownership by trainerEmail
    // ----------------------------------------------------------------
    public AiWorkflowResponse getWorkflow(Long id) {
        AiWorkflow workflow = findAndVerifyOwnership(id);
        return mapToResponse(workflow);
    }

    // ----------------------------------------------------------------
    // Update — null-safe, keeps existing values if field not provided
    // ----------------------------------------------------------------
    public AiWorkflowResponse updateWorkflow(Long id, AiWorkflowRequest request) {
        AiWorkflow workflow = findAndVerifyOwnership(id);

        if (request.getName() != null) {
            workflow.setName(request.getName().isBlank() ? "Untitled Workflow" : request.getName());
        }
        if (request.getDescription() != null) {
            workflow.setDescription(request.getDescription());
        }
        if (request.getCategory() != null) {
            workflow.setCategory(request.getCategory());
        }
        if (request.getTriggerType() != null) {
            workflow.setTriggerType(request.getTriggerType());
        }
        if (request.getStatus() != null && !request.getStatus().isBlank()) {
            workflow.setStatus(request.getStatus());
        }
        if (request.getSourceType() != null && !request.getSourceType().isBlank()) {
            workflow.setSourceType(request.getSourceType());
        }
        if (request.getTemplateKey() != null) {
            workflow.setTemplateKey(request.getTemplateKey());
        }
        if (request.getNodesJson() != null) {
            workflow.setNodesJson(request.getNodesJson());
        }
        if (request.getConfigJson() != null) {
            workflow.setConfigJson(request.getConfigJson());
        }

        return mapToResponse(workflowRepository.save(workflow));
    }

    // ----------------------------------------------------------------
    // Delete — validates ownership before deleting
    // ----------------------------------------------------------------
    public void deleteWorkflow(Long id) {
        findAndVerifyOwnership(id); // throws if not found or not owner
        workflowRepository.deleteById(id);
    }

    // ----------------------------------------------------------------
    // Duplicate — copies all fields, resets run state, sets DRAFT
    // ----------------------------------------------------------------
    public AiWorkflowResponse duplicateWorkflow(Long id) {
        AiWorkflow original = findAndVerifyOwnership(id);

        AiWorkflow copy = new AiWorkflow();
        copy.setTrainerEmail(getCurrentTrainerEmail());
        copy.setName(original.getName() + " Copy");
        copy.setDescription(original.getDescription());
        copy.setCategory(original.getCategory());
        copy.setTriggerType(original.getTriggerType());
        copy.setStatus("DRAFT");
        copy.setSourceType(original.getSourceType());
        copy.setTemplateKey(original.getTemplateKey());
        copy.setNodesJson(original.getNodesJson());
        copy.setConfigJson(original.getConfigJson());
        copy.setLastRunStatus("NEVER_RUN");
        copy.setLastRunAt(null);

        return mapToResponse(workflowRepository.save(copy));
    }

    // ----------------------------------------------------------------
    // Update status only
    // ----------------------------------------------------------------
    public AiWorkflowResponse updateStatus(Long id, AiWorkflowStatusRequest request) {
        if (request.getStatus() == null || request.getStatus().isBlank()) {
            throw new RuntimeException("Status is required");
        }
        AiWorkflow workflow = findAndVerifyOwnership(id);
        workflow.setStatus(request.getStatus());
        return mapToResponse(workflowRepository.save(workflow));
    }

    // ----------------------------------------------------------------
    // Private helpers
    // ----------------------------------------------------------------

    /**
     * Loads workflow by id and verifies the current trainer owns it.
     * Throws RuntimeException if not found or if trainerEmail doesn't match.
     */
    private AiWorkflow findAndVerifyOwnership(Long id) {
        AiWorkflow workflow = workflowRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Workflow not found: " + id));

        String currentEmail = getCurrentTrainerEmail();
        if (!currentEmail.equalsIgnoreCase(workflow.getTrainerEmail())) {
            throw new RuntimeException("Access denied: workflow does not belong to current trainer");
        }
        return workflow;
    }

    private AiWorkflowResponse mapToResponse(AiWorkflow workflow) {
        AiWorkflowResponse response = new AiWorkflowResponse();
        response.setId(workflow.getId());
        response.setTrainerEmail(workflow.getTrainerEmail());
        response.setName(workflow.getName());
        response.setDescription(workflow.getDescription());
        response.setCategory(workflow.getCategory());
        response.setTriggerType(workflow.getTriggerType());
        response.setStatus(workflow.getStatus());
        response.setSourceType(workflow.getSourceType());
        response.setTemplateKey(workflow.getTemplateKey());
        response.setNodesJson(workflow.getNodesJson());
        response.setConfigJson(workflow.getConfigJson());
        response.setLastRunStatus(workflow.getLastRunStatus());
        response.setLastRunAt(workflow.getLastRunAt());
        response.setCreatedAt(workflow.getCreatedAt());
        response.setUpdatedAt(workflow.getUpdatedAt());
        return response;
    }

    private String valueOrDefault(String value, String defaultValue) {
        return (value == null || value.isBlank()) ? defaultValue : value;
    }
}