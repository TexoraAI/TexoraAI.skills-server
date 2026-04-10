package com.lms.progress.service;

import com.lms.progress.dto.AssignmentProgressResponse;
import com.lms.progress.model.AssignmentProgress;
import com.lms.progress.repository.AssignmentProgressRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;

@Service
public class AssignmentProgressService {

    private final AssignmentProgressRepository repo;

    public AssignmentProgressService(AssignmentProgressRepository repo) {
        this.repo = repo;
    }

    public AssignmentProgressResponse markCompleted(
            String email,
            Long batchId,
            Long assignmentId,
            int totalAssignments) {

        AssignmentProgress p = repo
                .findByStudentEmailAndBatchId(email, batchId)
                .orElseGet(() -> {
                    AssignmentProgress fresh = new AssignmentProgress();
                    fresh.setStudentEmail(email);
                    fresh.setBatchId(batchId);
                    fresh.setCompletedAssignmentIds(new ArrayList<>());
                    return fresh;
                });

        p.setTotalAssignments(totalAssignments);

        if (!p.getCompletedAssignmentIds().contains(assignmentId)) {
            p.getCompletedAssignmentIds().add(assignmentId);
        }

        double percentage =
                (double) p.getCompletedAssignmentIds().size()
                        / totalAssignments * 100;

        p.setPercentage(Math.min(percentage, 100));
        p.setUpdatedAt(Instant.now());

        return toResponse(repo.save(p));
    }

    public AssignmentProgressResponse get(String email, Long batchId) {
        return repo.findByStudentEmailAndBatchId(email, batchId)
                .map(this::toResponse)
                .orElse(new AssignmentProgressResponse());
    }

    private AssignmentProgressResponse toResponse(AssignmentProgress p) {
        AssignmentProgressResponse r = new AssignmentProgressResponse();
        r.setId(p.getId());
        r.setStudentEmail(p.getStudentEmail());
        r.setBatchId(p.getBatchId());
        r.setCompletedAssignmentIds(p.getCompletedAssignmentIds());
        r.setTotalAssignments(p.getTotalAssignments());
        r.setPercentage(p.getPercentage());
        r.setUpdatedAt(p.getUpdatedAt());
        return r;
    }
}