package com.lms.progress.controller;

import com.lms.progress.dto.BatchProgressReportDTO;
import com.lms.progress.dto.StudentProgressReportDTO;
import com.lms.progress.dto.TrainerProgressReportDTO;
import com.lms.progress.service.BatchProgressService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/progress/reports")
public class BatchProgressController {

    @Autowired
    private BatchProgressService batchProgressService;

    // ─────────────────────────────────────────────────────────────────────
    // TRAINER ENDPOINTS
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Trainer clicks on a batch → gets all students' progress in that batch
     *
     * GET /progress/reports/batch/{batchId}
     *
     * Response: BatchProgressReportDTO
     *   - batchId
     *   - totalStudents
     *   - avgVideoWatchPercentage, avgFileDownloadPercentage, ...
     *   - studentReports: [ { studentEmail, videosWatched, ... }, ... ]
     */
    @GetMapping("/batch/{batchId}")
    public ResponseEntity<BatchProgressReportDTO> getBatchReport(
            @PathVariable Long batchId) {

        BatchProgressReportDTO report = batchProgressService.getBatchProgressReport(batchId);
        return ResponseEntity.ok(report);
    }

    /**
     * Trainer clicks on a specific student inside a batch → full detailed report
     *
     * GET /progress/reports/batch/{batchId}/student/{studentEmail}
     *
     * Response: StudentProgressReportDTO
     *   - studentEmail, batchId
     *   - totalVideos, videosWatched, videoWatchPercentage
     *   - totalFiles, filesDownloaded, fileDownloadPercentage
     *   - totalQuizzes, quizzesCompleted, quizCompletionPercentage
     *   - totalAssignments, assignmentsCompleted, assignmentCompletionPercentage
     *   - totalCourseContent, courseContentCompleted, courseProgressPercentage
     *   - overallProgressPercentage
     */
    @GetMapping("/batch/{batchId}/student/{studentEmail}")
    public ResponseEntity<StudentProgressReportDTO> getStudentReportInBatch(
            @PathVariable Long batchId,
            @PathVariable String studentEmail) {

        StudentProgressReportDTO report =
                batchProgressService.getStudentProgressInBatch(batchId, studentEmail);
        return ResponseEntity.ok(report);
    }

    // ─────────────────────────────────────────────────────────────────────
    // ADMIN ENDPOINTS
    // ─────────────────────────────────────────────────────────────────────

    /**
     * Admin clicks on a trainer → all batches + their student progress
     *
     * GET /progress/reports/trainer/{trainerEmail}
     *
     * Response: TrainerProgressReportDTO
     *   - trainerEmail
     *   - totalBatches, totalStudentsHandled
     *   - avgVideoWatchPercentage, avgFileDownloadPercentage, ...
     *   - batchReports: [ { batchId, studentReports: [...] }, ... ]
     */
    @GetMapping("/trainer/{trainerEmail}")
    public ResponseEntity<TrainerProgressReportDTO> getTrainerReport(
            @PathVariable String trainerEmail) {

        TrainerProgressReportDTO report =
                batchProgressService.getTrainerProgressReport(trainerEmail);
        return ResponseEntity.ok(report);
    }
}