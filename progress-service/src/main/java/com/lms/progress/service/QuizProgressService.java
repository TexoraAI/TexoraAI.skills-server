package com.lms.progress.service;

import com.lms.progress.dto.QuizProgressResponse;
import com.lms.progress.model.QuizProgress;
import com.lms.progress.repository.QuizProgressRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.ArrayList;

@Service
public class QuizProgressService {

    private final QuizProgressRepository repo;

    public QuizProgressService(QuizProgressRepository repo) {
        this.repo = repo;
    }

    // ================= MARK QUIZ ATTEMPTED =================
    public QuizProgressResponse markAttempted(
            String email,
            Long batchId,
            Long quizId,
            int totalQuizzes) {

        QuizProgress p = repo
                .findByStudentEmailAndBatchId(email, batchId)
                .orElseGet(() -> {
                    QuizProgress fresh = new QuizProgress();
                    fresh.setStudentEmail(email);
                    fresh.setBatchId(batchId);
                    fresh.setCompletedQuizIds(new ArrayList<>());
                    return fresh;
                });

        p.setTotalQuizzes(totalQuizzes);

        if (!p.getCompletedQuizIds().contains(quizId)) {
            p.getCompletedQuizIds().add(quizId);
        }

        double pct = totalQuizzes > 0
                ? (double) p.getCompletedQuizIds().size() / totalQuizzes * 100
                : 0;

        p.setPercentage(Math.min(pct, 100));
        p.setUpdatedAt(Instant.now());

        return toResponse(repo.save(p));
    }

    // ================= GET PROGRESS =================
    public QuizProgressResponse get(String email, Long batchId) {
        return repo.findByStudentEmailAndBatchId(email, batchId)
                .map(this::toResponse)
                .orElseGet(() -> {
                    QuizProgressResponse empty = new QuizProgressResponse();
                    empty.setStudentEmail(email);
                    empty.setBatchId(batchId);
                    empty.setCompletedQuizIds(new ArrayList<>());
                    empty.setTotalQuizzes(0);
                    empty.setPercentage(0);
                    empty.setUpdatedAt(Instant.now());
                    return empty;
                });
    }

    // ================= MAPPER =================
    private QuizProgressResponse toResponse(QuizProgress p) {
        QuizProgressResponse r = new QuizProgressResponse();
        r.setId(p.getId());
        r.setStudentEmail(p.getStudentEmail());
        r.setBatchId(p.getBatchId());
        r.setCompletedQuizIds(p.getCompletedQuizIds());
        r.setTotalQuizzes(p.getTotalQuizzes());
        r.setPercentage(p.getPercentage());
        r.setUpdatedAt(p.getUpdatedAt());
        return r;
    }
}