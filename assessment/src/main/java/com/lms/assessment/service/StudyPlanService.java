

package com.lms.assessment.service;

import com.lms.assessment.dto.StudyPlanRequest;
import com.lms.assessment.dto.StudyPlanResponse;
import com.lms.assessment.model.*;
import com.lms.assessment.repository.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.Comparator;
import com.lms.assessment.dto.StudyPlanAdminReportResponse;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

@Service
public class StudyPlanService {

    private static final Logger log = LoggerFactory.getLogger(StudyPlanService.class);

    private final StudyPlanRepository          studyPlanRepository;
    private final StudyPlanSectionRepository   sectionRepository;
    private final StudyPlanItemRepository      itemRepository;
    private final StudyPlanProgressRepository  progressRepository;
    private final CodingProblemRepository      problemRepository;

    public StudyPlanService(StudyPlanRepository studyPlanRepository,
                            StudyPlanSectionRepository sectionRepository,
                            StudyPlanItemRepository itemRepository,
                            StudyPlanProgressRepository progressRepository,
                            CodingProblemRepository problemRepository) {
        this.studyPlanRepository = studyPlanRepository;
        this.sectionRepository   = sectionRepository;
        this.itemRepository      = itemRepository;
        this.progressRepository  = progressRepository;
        this.problemRepository   = problemRepository;
    }

    /* ─────────────────────────────────────────────
       TRAINER: CREATE
       ───────────────────────────────────────────── */

    @Transactional
    public StudyPlanResponse createStudyPlan(StudyPlanRequest req, String trainerEmail, String organizationId) {
        StudyPlan plan = new StudyPlan();
        plan.setTitle(req.getTitle());
        plan.setDescription(req.getDescription());
        plan.setTrainerEmail(trainerEmail);
        plan.setBatchId(req.getBatchId());
        plan.setThumbnailColor(req.getThumbnailColor() != null ? req.getThumbnailColor() : "#6366f1");
        plan.setIcon(req.getIcon() != null ? req.getIcon() : "📘");
        plan.setActive(true);
        plan.setOrganizationId(organizationId); // null for standalone trainers — expected
        if (req.getDueDate() != null && !req.getDueDate().isBlank()) {
            plan.setDueDate(LocalDateTime.parse(req.getDueDate()));
        }

        StudyPlan saved = studyPlanRepository.save(plan);

        if (req.getSections() != null) {
            for (StudyPlanRequest.SectionRequest sr : req.getSections()) {
                saveSection(saved, sr);
            }
        }

        log.info("StudyPlan created: id={} trainer={} batch={} org={}", saved.getId(), trainerEmail, req.getBatchId(), organizationId);
        return buildResponse(saved, null);
    }

    /* ─────────────────────────────────────────────
       TRAINER: UPDATE
       ───────────────────────────────────────────── */

    @Transactional
    public StudyPlanResponse updateStudyPlan(Long planId, StudyPlanRequest req, String trainerEmail, String organizationId) {
        StudyPlan plan = findOwnedPlan(planId, trainerEmail, organizationId);

        plan.setTitle(req.getTitle());
        plan.setDescription(req.getDescription());
        plan.setBatchId(req.getBatchId());
        if (req.getThumbnailColor() != null) plan.setThumbnailColor(req.getThumbnailColor());
        if (req.getIcon() != null) plan.setIcon(req.getIcon());
        if (req.getDueDate() != null && !req.getDueDate().isBlank()) {
            plan.setDueDate(LocalDateTime.parse(req.getDueDate()));
        }

        plan.getSections().clear();
        studyPlanRepository.save(plan);

        if (req.getSections() != null) {
            for (StudyPlanRequest.SectionRequest sr : req.getSections()) {
                saveSection(plan, sr);
            }
        }

        StudyPlan updated = studyPlanRepository.save(plan);
        log.info("StudyPlan updated: id={} trainer={}", planId, trainerEmail);
        return buildResponse(updated, null);
    }

    /* ─────────────────────────────────────────────
       TRAINER: DELETE
       ───────────────────────────────────────────── */

    @Transactional
    public void deleteStudyPlan(Long planId, String trainerEmail, String organizationId) {
        StudyPlan plan = findOwnedPlan(planId, trainerEmail, organizationId);
        studyPlanRepository.delete(plan);
        log.info("StudyPlan deleted: id={} trainer={}", planId, trainerEmail);
    }

    /* ─────────────────────────────────────────────
       TRAINER: GET MY PLANS
       ───────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<StudyPlanResponse> getMyPlans(String trainerEmail, String organizationId) {
        List<StudyPlan> plans = organizationId == null
                ? studyPlanRepository.findByTrainerEmailOrderByCreatedAtDesc(trainerEmail)
                : studyPlanRepository.findByOrganizationIdAndTrainerEmailOrderByCreatedAtDesc(organizationId, trainerEmail);

        return plans.stream()
                .map(p -> buildResponse(p, null))
                .collect(Collectors.toList());
    }

    /* ─────────────────────────────────────────────
       TRAINER: GET SINGLE PLAN
       ───────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public StudyPlanResponse getPlanById(Long planId, String trainerEmail, String organizationId) {
        StudyPlan plan = findOwnedPlan(planId, trainerEmail, organizationId);
        return buildResponse(plan, null);
    }

    /* ─────────────────────────────────────────────
       STUDENT: GET ASSIGNED PLANS FOR BATCH
       ───────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public List<StudyPlanResponse> getStudentPlans(Long batchId, String studentEmail, String organizationId) {
        List<StudyPlan> plans = organizationId == null
                ? studyPlanRepository.findByBatchIdAndActiveOrderByCreatedAtDesc(batchId, true)
                : studyPlanRepository.findByOrganizationIdAndBatchIdAndActiveOrderByCreatedAtDesc(organizationId, batchId, true);

        return plans.stream()
                .map(p -> buildResponse(p, studentEmail))
                .collect(Collectors.toList());
    }

    /* ─────────────────────────────────────────────
       STUDENT: GET SINGLE PLAN WITH PROGRESS
       ───────────────────────────────────────────── */

    @Transactional(readOnly = true)
    public StudyPlanResponse getStudentPlanById(Long planId, String studentEmail, String organizationId) {
        StudyPlan plan = studyPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Study plan not found: " + planId));

        assertSameOrg(plan.getOrganizationId(), organizationId);

        return buildResponse(plan, studentEmail);
    }

    /* ─────────────────────────────────────────────
       STUDENT: MARK ITEM COMPLETE
       ───────────────────────────────────────────── */

    @Transactional
    public void markItemComplete(Long studyPlanItemId, String studentEmail,
                                  Long batchId, Long problemId, int marksObtained) {
        Optional<StudyPlanProgress> existing =
                progressRepository.findByStudyPlanItemIdAndStudentEmail(studyPlanItemId, studentEmail);

        if (existing.isPresent()) {
            StudyPlanProgress prog = existing.get();
            if (!prog.isCompleted()) {
                prog.setCompleted(true);
                prog.setMarksObtained(marksObtained);
                prog.setCompletedAt(LocalDateTime.now());
                progressRepository.save(prog);
            }
            return;
        }

        StudyPlanItem item = itemRepository.findById(studyPlanItemId)
                .orElseThrow(() -> new RuntimeException("StudyPlanItem not found: " + studyPlanItemId));

        StudyPlanProgress progress = new StudyPlanProgress();
        progress.setStudyPlanItemId(studyPlanItemId);
        progress.setStudyPlanId(item.getSection().getStudyPlan().getId());
        progress.setStudentEmail(studentEmail);
        progress.setBatchId(batchId);
        progress.setProblemId(problemId);
        progress.setCompleted(true);
        progress.setMarksObtained(marksObtained);
        progress.setCompletedAt(LocalDateTime.now());
        progressRepository.save(progress);

        log.info("StudyPlan item completed: itemId={} student={} marks={}", studyPlanItemId, studentEmail, marksObtained);
    }

    /* ─────────────────────────────────────────────
       STUDENT: AUTO-MARK via problemId (called from judge hook)
       ───────────────────────────────────────────── */

    @Transactional
    public void autoMarkProgressByProblem(Long problemId, String studentEmail,
                                           Long batchId, int marksObtained) {
        List<StudyPlanItem> items = itemRepository.findByProblemId(problemId);
        for (StudyPlanItem item : items) {
            StudyPlan plan = item.getSection().getStudyPlan();
            if (plan.getBatchId() != null && plan.getBatchId().equals(batchId) && plan.isActive()) {
                markItemComplete(item.getId(), studentEmail, batchId, problemId, marksObtained);
            }
        }
    }

    /* ─────────────────────────────────────────────
       TRAINER: TOGGLE ACTIVE
       ───────────────────────────────────────────── */

    @Transactional
    public StudyPlanResponse toggleActive(Long planId, String trainerEmail, String organizationId) {
        StudyPlan plan = findOwnedPlan(planId, trainerEmail, organizationId);
        plan.setActive(!plan.isActive());
        return buildResponse(studyPlanRepository.save(plan), null);
    }

    /* ─────────────────────────────────────────────
       PRIVATE HELPERS
       ───────────────────────────────────────────── */

    private StudyPlan findOwnedPlan(Long planId, String trainerEmail, String organizationId) {
        StudyPlan plan = studyPlanRepository.findByIdAndTrainerEmail(planId, trainerEmail)
                .orElseThrow(() -> new RuntimeException("Study plan not found or not owned by trainer: " + planId));
        assertSameOrg(plan.getOrganizationId(), organizationId);
        return plan;
    }

    private void assertSameOrg(String resourceOrgId, String callerOrgId) {
        if (callerOrgId == null) return;
        if (!callerOrgId.equals(resourceOrgId)) {
            throw new RuntimeException("Access denied: study plan belongs to a different organization");
        }
    }

    private void saveSection(StudyPlan plan, StudyPlanRequest.SectionRequest sr) {
        StudyPlanSection section = new StudyPlanSection();
        section.setStudyPlan(plan);
        section.setTitle(sr.getTitle());
        section.setDescription(sr.getDescription());
        section.setOrderIndex(sr.getOrderIndex());
        StudyPlanSection savedSection = sectionRepository.save(section);

        if (sr.getItems() != null) {
            for (StudyPlanRequest.ItemRequest ir : sr.getItems()) {
                saveItem(savedSection, ir);
            }
        }
    }

    private void saveItem(StudyPlanSection section, StudyPlanRequest.ItemRequest ir) {
        StudyPlanItem item = new StudyPlanItem();
        item.setSection(section);
        item.setProblemId(ir.getProblemId());
        item.setOrderIndex(ir.getOrderIndex());

        problemRepository.findById(ir.getProblemId()).ifPresent(p -> {
            item.setProblemTitle(p.getTitle());
            item.setProblemDifficulty(p.getDifficulty() != null ? p.getDifficulty().name() : null);
            item.setProblemTotalMarks(p.getTotalMarks());
        });

        itemRepository.save(item);
    }

    private StudyPlanResponse buildResponse(StudyPlan plan, String studentEmail) {
        StudyPlanResponse resp = new StudyPlanResponse();
        resp.setId(plan.getId());
        resp.setTitle(plan.getTitle());
        resp.setDescription(plan.getDescription());
        resp.setTrainerEmail(plan.getTrainerEmail());
        resp.setBatchId(plan.getBatchId());
        resp.setThumbnailColor(plan.getThumbnailColor());
        resp.setIcon(plan.getIcon());
        resp.setActive(plan.isActive());
        resp.setDueDate(plan.getDueDate());
        resp.setCreatedAt(plan.getCreatedAt());
        resp.setUpdatedAt(plan.getUpdatedAt());

        List<StudyPlanSection> sections = sectionRepository
                .findByStudyPlanIdOrderByOrderIndexAsc(plan.getId());

        int totalProblems = 0;
        int completedProblems = 0;

        Map<Long, StudyPlanProgress> progressMap = new java.util.HashMap<>();
        if (studentEmail != null) {
            progressRepository
                .findByStudyPlanIdAndStudentEmail(plan.getId(), studentEmail)
                .forEach(p -> progressMap.put(p.getStudyPlanItemId(), p));
        }

        List<StudyPlanResponse.SectionResponse> sectionResponses = new ArrayList<>();
        for (StudyPlanSection section : sections) {
            StudyPlanResponse.SectionResponse sr = new StudyPlanResponse.SectionResponse();
            sr.setId(section.getId());
            sr.setTitle(section.getTitle());
            sr.setDescription(section.getDescription());
            sr.setOrderIndex(section.getOrderIndex());

            List<StudyPlanItem> items = itemRepository.findBySectionIdOrderByOrderIndexAsc(section.getId());
            List<StudyPlanResponse.ItemResponse> itemResponses = new ArrayList<>();

            for (StudyPlanItem item : items) {
                totalProblems++;
                StudyPlanResponse.ItemResponse ir = new StudyPlanResponse.ItemResponse();
                ir.setId(item.getId());
                ir.setProblemId(item.getProblemId());
                ir.setProblemTitle(item.getProblemTitle());
                ir.setProblemDifficulty(item.getProblemDifficulty());
                ir.setProblemTotalMarks(item.getProblemTotalMarks());
                ir.setOrderIndex(item.getOrderIndex());

                StudyPlanProgress prog = progressMap.get(item.getId());
                if (prog != null && prog.isCompleted()) {
                    ir.setCompleted(true);
                    ir.setMarksObtained(prog.getMarksObtained());
                    completedProblems++;
                } else {
                    ir.setCompleted(false);
                }
                itemResponses.add(ir);
            }
            sr.setItems(itemResponses);
            sectionResponses.add(sr);
        }

        resp.setSections(sectionResponses);
        resp.setTotalProblems(totalProblems);
        resp.setCompletedProblems(completedProblems);
        return resp;
    }
    @Transactional(readOnly = true)
    public List<StudyPlanAdminReportResponse> getAdminReport(String organizationId) {
        List<StudyPlan> plans = studyPlanRepository.findByOrganizationId(organizationId);
        return plans.stream()
                .sorted(Comparator.comparing(StudyPlan::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toAdminReport)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<StudyPlanAdminReportResponse> getSuperAdminReport() {
        List<StudyPlan> plans = studyPlanRepository.findByOrganizationIdIsNull();
        return plans.stream()
                .sorted(Comparator.comparing(StudyPlan::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .map(this::toAdminReport)
                .collect(Collectors.toList());
    }

    private StudyPlanAdminReportResponse toAdminReport(StudyPlan plan) {
        List<StudyPlanSection> sections = sectionRepository.findByStudyPlanIdOrderByOrderIndexAsc(plan.getId());
        int itemCount = 0;
        for (StudyPlanSection section : sections) {
            itemCount += itemRepository.findBySectionIdOrderByOrderIndexAsc(section.getId()).size();
        }

        return new StudyPlanAdminReportResponse(
                plan.getId(),
                plan.getTitle(),
                plan.getTrainerEmail(),
                plan.getBatchId(),
                plan.isActive(),
                plan.getDueDate(),
                plan.getCreatedAt(),
                itemCount,
                plan.getOrganizationId() != null ? plan.getOrganizationId() : "Standalone"
        );
    }
    @Transactional(readOnly = true)
    public StudyPlanResponse getPlanItemsForAdmin(Long planId, String organizationId) {
        StudyPlan plan = studyPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Study plan not found: " + planId));
        assertSameOrg(plan.getOrganizationId(), organizationId);
        return buildResponse(plan, null);
    }
    
    @Transactional(readOnly = true)
    public StudyPlanResponse getPlanItemsForSuperAdmin(Long planId) {
        StudyPlan plan = studyPlanRepository.findById(planId)
                .orElseThrow(() -> new RuntimeException("Study plan not found: " + planId));
        if (plan.getOrganizationId() != null) {
            throw new RuntimeException("This plan does not belong to a standalone/super-admin scope");
        }
        return buildResponse(plan, null);
    }
}