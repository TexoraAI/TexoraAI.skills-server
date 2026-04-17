package com.lms.progress.service;

import com.lms.progress.dto.BatchProgressReportDTO;
import com.lms.progress.dto.StudentProgressReportDTO;
import com.lms.progress.dto.TrainerProgressReportDTO;
import com.lms.progress.model.*;
import com.lms.progress.repository.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Optional;
import java.util.Set;

@Service
public class BatchProgressService {

    @Autowired
    private StudentBatchMapRepository studentBatchMapRepository;

    @Autowired
    private TrainerBatchMapRepository trainerBatchMapRepository;

    @Autowired
    private VideoProgressRepository videoProgressRepository;

    @Autowired
    private FileProgressRepository fileProgressRepository;

    @Autowired
    private QuizProgressRepository quizProgressRepository;

    @Autowired
    private AssignmentProgressRepository assignmentProgressRepository;

    @Autowired
    private ProgressRepository progressRepository;

    // ─────────────────────────────────────────────────────────────────────
    // 1.  Trainer clicks a batch → full report for ALL students in that batch
    // GET /progress/reports/batch/{batchId}
    // ─────────────────────────────────────────────────────────────────────
    public BatchProgressReportDTO getBatchProgressReport(Long batchId) {

        // All students assigned to this batch
        List<StudentBatchMap> studentMappings = studentBatchMapRepository.findByBatchId(batchId);

        if (studentMappings.isEmpty()) {
            throw new RuntimeException("No students found for batchId: " + batchId);
        }

        List<StudentProgressReportDTO> studentReports = new ArrayList<>();

        for (StudentBatchMap mapping : studentMappings) {
            StudentProgressReportDTO report = buildStudentReport(mapping.getStudentEmail(), batchId);
            studentReports.add(report);
        }

        BatchProgressReportDTO batchReport = new BatchProgressReportDTO();
        batchReport.setBatchId(batchId);
        batchReport.setTotalStudents(studentReports.size());
        batchReport.setStudentReports(studentReports);

        // Compute batch-level averages
        batchReport.setAvgVideoWatchPercentage(
                round(avg(studentReports, r -> r.getVideoWatchPercentage())));
        batchReport.setAvgFileDownloadPercentage(
                round(avg(studentReports, r -> r.getFileDownloadPercentage())));
        batchReport.setAvgQuizCompletionPercentage(
                round(avg(studentReports, r -> r.getQuizCompletionPercentage())));
        batchReport.setAvgAssignmentCompletionPercentage(
                round(avg(studentReports, r -> r.getAssignmentCompletionPercentage())));
        batchReport.setAvgCourseProgressPercentage(
                round(avg(studentReports, r -> r.getCourseProgressPercentage())));
        batchReport.setAvgOverallProgressPercentage(
                round(avg(studentReports, r -> r.getOverallProgressPercentage())));

        return batchReport;
    }

    // ─────────────────────────────────────────────────────────────────────
    // 2.  Trainer clicks a student inside a batch → individual detailed report
    // GET /progress/reports/batch/{batchId}/student/{studentEmail}
    // ─────────────────────────────────────────────────────────────────────
    public StudentProgressReportDTO getStudentProgressInBatch(Long batchId, String studentEmail) {

        boolean exists = studentBatchMapRepository
                .existsByStudentEmailAndBatchId(studentEmail, batchId);

        if (!exists) {
            throw new RuntimeException(
                    "Student " + studentEmail + " is not mapped to batchId: " + batchId);
        }

        return buildStudentReport(studentEmail, batchId);
    }

    // ─────────────────────────────────────────────────────────────────────
    // 3.  Admin clicks a trainer → all batches + their student progress
    // GET /progress/reports/trainer/{trainerEmail}
    // ─────────────────────────────────────────────────────────────────────
    public TrainerProgressReportDTO getTrainerProgressReport(String trainerEmail) {

        List<TrainerBatchMap> trainerBatchMaps =
                trainerBatchMapRepository.findByTrainerEmail(trainerEmail);

        if (trainerBatchMaps.isEmpty()) {
            throw new RuntimeException("No batches found for trainer: " + trainerEmail);
        }

        List<BatchProgressReportDTO> batchReports = new ArrayList<>();

        // Track unique students across all batches to count total
        Set<String> allStudentEmails = new HashSet<>();

        for (TrainerBatchMap tbm : trainerBatchMaps) {
            BatchProgressReportDTO batchReport = getBatchProgressReport(tbm.getBatchId());
            batchReports.add(batchReport);

            // Collect all student emails from this batch
            for (StudentProgressReportDTO sr : batchReport.getStudentReports()) {
                allStudentEmails.add(sr.getStudentEmail());
            }
        }

        TrainerProgressReportDTO trainerReport = new TrainerProgressReportDTO();
        trainerReport.setTrainerEmail(trainerEmail);
        trainerReport.setTotalBatches(batchReports.size());
        trainerReport.setTotalStudentsHandled(allStudentEmails.size());
        trainerReport.setBatchReports(batchReports);

        // Trainer-level averages (average of batch averages)
        trainerReport.setAvgVideoWatchPercentage(
                round(avgBatch(batchReports, r -> r.getAvgVideoWatchPercentage())));
        trainerReport.setAvgFileDownloadPercentage(
                round(avgBatch(batchReports, r -> r.getAvgFileDownloadPercentage())));
        trainerReport.setAvgQuizCompletionPercentage(
                round(avgBatch(batchReports, r -> r.getAvgQuizCompletionPercentage())));
        trainerReport.setAvgAssignmentCompletionPercentage(
                round(avgBatch(batchReports, r -> r.getAvgAssignmentCompletionPercentage())));
        trainerReport.setAvgCourseProgressPercentage(
                round(avgBatch(batchReports, r -> r.getAvgCourseProgressPercentage())));
        trainerReport.setAvgOverallProgressPercentage(
                round(avgBatch(batchReports, r -> r.getAvgOverallProgressPercentage())));

        return trainerReport;
    }

    // ─────────────────────────────────────────────────────────────────────
    // PRIVATE: Build one student's full progress report for a given batch
    // ─────────────────────────────────────────────────────────────────────
    private StudentProgressReportDTO buildStudentReport(String studentEmail, Long batchId) {

        StudentProgressReportDTO dto = new StudentProgressReportDTO();
        dto.setStudentEmail(studentEmail);
        dto.setBatchId(batchId);

        // ── Video Progress ──────────────────────────────────────────────
        // VideoProgress stores: watchedVideoIds (list), totalVideoCount, watchPercentage
        Optional<VideoProgress> vpOpt =
                videoProgressRepository.findByStudentEmailAndBatchId(studentEmail, batchId);

        if (vpOpt.isPresent()) {
            VideoProgress vp = vpOpt.get();
            int watched = vp.getWatchedVideoIds() != null ? vp.getWatchedVideoIds().size() : 0;
            dto.setTotalVideos(vp.getTotalVideoCount());
            dto.setVideosWatched(watched);
            dto.setVideoWatchPercentage(round(vp.getWatchPercentage()));
        } else {
            dto.setTotalVideos(0);
            dto.setVideosWatched(0);
            dto.setVideoWatchPercentage(0.0);
        }

        // ── File Progress ───────────────────────────────────────────────
        // FileProgress stores: downloadedFileIds (list), totalFileCount, downloadPercentage
        Optional<FileProgress> fpOpt =
                fileProgressRepository.findByStudentEmailAndBatchId(studentEmail, batchId);

        if (fpOpt.isPresent()) {
            FileProgress fp = fpOpt.get();
            int downloaded = fp.getDownloadedFileIds() != null ? fp.getDownloadedFileIds().size() : 0;
            dto.setTotalFiles(fp.getTotalFileCount());
            dto.setFilesDownloaded(downloaded);
            dto.setFileDownloadPercentage(round(fp.getDownloadPercentage()));
        } else {
            dto.setTotalFiles(0);
            dto.setFilesDownloaded(0);
            dto.setFileDownloadPercentage(0.0);
        }

        // ── Quiz Progress ───────────────────────────────────────────────
        // QuizProgress stores: completedQuizIds (list), totalQuizzes, percentage
        Optional<QuizProgress> qpOpt =
                quizProgressRepository.findByStudentEmailAndBatchId(studentEmail, batchId);

        if (qpOpt.isPresent()) {
            QuizProgress qp = qpOpt.get();
            int completed = qp.getCompletedQuizIds() != null ? qp.getCompletedQuizIds().size() : 0;
            dto.setTotalQuizzes(qp.getTotalQuizzes());
            dto.setQuizzesCompleted(completed);
            dto.setQuizCompletionPercentage(round(qp.getPercentage()));
        } else {
            dto.setTotalQuizzes(0);
            dto.setQuizzesCompleted(0);
            dto.setQuizCompletionPercentage(0.0);
        }

        // ── Assignment Progress ─────────────────────────────────────────
        // AssignmentProgress stores: completedAssignmentIds (list), totalAssignments, percentage
        Optional<AssignmentProgress> apOpt =
                assignmentProgressRepository.findByStudentEmailAndBatchId(studentEmail, batchId);

        if (apOpt.isPresent()) {
            AssignmentProgress ap = apOpt.get();
            int completed = ap.getCompletedAssignmentIds() != null
                    ? ap.getCompletedAssignmentIds().size() : 0;
            dto.setTotalAssignments(ap.getTotalAssignments());
            dto.setAssignmentsCompleted(completed);
            dto.setAssignmentCompletionPercentage(round(ap.getPercentage()));
        } else {
            dto.setTotalAssignments(0);
            dto.setAssignmentsCompleted(0);
            dto.setAssignmentCompletionPercentage(0.0);
        }

        // ── Course Content Progress ─────────────────────────────────────
        // Progress stores: completedContentIds, totalContentCount, progressPercentage
        // A student may be enrolled in multiple courses — sum them all up
        List<Progress> progressList =
                progressRepository.findByStudentEmail(studentEmail);

        int totalContent = 0;
        int completedContent = 0;

        for (Progress p : progressList) {
            totalContent += p.getTotalContentCount();
            completedContent += p.getCompletedContentIds() != null
                    ? p.getCompletedContentIds().size() : 0;
        }

        dto.setTotalCourseContent(totalContent);
        dto.setCourseContentCompleted(completedContent);
        dto.setCourseProgressPercentage(
                totalContent > 0 ? round((completedContent * 100.0) / totalContent) : 0.0);

        // ── Overall ─────────────────────────────────────────────────────
        // Average of all 5 progress areas
        double overall = (dto.getVideoWatchPercentage()
                + dto.getFileDownloadPercentage()
                + dto.getQuizCompletionPercentage()
                + dto.getAssignmentCompletionPercentage()
                + dto.getCourseProgressPercentage()) / 5.0;

        dto.setOverallProgressPercentage(round(overall));

        return dto;
    }

    // ─────────────────────────────────────────────────────────────────────
    // PRIVATE HELPERS
    // ─────────────────────────────────────────────────────────────────────

    @FunctionalInterface
    private interface StudentExtractor {
        double get(StudentProgressReportDTO r);
    }

    @FunctionalInterface
    private interface BatchExtractor {
        double get(BatchProgressReportDTO r);
    }

    private double avg(List<StudentProgressReportDTO> list, StudentExtractor fn) {
        if (list == null || list.isEmpty()) return 0.0;
        double sum = 0;
        for (StudentProgressReportDTO r : list) sum += fn.get(r);
        return sum / list.size();
    }

    private double avgBatch(List<BatchProgressReportDTO> list, BatchExtractor fn) {
        if (list == null || list.isEmpty()) return 0.0;
        double sum = 0;
        for (BatchProgressReportDTO r : list) sum += fn.get(r);
        return sum / list.size();
    }

    private double round(double value) {
        return Math.round(value * 100.0) / 100.0;
    }
}