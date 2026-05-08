//package com.lms.progress.service;
//
//import com.lms.progress.dto.*;
//import com.lms.progress.model.SkillScore;
//import com.lms.progress.repository.SkillScoreRepository;
//import org.springframework.stereotype.Service;
//
//import java.time.Instant;
//import java.util.*;
//import java.util.stream.Collectors;
//
//@Service
//public class SkillMapService {
//
//    private final SkillScoreRepository repo;
//
//    public SkillMapService(SkillScoreRepository repo) {
//        this.repo = repo;
//    }
//
//    // ══════════════════════════════════════════════════════
//    // 1. UPSERT — called when quiz/assignment/video finishes
//    //    POST /api/skill-map/upsert
//    //    Updates only the sub-score that changed,
//    //    recalculates overall, flips isWeak / isStrong flags
//    // ══════════════════════════════════════════════════════
//    public SkillEntryDTO upsertSkill(SkillUpsertRequest req) {
//
//        // find existing row OR create fresh one
//        SkillScore s = repo.findByStudentEmailAndBatchIdAndSkillName(
//                req.getStudentEmail(), req.getBatchId(), req.getSkillName())
//                .orElseGet(() -> {
//                    SkillScore fresh = new SkillScore();
//                    fresh.setStudentEmail(req.getStudentEmail());
//                    fresh.setBatchId(req.getBatchId());
//                    fresh.setTrainerEmail(req.getTrainerEmail());
//                    fresh.setSkillName(req.getSkillName());
//                    fresh.setQuizScore(0);
//                    fresh.setAssignmentScore(0);
//                    fresh.setVideoScore(0);
//                    fresh.setOverallScore(0);
//                    return fresh;
//                });
//
//        // only overwrite sub-scores that were actually sent
//        if (req.getQuizScore()       != null) s.setQuizScore(req.getQuizScore());
//        if (req.getAssignmentScore() != null) s.setAssignmentScore(req.getAssignmentScore());
//        if (req.getVideoScore()      != null) s.setVideoScore(req.getVideoScore());
//        if (req.getTrainerEmail()    != null) s.setTrainerEmail(req.getTrainerEmail());
//
//        // recalculate overall: quiz 40% + assignment 40% + video 20%
//        double overall = (s.getQuizScore() * 0.40)
//                       + (s.getAssignmentScore() * 0.40)
//                       + (s.getVideoScore() * 0.20);
//        overall = Math.min(Math.round(overall * 10.0) / 10.0, 100.0);
//        s.setOverallScore(overall);
//
//        // set flags
//        s.setWeak(overall < 50);
//        s.setStrong(overall >= 70);
//        s.setUpdatedAt(Instant.now());
//
//        SkillScore saved = repo.save(s);
//
//        System.out.println("🧠 SkillMap upsert → " + req.getStudentEmail()
//                + " | batch=" + req.getBatchId()
//                + " | skill=" + req.getSkillName()
//                + " | overall=" + overall + "%");
//
//        return toSkillEntry(saved);
//    }
//
//    // ══════════════════════════════════════════════════════
//    // 2. STUDENT VIEW
//    //    GET /api/skill-map/student?email=x&batchId=5
//    //    Powers SkillMap.jsx — shows student their own skill scores
//    // ══════════════════════════════════════════════════════
//    public StudentSkillMapResponse getStudentSkillMap(String email, Long batchId) {
//
//        List<SkillScore> scores = repo.findByStudentEmailAndBatchId(email, batchId);
//
//        StudentSkillMapResponse res = new StudentSkillMapResponse();
//        res.setStudentEmail(email);
//        res.setBatchId(batchId);
//        res.setTotalSkills(scores.size());
//
//        long strongCount = scores.stream().filter(SkillScore::isStrong).count();
//        long weakCount   = scores.stream().filter(SkillScore::isWeak).count();
//        res.setStrongCount((int) strongCount);
//        res.setWeakCount((int) weakCount);
//
//        double avg = scores.isEmpty() ? 0 :
//                Math.round(scores.stream()
//                        .mapToDouble(SkillScore::getOverallScore)
//                        .average().orElse(0) * 10) / 10.0;
//        res.setAvgScore(avg);
//
//        res.setSkills(scores.stream()
//                .sorted(Comparator.comparingDouble(SkillScore::getOverallScore).reversed())
//                .map(this::toSkillEntry)
//                .collect(Collectors.toList()));
//
//        return res;
//    }
//
//    // ══════════════════════════════════════════════════════
//    // 3. TRAINER VIEW — batch analytics
//    //    GET /api/skill-map/trainer/batch?batchId=5
//    //    Powers TrainerSkillMap.jsx — shows trainer their batch
//    // ══════════════════════════════════════════════════════
//    public TrainerBatchSkillResponse getBatchSkillAnalytics(Long batchId) {
//
//        List<SkillScore> all = repo.findByBatchId(batchId);
//
//        // group by student
//        Map<String, List<SkillScore>> byStudent = all.stream()
//                .collect(Collectors.groupingBy(SkillScore::getStudentEmail));
//
//        // group by skill for averages
//        Map<String, List<SkillScore>> bySkill = all.stream()
//                .collect(Collectors.groupingBy(SkillScore::getSkillName));
//
//        // build student rows
//        List<TrainerStudentSkillRow> studentRows = new ArrayList<>();
//        int strongStudents = 0;
//        int weakStudents   = 0;
//
//        for (Map.Entry<String, List<SkillScore>> entry : byStudent.entrySet()) {
//            String email       = entry.getKey();
//            List<SkillScore> ss = entry.getValue();
//
//            TrainerStudentSkillRow row = new TrainerStudentSkillRow();
//            row.setStudentEmail(email);
//
//            double studentAvg = Math.round(ss.stream()
//                    .mapToDouble(SkillScore::getOverallScore)
//                    .average().orElse(0) * 10) / 10.0;
//            row.setAvgScore(studentAvg);
//
//            boolean needsHelp = ss.stream().anyMatch(SkillScore::isWeak);
//            row.setNeedsHelp(needsHelp);
//
//            boolean allStrong = ss.stream().allMatch(SkillScore::isStrong);
//
//            row.setSkills(ss.stream()
//                    .sorted(Comparator.comparingDouble(SkillScore::getOverallScore).reversed())
//                    .map(this::toSkillEntry)
//                    .collect(Collectors.toList()));
//
//            studentRows.add(row);
//            if (allStrong && !ss.isEmpty()) strongStudents++;
//            if (needsHelp) weakStudents++;
//        }
//
//        // build skill averages (for radar + bar chart)
//        List<SkillAvgEntry> skillAverages = new ArrayList<>();
//        for (Map.Entry<String, List<SkillScore>> entry : bySkill.entrySet()) {
//            double avg = Math.round(entry.getValue().stream()
//                    .mapToDouble(SkillScore::getOverallScore)
//                    .average().orElse(0) * 10) / 10.0;
//            SkillAvgEntry sae = new SkillAvgEntry();
//            sae.setSkillName(entry.getKey());
//            sae.setAvgScore(avg);
//            sae.setLevel(toLevel(avg));
//            skillAverages.add(sae);
//        }
//        skillAverages.sort(Comparator.comparing(SkillAvgEntry::getSkillName));
//
//        // overall batch avg
//        double batchAvg = all.isEmpty() ? 0 :
//                Math.round(all.stream()
//                        .mapToDouble(SkillScore::getOverallScore)
//                        .average().orElse(0) * 10) / 10.0;
//
//        // trainerEmail — take from first record
//        String trainerEmail = all.isEmpty() ? null : all.get(0).getTrainerEmail();
//
//        TrainerBatchSkillResponse res = new TrainerBatchSkillResponse();
//        res.setBatchId(batchId);
//        res.setTrainerEmail(trainerEmail);
//        res.setTotalStudents(byStudent.size());
//        res.setStrongStudents(strongStudents);
//        res.setWeakStudents(weakStudents);
//        res.setBatchAvgScore(batchAvg);
//        res.setSkillAverages(skillAverages);
//        res.setStudents(studentRows);
//
//        return res;
//    }
//
//    // ══════════════════════════════════════════════════════
//    // 4. ADMIN VIEW — org-wide
//    //    GET /api/skill-map/admin/org
//    //    Powers AdminSkillDashboard.jsx
//    // ══════════════════════════════════════════════════════
//    public AdminOrgSkillResponse getOrgSkillDashboard() {
//
//        List<SkillScore> all = repo.findAll();
//
//        // ── totals ──
//        Set<String> uniqueStudents = all.stream()
//                .map(SkillScore::getStudentEmail).collect(Collectors.toSet());
//        Set<Long> uniqueBatches = all.stream()
//                .map(SkillScore::getBatchId).collect(Collectors.toSet());
//
//        // strong learner = student where ALL skills are strong
//        Map<String, List<SkillScore>> byStudent = all.stream()
//                .collect(Collectors.groupingBy(SkillScore::getStudentEmail));
//
//        long totalStrong = byStudent.values().stream()
//                .filter(list -> !list.isEmpty() && list.stream().allMatch(SkillScore::isStrong))
//                .count();
//        long totalWeak = byStudent.values().stream()
//                .filter(list -> list.stream().anyMatch(SkillScore::isWeak))
//                .count();
//
//        double orgAvg = all.isEmpty() ? 0 :
//                Math.round(all.stream()
//                        .mapToDouble(SkillScore::getOverallScore)
//                        .average().orElse(0) * 10) / 10.0;
//
//        // ── org skill averages (radar + progress bars) ──
//        Map<String, List<SkillScore>> bySkill = all.stream()
//                .collect(Collectors.groupingBy(SkillScore::getSkillName));
//
//        List<SkillAvgEntry> orgSkillAvg = new ArrayList<>();
//        for (Map.Entry<String, List<SkillScore>> e : bySkill.entrySet()) {
//            double avg = Math.round(e.getValue().stream()
//                    .mapToDouble(SkillScore::getOverallScore)
//                    .average().orElse(0) * 10) / 10.0;
//            SkillAvgEntry sae = new SkillAvgEntry();
//            sae.setSkillName(e.getKey());
//            sae.setAvgScore(avg);
//            sae.setLevel(toLevel(avg));
//            orgSkillAvg.add(sae);
//        }
//        orgSkillAvg.sort(Comparator.comparing(SkillAvgEntry::getSkillName));
//
//        // ── batch summary cards ──
//        Map<Long, List<SkillScore>> byBatch = all.stream()
//                .collect(Collectors.groupingBy(SkillScore::getBatchId));
//
//        List<BatchSummaryEntry> batchSummaries = new ArrayList<>();
//        for (Map.Entry<Long, List<SkillScore>> e : byBatch.entrySet()) {
//            Long batchId       = e.getKey();
//            List<SkillScore> bs = e.getValue();
//
//            Map<String, List<SkillScore>> bsByStudent = bs.stream()
//                    .collect(Collectors.groupingBy(SkillScore::getStudentEmail));
//
//            long bStrongCount = bsByStudent.values().stream()
//                    .filter(l -> !l.isEmpty() && l.stream().allMatch(SkillScore::isStrong))
//                    .count();
//            long bWeakCount = bsByStudent.values().stream()
//                    .filter(l -> l.stream().anyMatch(SkillScore::isWeak))
//                    .count();
//
//            double bAvg = Math.round(bs.stream()
//                    .mapToDouble(SkillScore::getOverallScore)
//                    .average().orElse(0) * 10) / 10.0;
//
//            String trainerEmail = bs.stream()
//                    .map(SkillScore::getTrainerEmail)
//                    .filter(Objects::nonNull)
//                    .findFirst().orElse("Unknown");
//
//            BatchSummaryEntry bse = new BatchSummaryEntry();
//            bse.setBatchId(batchId);
//            bse.setTrainerEmail(trainerEmail);
//            bse.setStudents(bsByStudent.size());
//            bse.setAvgScore(bAvg);
//            bse.setStrongCount((int) bStrongCount);
//            bse.setWeakCount((int) bWeakCount);
//            batchSummaries.add(bse);
//        }
//        batchSummaries.sort(Comparator.comparing(BatchSummaryEntry::getBatchId));
//
//        // ── by-batch skill matrix (for "By Batch" tab bar chart) ──
//        // { skillName → { batchId → avg } }
//        Map<String, Map<Long, List<Double>>> matrix = new HashMap<>();
//        for (SkillScore ss : all) {
//            matrix
//                .computeIfAbsent(ss.getSkillName(), k -> new HashMap<>())
//                .computeIfAbsent(ss.getBatchId(),   k -> new ArrayList<>())
//                .add(ss.getOverallScore());
//        }
//
//        List<BatchSkillMatrix> batchSkillMatrix = new ArrayList<>();
//        for (Map.Entry<String, Map<Long, List<Double>>> skillEntry : matrix.entrySet()) {
//            BatchSkillMatrix bsm = new BatchSkillMatrix();
//            bsm.setSkillName(skillEntry.getKey());
//
//            Map<Long, Double> batchScores = new LinkedHashMap<>();
//            for (Map.Entry<Long, List<Double>> batchEntry : skillEntry.getValue().entrySet()) {
//                double avg = Math.round(batchEntry.getValue().stream()
//                        .mapToDouble(Double::doubleValue)
//                        .average().orElse(0) * 10) / 10.0;
//                batchScores.put(batchEntry.getKey(), avg);
//            }
//            bsm.setBatchScores(batchScores);
//            batchSkillMatrix.add(bsm);
//        }
//        batchSkillMatrix.sort(Comparator.comparing(BatchSkillMatrix::getSkillName));
//
//        // ── assemble final response ──
//        AdminOrgSkillResponse res = new AdminOrgSkillResponse();
//        res.setTotalStudents(uniqueStudents.size());
//        res.setOrgAvgScore(orgAvg);
//        res.setTotalStrongLearners((int) totalStrong);
//        res.setTotalNeedAttention((int) totalWeak);
//        res.setActiveBatches(uniqueBatches.size());
//        res.setOrgSkillAverages(orgSkillAvg);
//        res.setBatchSummaries(batchSummaries);
//        res.setBatchSkillMatrix(batchSkillMatrix);
//        return res;
//    }
//
//    // ══════════════════════════════════════════════════════
//    // 5. TRAINER — all batches they own
//    //    GET /api/skill-map/trainer?trainerEmail=x
//    //    Useful if trainer has multiple batches
//    // ══════════════════════════════════════════════════════
//    public List<TrainerBatchSkillResponse> getTrainerAllBatches(String trainerEmail) {
//
//        List<SkillScore> all = repo.findByTrainerEmail(trainerEmail);
//
//        // group by batchId and delegate to getBatchSkillAnalytics per batch
//        Set<Long> batchIds = all.stream()
//                .map(SkillScore::getBatchId).collect(Collectors.toSet());
//
//        return batchIds.stream()
//                .map(this::getBatchSkillAnalytics)
//                .sorted(Comparator.comparing(TrainerBatchSkillResponse::getBatchId))
//                .collect(Collectors.toList());
//    }
//
//    // ══════════════════════════════════════════════════════
//    // PRIVATE HELPERS
//    // ══════════════════════════════════════════════════════
//
//    private SkillEntryDTO toSkillEntry(SkillScore s) {
//        SkillEntryDTO dto = new SkillEntryDTO();
//        dto.setSkillName(s.getSkillName());
//        dto.setQuizScore(s.getQuizScore());
//        dto.setAssignmentScore(s.getAssignmentScore());
//        dto.setVideoScore(s.getVideoScore());
//        dto.setOverallScore(s.getOverallScore());
//        dto.setWeak(s.isWeak());
//        dto.setStrong(s.isStrong());
//        dto.setLevel(toLevel(s.getOverallScore()));
//        dto.setUpdatedAt(s.getUpdatedAt());
//        return dto;
//    }
//
//    private String toLevel(double score) {
//        if (score >= 70) return "Advanced";
//        if (score >= 40) return "Intermediate";
//        return "Beginner";
//    }
//}
package com.lms.progress.service;

import com.lms.progress.dto.*;
import com.lms.progress.model.SkillScore;
import com.lms.progress.model.Progress;
import com.lms.progress.model.QuizProgress;
import com.lms.progress.model.VideoProgress;
import com.lms.progress.repository.SkillScoreRepository;
import com.lms.progress.repository.ProgressRepository;
import com.lms.progress.repository.QuizProgressRepository;
import com.lms.progress.repository.VideoProgressRepository;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.*;
import java.util.stream.Collectors;

/**
 * SkillMapService — FINAL VERSION
 * ════════════════════════════════════════════════════════════
 *
 * Uses your EXISTING tables exactly as they are:
 *   quiz_progress     → quizScore   (QuizProgress.percentage)
 *   progress          → assignmentScore (Progress.progressPercentage)
 *                       We map "course content progress" as assignment
 *                       because that IS the work students do per course
 *   video_progress    → videoScore  (VideoProgress.watchPercentage)
 *
 * Formula (same as your SkillScore entity):
 *   overall = quiz*0.40 + assignment*0.40 + video*0.20
 *
 * Skill Name logic:
 *   "Batch {batchId} Skills" — one skill per batch
 *   This shows real data immediately.
 *   Later when courses have skill tags, replace deriveSkillName().
 *
 * NO changes needed to:
 *   SkillScore.java          ✅ keep as is
 *   SkillEntryDTO.java       ✅ keep as is
 *   SkillUpsertRequest.java  ✅ keep as is
 *   All other DTOs           ✅ keep as is
 *   SkillScoreRepository     ✅ keep as is
 *   SkillMapController       ✅ keep as is (just add /seed endpoint)
 * ════════════════════════════════════════════════════════════
 */
@Service
public class SkillMapService {

    private final SkillScoreRepository   repo;
    private final ProgressRepository     progressRepo;     // course content progress
    private final QuizProgressRepository quizRepo;         // quiz/assessment progress
    private final VideoProgressRepository videoRepo;       // video watch progress

    public SkillMapService(SkillScoreRepository repo,
                           ProgressRepository progressRepo,
                           QuizProgressRepository quizRepo,
                           VideoProgressRepository videoRepo) {
        this.repo         = repo;
        this.progressRepo = progressRepo;
        this.quizRepo     = quizRepo;
        this.videoRepo    = videoRepo;
    }

    // ══════════════════════════════════════════════════════
    // 1. UPSERT
    //    POST /api/skill-map/upsert
    //    Called from QuizProgressService and VideoProgressService
    //    after every save. Only updates the field that changed.
    // ══════════════════════════════════════════════════════
    public SkillEntryDTO upsertSkill(SkillUpsertRequest req) {

        SkillScore s = repo.findByStudentEmailAndBatchIdAndSkillName(
                req.getStudentEmail(), req.getBatchId(), req.getSkillName())
                .orElseGet(() -> {
                    SkillScore fresh = new SkillScore();
                    fresh.setStudentEmail(req.getStudentEmail());
                    fresh.setBatchId(req.getBatchId());
                    fresh.setTrainerEmail(req.getTrainerEmail());
                    fresh.setSkillName(req.getSkillName());
                    fresh.setQuizScore(0);
                    fresh.setAssignmentScore(0);
                    fresh.setVideoScore(0);
                    fresh.setOverallScore(0);
                    return fresh;
                });

        // only overwrite sub-scores that were actually sent
        if (req.getQuizScore()       != null) s.setQuizScore(req.getQuizScore());
        if (req.getAssignmentScore() != null) s.setAssignmentScore(req.getAssignmentScore());
        if (req.getVideoScore()      != null) s.setVideoScore(req.getVideoScore());
        if (req.getTrainerEmail()    != null) s.setTrainerEmail(req.getTrainerEmail());

        recalcAndSave(s);

        System.out.println("🧠 SkillMap upsert → " + req.getStudentEmail()
                + " | batch=" + req.getBatchId()
                + " | skill=" + req.getSkillName()
                + " | quiz=" + s.getQuizScore()
                + "% assign=" + s.getAssignmentScore()
                + "% video=" + s.getVideoScore()
                + "% → overall=" + s.getOverallScore() + "%");

        return toSkillEntry(s);
    }

    // ══════════════════════════════════════════════════════
    // 2. SEED — reads ALL existing progress tables and
    //    populates skill_scores.
    //    POST /api/skill-map/seed
    //    Safe to call multiple times — upserts, no duplicates.
    //
    //    Mapping:
    //      VideoProgress.watchPercentage  → videoScore
    //      QuizProgress.percentage        → quizScore
    //      Progress.progressPercentage    → assignmentScore
    //        (course content completion = the "work" done = assignment proxy)
    // ══════════════════════════════════════════════════════
    public String seedFromExistingProgress() {
        int count = 0;

        // ── collect all video progress: key = email|batchId ──
        Map<String, VideoProgress> videoMap = new HashMap<>();
        for (VideoProgress vp : videoRepo.findAll()) {
            videoMap.put(key(vp.getStudentEmail(), vp.getBatchId()), vp);
        }

        // ── collect all quiz progress: key = email|batchId ──
        Map<String, QuizProgress> quizMap = new HashMap<>();
        for (QuizProgress qp : quizRepo.findAll()) {
            quizMap.put(key(qp.getStudentEmail(), qp.getBatchId()), qp);
        }

        // ── collect all course progress: key = email (avg across courses) ──
        // Progress uses courseId not batchId, so we group by student email
        // and average all their course progress as the "assignment score"
        Map<String, Double> courseAvgByStudent = new HashMap<>();
        Map<String, List<Progress>> courseByStudent = progressRepo.findAll()
                .stream().collect(Collectors.groupingBy(Progress::getStudentEmail));
        for (Map.Entry<String, List<Progress>> e : courseByStudent.entrySet()) {
            double avg = e.getValue().stream()
                    .mapToDouble(Progress::getProgressPercentage)
                    .average().orElse(0);
            courseAvgByStudent.put(e.getKey(), round(avg));
        }

        // ── get all unique student+batch combos ──
        Set<String> combos = new HashSet<>();
        combos.addAll(videoMap.keySet());
        combos.addAll(quizMap.keySet());

        for (String combo : combos) {
            String[] parts      = combo.split("\\|");
            String studentEmail = parts[0];
            Long   batchId      = Long.parseLong(parts[1]);

            VideoProgress vp = videoMap.get(combo);
            QuizProgress  qp = quizMap.get(combo);

            double videoScore      = vp != null ? vp.getWatchPercentage() : 0;
            double quizScore       = qp != null ? qp.getPercentage()       : 0;
            double assignmentScore = courseAvgByStudent.getOrDefault(studentEmail, 0.0);

            // skill name = one skill per batch
            String skillName = deriveSkillName(batchId);

            SkillUpsertRequest req = new SkillUpsertRequest();
            req.setStudentEmail(studentEmail);
            req.setBatchId(batchId);
            req.setSkillName(skillName);
            req.setQuizScore(quizScore);
            req.setAssignmentScore(assignmentScore);
            req.setVideoScore(videoScore);
            upsertSkill(req);
            count++;
        }

        return "✅ Seeded " + count + " skill records from "
             + "video_progress + quiz_progress + progress tables";
    }

    // ══════════════════════════════════════════════════════
    // 3. STUDENT VIEW
    //    GET /api/skill-map/student?email=x&batchId=5
    //    Auto-seeds if no skill data exists yet for this student
    // ══════════════════════════════════════════════════════
    public StudentSkillMapResponse getStudentSkillMap(String email, Long batchId) {

        List<SkillScore> scores = repo.findByStudentEmailAndBatchId(email, batchId);

        // ── auto-seed if empty ──
        if (scores.isEmpty()) {
            autoSeedStudent(email, batchId);
            scores = repo.findByStudentEmailAndBatchId(email, batchId);
        }

        StudentSkillMapResponse res = new StudentSkillMapResponse();
        res.setStudentEmail(email);
        res.setBatchId(batchId);
        res.setTotalSkills(scores.size());

        long strongCount = scores.stream().filter(SkillScore::isStrong).count();
        long weakCount   = scores.stream().filter(SkillScore::isWeak).count();
        res.setStrongCount((int) strongCount);
        res.setWeakCount((int) weakCount);

        double avg = scores.isEmpty() ? 0 :
                round(scores.stream()
                        .mapToDouble(SkillScore::getOverallScore)
                        .average().orElse(0));
        res.setAvgScore(avg);

        res.setSkills(scores.stream()
                .sorted(Comparator.comparingDouble(SkillScore::getOverallScore).reversed())
                .map(this::toSkillEntry)
                .collect(Collectors.toList()));

        return res;
    }

    // ══════════════════════════════════════════════════════
    // 4. TRAINER VIEW
    //    GET /api/skill-map/trainer/batch?batchId=5
    //    Auto-seeds batch if no skill data exists
    // ══════════════════════════════════════════════════════
    public TrainerBatchSkillResponse getBatchSkillAnalytics(Long batchId) {

        List<SkillScore> all = repo.findByBatchId(batchId);

        // ── auto-seed if empty ──
        if (all.isEmpty()) {
            autoSeedBatch(batchId);
            all = repo.findByBatchId(batchId);
        }

        // group by student
        Map<String, List<SkillScore>> byStudent = all.stream()
                .collect(Collectors.groupingBy(SkillScore::getStudentEmail));

        // group by skill for averages
        Map<String, List<SkillScore>> bySkill = all.stream()
                .collect(Collectors.groupingBy(SkillScore::getSkillName));

        // ── build student rows ──
        List<TrainerStudentSkillRow> studentRows = new ArrayList<>();
        int strongStudents = 0;
        int weakStudents   = 0;

        for (Map.Entry<String, List<SkillScore>> entry : byStudent.entrySet()) {
            List<SkillScore> ss = entry.getValue();

            TrainerStudentSkillRow row = new TrainerStudentSkillRow();
            row.setStudentEmail(entry.getKey());
            row.setAvgScore(round(ss.stream()
                    .mapToDouble(SkillScore::getOverallScore)
                    .average().orElse(0)));

            boolean needsHelp = ss.stream().anyMatch(SkillScore::isWeak);
            boolean allStrong = !ss.isEmpty() && ss.stream().allMatch(SkillScore::isStrong);
            row.setNeedsHelp(needsHelp);

            row.setSkills(ss.stream()
                    .sorted(Comparator.comparingDouble(SkillScore::getOverallScore).reversed())
                    .map(this::toSkillEntry)
                    .collect(Collectors.toList()));

            studentRows.add(row);
            if (allStrong)  strongStudents++;
            if (needsHelp)  weakStudents++;
        }

        // ── build skill averages ──
        List<SkillAvgEntry> skillAverages = new ArrayList<>();
        for (Map.Entry<String, List<SkillScore>> entry : bySkill.entrySet()) {
            double avg = round(entry.getValue().stream()
                    .mapToDouble(SkillScore::getOverallScore)
                    .average().orElse(0));
            SkillAvgEntry sae = new SkillAvgEntry();
            sae.setSkillName(entry.getKey());
            sae.setAvgScore(avg);
            sae.setLevel(toLevel(avg));
            skillAverages.add(sae);
        }
        skillAverages.sort(Comparator.comparing(SkillAvgEntry::getSkillName));

        double batchAvg = all.isEmpty() ? 0 :
                round(all.stream().mapToDouble(SkillScore::getOverallScore).average().orElse(0));

        String trainerEmail = all.stream()
                .map(SkillScore::getTrainerEmail)
                .filter(Objects::nonNull)
                .findFirst().orElse(null);

        TrainerBatchSkillResponse res = new TrainerBatchSkillResponse();
        res.setBatchId(batchId);
        res.setTrainerEmail(trainerEmail);
        res.setTotalStudents(byStudent.size());
        res.setStrongStudents(strongStudents);
        res.setWeakStudents(weakStudents);
        res.setBatchAvgScore(batchAvg);
        res.setSkillAverages(skillAverages);
        res.setStudents(studentRows);
        return res;
    }

    // ══════════════════════════════════════════════════════
    // 5. TRAINER — all batches
    //    GET /api/skill-map/trainer?trainerEmail=x
    // ══════════════════════════════════════════════════════
    public List<TrainerBatchSkillResponse> getTrainerAllBatches(String trainerEmail) {
        List<SkillScore> all = repo.findByTrainerEmail(trainerEmail);
        Set<Long> batchIds = all.stream()
                .map(SkillScore::getBatchId).collect(Collectors.toSet());
        return batchIds.stream()
                .map(this::getBatchSkillAnalytics)
                .sorted(Comparator.comparing(TrainerBatchSkillResponse::getBatchId))
                .collect(Collectors.toList());
    }

    // ══════════════════════════════════════════════════════
    // 6. ADMIN VIEW
    //    GET /api/skill-map/admin/org
    // ══════════════════════════════════════════════════════
    public AdminOrgSkillResponse getOrgSkillDashboard() {

        List<SkillScore> all = repo.findAll();

        Set<String> uniqueStudents = all.stream()
                .map(SkillScore::getStudentEmail).collect(Collectors.toSet());
        Set<Long> uniqueBatches = all.stream()
                .map(SkillScore::getBatchId).collect(Collectors.toSet());

        Map<String, List<SkillScore>> byStudent = all.stream()
                .collect(Collectors.groupingBy(SkillScore::getStudentEmail));

        long totalStrong = byStudent.values().stream()
                .filter(l -> !l.isEmpty() && l.stream().allMatch(SkillScore::isStrong))
                .count();
        long totalWeak = byStudent.values().stream()
                .filter(l -> l.stream().anyMatch(SkillScore::isWeak))
                .count();

        double orgAvg = all.isEmpty() ? 0 :
                round(all.stream().mapToDouble(SkillScore::getOverallScore).average().orElse(0));

        // ── org skill averages ──
        Map<String, List<SkillScore>> bySkill = all.stream()
                .collect(Collectors.groupingBy(SkillScore::getSkillName));

        List<SkillAvgEntry> orgSkillAvg = new ArrayList<>();
        for (Map.Entry<String, List<SkillScore>> e : bySkill.entrySet()) {
            double avg = round(e.getValue().stream()
                    .mapToDouble(SkillScore::getOverallScore).average().orElse(0));
            SkillAvgEntry sae = new SkillAvgEntry();
            sae.setSkillName(e.getKey());
            sae.setAvgScore(avg);
            sae.setLevel(toLevel(avg));
            orgSkillAvg.add(sae);
        }
        orgSkillAvg.sort(Comparator.comparing(SkillAvgEntry::getSkillName));

        // ── batch summary cards ──
        Map<Long, List<SkillScore>> byBatch = all.stream()
                .collect(Collectors.groupingBy(SkillScore::getBatchId));

        List<BatchSummaryEntry> batchSummaries = new ArrayList<>();
        for (Map.Entry<Long, List<SkillScore>> e : byBatch.entrySet()) {
            Long batchId        = e.getKey();
            List<SkillScore> bs = e.getValue();

            Map<String, List<SkillScore>> bsByStudent = bs.stream()
                    .collect(Collectors.groupingBy(SkillScore::getStudentEmail));

            long bStrong = bsByStudent.values().stream()
                    .filter(l -> !l.isEmpty() && l.stream().allMatch(SkillScore::isStrong)).count();
            long bWeak   = bsByStudent.values().stream()
                    .filter(l -> l.stream().anyMatch(SkillScore::isWeak)).count();

            double bAvg = round(bs.stream()
                    .mapToDouble(SkillScore::getOverallScore).average().orElse(0));

            String trainerEmail = bs.stream()
                    .map(SkillScore::getTrainerEmail)
                    .filter(Objects::nonNull)
                    .findFirst().orElse("Unknown");

            BatchSummaryEntry bse = new BatchSummaryEntry();
            bse.setBatchId(batchId);
            bse.setTrainerEmail(trainerEmail);
            bse.setStudents(bsByStudent.size());
            bse.setAvgScore(bAvg);
            bse.setStrongCount((int) bStrong);
            bse.setWeakCount((int) bWeak);
            batchSummaries.add(bse);
        }
        batchSummaries.sort(Comparator.comparing(BatchSummaryEntry::getBatchId));

        // ── by-batch skill matrix ──
        Map<String, Map<Long, List<Double>>> matrix = new HashMap<>();
        for (SkillScore ss : all) {
            matrix.computeIfAbsent(ss.getSkillName(), k -> new HashMap<>())
                  .computeIfAbsent(ss.getBatchId(), k -> new ArrayList<>())
                  .add(ss.getOverallScore());
        }

        List<BatchSkillMatrix> batchSkillMatrix = new ArrayList<>();
        for (Map.Entry<String, Map<Long, List<Double>>> skillEntry : matrix.entrySet()) {
            BatchSkillMatrix bsm = new BatchSkillMatrix();
            bsm.setSkillName(skillEntry.getKey());
            Map<Long, Double> batchScores = new LinkedHashMap<>();
            for (Map.Entry<Long, List<Double>> batchEntry : skillEntry.getValue().entrySet()) {
                batchScores.put(batchEntry.getKey(),
                        round(batchEntry.getValue().stream()
                                .mapToDouble(Double::doubleValue).average().orElse(0)));
            }
            bsm.setBatchScores(batchScores);
            batchSkillMatrix.add(bsm);
        }
        batchSkillMatrix.sort(Comparator.comparing(BatchSkillMatrix::getSkillName));

        AdminOrgSkillResponse res = new AdminOrgSkillResponse();
        res.setTotalStudents(uniqueStudents.size());
        res.setOrgAvgScore(orgAvg);
        res.setTotalStrongLearners((int) totalStrong);
        res.setTotalNeedAttention((int) totalWeak);
        res.setActiveBatches(uniqueBatches.size());
        res.setOrgSkillAverages(orgSkillAvg);
        res.setBatchSummaries(batchSummaries);
        res.setBatchSkillMatrix(batchSkillMatrix);
        return res;
    }

    // ══════════════════════════════════════════════════════
    // AUTO-SEED HELPERS
    // Called when a student/batch has no skill_scores yet.
    // Reads from existing tables and creates records.
    // ══════════════════════════════════════════════════════

    private void autoSeedStudent(String email, Long batchId) {
        double videoScore = videoRepo
                .findByStudentEmailAndBatchId(email, batchId)
                .map(VideoProgress::getWatchPercentage)
                .orElse(0.0);

        double quizScore = quizRepo
                .findByStudentEmailAndBatchId(email, batchId)
                .map(QuizProgress::getPercentage)
                .orElse(0.0);

        // average of all course progress for this student
        double assignmentScore = progressRepo
                .findByStudentEmail(email).stream()
                .mapToDouble(Progress::getProgressPercentage)
                .average().orElse(0.0);

        SkillUpsertRequest req = new SkillUpsertRequest();
        req.setStudentEmail(email);
        req.setBatchId(batchId);
        req.setSkillName(deriveSkillName(batchId));
        req.setQuizScore(quizScore);
        req.setAssignmentScore(assignmentScore);
        req.setVideoScore(videoScore);
        upsertSkill(req);
    }

    private void autoSeedBatch(Long batchId) {
        // collect all students in this batch from video + quiz tables
        Set<String> students = new HashSet<>();
        videoRepo.findByBatchId(batchId).forEach(v -> students.add(v.getStudentEmail()));
        quizRepo.findByBatchId(batchId).forEach(q -> students.add(q.getStudentEmail()));
        for (String email : students) {
            autoSeedStudent(email, batchId);
        }
    }

    // ══════════════════════════════════════════════════════
    // PRIVATE HELPERS
    // ══════════════════════════════════════════════════════

    private void recalcAndSave(SkillScore s) {
        // quiz 40% + assignment(course) 40% + video 20%
        double overall = (s.getQuizScore()       * 0.40)
                       + (s.getAssignmentScore() * 0.40)
                       + (s.getVideoScore()      * 0.20);
        overall = Math.min(round(overall), 100.0);
        s.setOverallScore(overall);
        s.setWeak(overall < 50);
        s.setStrong(overall >= 70);
        s.setUpdatedAt(Instant.now());
        repo.save(s);
    }

    private SkillEntryDTO toSkillEntry(SkillScore s) {
        SkillEntryDTO dto = new SkillEntryDTO();
        dto.setSkillName(s.getSkillName());
        dto.setQuizScore(s.getQuizScore());
        dto.setAssignmentScore(s.getAssignmentScore());
        dto.setVideoScore(s.getVideoScore());
        dto.setOverallScore(s.getOverallScore());
        dto.setWeak(s.isWeak());
        dto.setStrong(s.isStrong());
        dto.setLevel(toLevel(s.getOverallScore()));
        dto.setUpdatedAt(s.getUpdatedAt());
        return dto;
    }

    private String toLevel(double score) {
        if (score >= 70) return "Advanced";
        if (score >= 40) return "Intermediate";
        return "Beginner";
    }

    private double round(double v) {
        return Math.round(v * 10.0) / 10.0;
    }

    private String key(String email, Long batchId) {
        return email + "|" + batchId;
    }

    /**
     * Skill name per batch.
     * Right now: "Batch 55 Skills"
     * Later: fetch course name / skill tag from Course Service
     * and pass it in via upsert instead.
     */
    private String deriveSkillName(Long batchId) {
        return "Batch " + batchId + " Skills";
    }
}