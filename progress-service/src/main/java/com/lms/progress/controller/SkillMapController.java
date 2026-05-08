package com.lms.progress.controller;

import com.lms.progress.dto.*;
import com.lms.progress.service.SkillMapService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/skill-map")
public class SkillMapController {

    private final SkillMapService service;

    public SkillMapController(SkillMapService service) {
        this.service = service;
    }

    // ══════════════════════════════════════════════════════
    // STUDENT ENDPOINTS
    // Used by: SkillMap.jsx (student view)
    // ══════════════════════════════════════════════════════

    /**
     * GET /api/skill-map/student?email=student@demo.com&batchId=5
     *
     * Returns full skill breakdown for one student in one batch.
     * Frontend uses this to populate:
     *   - Summary stat cards (avgScore, strongCount, weakCount, totalSkills)
     *   - SkillRadarChart
     *   - WeakSkillsSection
     *   - SkillCard list (Top Skills tab)
     */
    @GetMapping("/student")
    public ResponseEntity<StudentSkillMapResponse> getStudentSkillMap(
            @RequestParam String email,
            @RequestParam Long   batchId) {

        StudentSkillMapResponse response = service.getStudentSkillMap(email, batchId);
        return ResponseEntity.ok(response);
    }

    // ══════════════════════════════════════════════════════
    // TRAINER ENDPOINTS
    // Used by: TrainerSkillMap.jsx
    // ══════════════════════════════════════════════════════

    /**
     * GET /api/skill-map/trainer/batch?batchId=5
     *
     * Returns batch-level analytics including:
     *   - Per-student skill rows (Students tab)
     *   - Skill averages for Radar + Bar chart (Overview tab)
     *   - Weak students list (Weak Areas tab)
     *   - Summary stats: totalStudents, avgScore, strongStudents, weakStudents
     */
    @GetMapping("/trainer/batch")
    public ResponseEntity<TrainerBatchSkillResponse> getBatchSkillAnalytics(
            @RequestParam Long batchId) {

        TrainerBatchSkillResponse response = service.getBatchSkillAnalytics(batchId);
        return ResponseEntity.ok(response);
    }

    /**
     * GET /api/skill-map/trainer?trainerEmail=trainer@demo.com
     *
     * Returns all batches owned by this trainer.
     * Useful when trainer manages multiple batches.
     * Each item in list is same shape as /trainer/batch response.
     */
    @GetMapping("/trainer")
    public ResponseEntity<List<TrainerBatchSkillResponse>> getTrainerAllBatches(
            @RequestParam String trainerEmail) {

        List<TrainerBatchSkillResponse> response = service.getTrainerAllBatches(trainerEmail);
        return ResponseEntity.ok(response);
    }

    // ══════════════════════════════════════════════════════
    // ADMIN / MANAGER ENDPOINTS
    // Used by: AdminSkillDashboard.jsx
    // ══════════════════════════════════════════════════════

    /**
     * GET /api/skill-map/admin/org
     *
     * Returns org-wide skill intelligence:
     *   - Summary stats: totalStudents, orgAvgScore, strongLearners, needAttention, activeBatches
     *   - Org Skill Distribution (radar chart data)
     *   - Org Skill Averages (progress bars)
     *   - Batch summary cards (Overview tab)
     *   - Batch × Skill matrix (By Batch tab bar chart)
     */
    @GetMapping("/admin/org")
    public ResponseEntity<AdminOrgSkillResponse> getOrgSkillDashboard() {

        AdminOrgSkillResponse response = service.getOrgSkillDashboard();
        return ResponseEntity.ok(response);
    }

    // ══════════════════════════════════════════════════════
    // UPSERT ENDPOINT
    // Called internally by other services when scores change:
    //   - QuizProgressService  → after quiz submitted
    //   - AssignmentProgressService → after assignment graded
    //   - VideoProgressService → after all videos watched
    // ══════════════════════════════════════════════════════

    /**
     * POST /api/skill-map/upsert
     *
     * Creates or updates one skill score record.
     * Only the sub-scores provided in the request body are updated
     * (null = keep existing value).
     * Overall score recalculated automatically:
     *   overall = quiz*0.4 + assignment*0.4 + video*0.2
     *
     * Request body example:
     * {
     *   "studentEmail": "student@demo.com",
     *   "batchId": 5,
     *   "trainerEmail": "trainer@demo.com",
     *   "skillName": "JavaScript",
     *   "quizScore": 75.0,       // nullable
     *   "assignmentScore": null, // not changed
     *   "videoScore": null       // not changed
     * }
     */
    @PostMapping("/upsert")
    public ResponseEntity<SkillEntryDTO> upsertSkill(
            @RequestBody SkillUpsertRequest request) {

        SkillEntryDTO response = service.upsertSkill(request);
        return ResponseEntity.ok(response);
    }
    @PostMapping("/seed")
    public ResponseEntity<String> seedSkillScores() {
        return ResponseEntity.ok(service.seedFromExistingProgress());
    }
}