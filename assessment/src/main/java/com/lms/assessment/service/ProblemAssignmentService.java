

package com.lms.assessment.service;

import com.lms.assessment.dto.ProblemAssignmentRequest;
import com.lms.assessment.dto.ProblemAssignmentResponse;
import com.lms.assessment.exception.AlreadyAssignedException;
import com.lms.assessment.kafka.AssessmentEventProducer;
import com.lms.assessment.dto.CodingProblemResponse;
import com.lms.assessment.model.CodingProblem;
import com.lms.assessment.model.ProblemAssignment;
import com.lms.assessment.repository.CodingProblemRepository;
import com.lms.assessment.repository.ProblemAssignmentRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class ProblemAssignmentService {

    private static final Logger log = LoggerFactory.getLogger(ProblemAssignmentService.class);

    private final ProblemAssignmentRepository problemAssignmentRepository;
    private final CodingProblemRepository codingProblemRepository;
    private final CodingProblemService codingProblemService;
    private final AssessmentEventProducer     eventProducer;

    public ProblemAssignmentService(ProblemAssignmentRepository problemAssignmentRepository,
                                    CodingProblemRepository codingProblemRepository,
                                    CodingProblemService codingProblemService,
                                    AssessmentEventProducer     eventProducer) {
        this.problemAssignmentRepository = problemAssignmentRepository;
        this.codingProblemRepository     = codingProblemRepository;
        this.codingProblemService        = codingProblemService;
        this.eventProducer  =eventProducer;
    }

    // ── Trainer: assign problem to batch ──────────
    @Transactional
    public ProblemAssignmentResponse assignProblem(ProblemAssignmentRequest request, String organizationId) {
        String trainerEmail = extractEmailFromJwt();

        CodingProblem problem = codingProblemRepository.findById(request.getProblemId())
            .orElseThrow(() -> new RuntimeException("Problem not found: " + request.getProblemId()));

        assertSameOrg(problem.getOrganizationId(), organizationId);

        // ✅ Only block if ACTIVELY assigned (ignores soft-deleted rows)
        boolean activelyAssigned = problemAssignmentRepository
            .existsByProblemIdAndBatchIdAndIsActiveTrue(
                request.getProblemId(), request.getBatchId());

        if (activelyAssigned) {
            throw new AlreadyAssignedException("Problem already assigned to this batch");
        }

        // ✅ Reactivate old soft-deleted row OR create fresh one
        ProblemAssignment assignment = problemAssignmentRepository
            .findByProblemIdAndBatchId(request.getProblemId(), request.getBatchId())
            .orElseGet(ProblemAssignment::new);

        assignment.setProblem(problem);
        assignment.setBatchId(request.getBatchId());
        assignment.setAssignedByEmail(trainerEmail);
        assignment.setDueDate(request.getDueDate());
        assignment.setIsActive(true);  // ✅ reactivates removed assignment

        ProblemAssignment saved = problemAssignmentRepository.save(assignment);

        eventProducer.publishCodingProblemAssigned(
            problem.getId(), problem.getTitle(), request.getBatchId(), trainerEmail);

        return toResponse(saved);
    }

    // ── Trainer: unassign problem from batch ──────
    @Transactional
    public void unassignProblem(Long assignmentId, String organizationId) {
        ProblemAssignment assignment = problemAssignmentRepository.findById(assignmentId)
            .orElseThrow(() -> new RuntimeException("Assignment not found: " + assignmentId));

        assertSameOrg(assignment.getProblem().getOrganizationId(), organizationId);

        assignment.setIsActive(false);
        problemAssignmentRepository.save(assignment);
        log.info("Assignment soft-deleted: id={}", assignmentId);
    }

    // ── Trainer: view all assignments for a batch ─
    @Transactional(readOnly = true)
    public List<ProblemAssignmentResponse> getAssignmentsByBatch(String batchId, String organizationId) {
        return problemAssignmentRepository.findByBatchIdAndIsActiveTrue(batchId)
            .stream()
            .filter(a -> organizationId == null || organizationId.equals(a.getProblem().getOrganizationId()))
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    // ── Student: get all active problems for their batch ──
    @Transactional(readOnly = true)
    public List<CodingProblemResponse> getProblemsForStudent(String batchId, String organizationId) {
        return problemAssignmentRepository.findByBatchIdAndIsActiveTrue(batchId)
            .stream()
            .filter(a -> organizationId == null || organizationId.equals(a.getProblem().getOrganizationId()))
            .map(a -> codingProblemService.getProblemForStudent(a.getProblem().getId(), organizationId))
            .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public CodingProblemResponse getProblemForStudentById(Long problemId, String organizationId) {
        return codingProblemService.getProblemForStudent(problemId, organizationId);
    }

    // ── Private helpers ───────────────────────────
    private ProblemAssignmentResponse toResponse(ProblemAssignment a) {
        return ProblemAssignmentResponse.builder()
            .assignmentId(a.getId())
            .problemId(a.getProblem().getId())
            .problemTitle(a.getProblem().getTitle())
            .batchId(a.getBatchId())
            .assignedByEmail(a.getAssignedByEmail())
            .assignedAt(a.getAssignedAt())
            .dueDate(a.getDueDate())
            .isActive(a.getIsActive())
            .build();
    }

    private String extractEmailFromJwt() {
        return SecurityContextHolder.getContext()
            .getAuthentication()
            .getName();
    }

    // ================= ORG GUARD (private) =================

    private void assertSameOrg(String resourceOrgId, String callerOrgId) {
        if (callerOrgId == null) return;
        if (!callerOrgId.equals(resourceOrgId)) {
            throw new RuntimeException("Access denied: problem belongs to a different organization");
        }
    }
 // ── Admin/Trainer: view all batches a problem is assigned to ─
    @Transactional(readOnly = true)
    public List<ProblemAssignmentResponse> getAssignmentsByProblem(Long problemId, String organizationId) {
        return problemAssignmentRepository.findByProblemIdAndIsActiveTrue(problemId)
            .stream()
            .filter(a -> organizationId == null || organizationId.equals(a.getProblem().getOrganizationId()))
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
 // ── Super Admin: view all batches a problem is assigned to (no org filter) ─
    @Transactional(readOnly = true)
    public List<ProblemAssignmentResponse> getAssignmentsByProblemForSuperAdmin(Long problemId) {
        return problemAssignmentRepository.findByProblemIdAndIsActiveTrue(problemId)
            .stream()
            .filter(a -> a.getProblem().getOrganizationId() == null)
            .map(this::toResponse)
            .collect(Collectors.toList());
    }
    
}