package com.lms.batch.kafka;

import com.lms.batch.entity.BatchTrainerStudent;

import com.lms.batch.kafka.BatchAssignmentProducer;
import com.lms.batch.kafka.BatchLifecycleProducer;
import com.lms.batch.repository.BatchTrainerStudentRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import com.lms.batch.entity.OrgLimits;
import com.lms.batch.repository.OrgLimitsRepository;
@Component
public class AuthEventConsumer {

    private final BatchTrainerStudentRepository mappingRepo;
    private final BatchAssignmentProducer       assignmentProducer;
    private final BatchLifecycleProducer        lifecycleProducer;
    private final OrgLimitsRepository  orgLimitsRepository;

    public AuthEventConsumer(
            BatchTrainerStudentRepository mappingRepo,
            BatchAssignmentProducer assignmentProducer,
            BatchLifecycleProducer lifecycleProducer,
            OrgLimitsRepository  orgLimitsRepository
    ) {
        this.mappingRepo        = mappingRepo;
        this.assignmentProducer = assignmentProducer;
        this.lifecycleProducer  = lifecycleProducer;
        this.orgLimitsRepository=orgLimitsRepository;
    }

    @KafkaListener(topics = "auth-events", groupId = "batch-service-group")
    @Transactional
    public void handleAuthEvent(Map<String, Object> event) {

        if (event == null || event.get("eventType") == null) return;

        String eventType = event.get("eventType").toString();

        switch (eventType) {

            // ── ORG_CREATED ──────────────────────────────────────────────
            // Fired when:
            //   1. Super admin creates org via OrganizationService
            //   2. TENANT_ADMIN completes Google onboarding → auto-org created
            // orgId is always present here.
            // batch-service has no action needed — org context flows through
            // Department → Branch → Batch hierarchy, not directly here.
            case "ORG_CREATED" -> {
                String orgId = getString(event, "organizationId");
                String name  = getString(event, "displayName");
                String email = getString(event, "email");
                System.out.println("📥 AUTH EVENT: ORG_CREATED"
                        + " | orgId=" + orgId
                        + " | name="  + name
                        + " | email=" + email);
                // No action needed in batch-service for org creation.
                // orgId enters batch-service when admin creates Department with orgId.
            }
            case "ORG_LIMITS_UPDATED" -> {
                String orgId = getString(event, "organizationId");
                Integer maxDept    = getInt(event, "maxDepartments");
                Integer maxBranch  = getInt(event, "maxBranchesPerDept");
                Integer maxBatch   = getInt(event, "maxBatchesPerBranch");

                OrgLimits limits = orgLimitsRepository
                    .findById(orgId)
                    .orElse(new OrgLimits());
                
                limits.setOrganizationId(orgId);
                limits.setMaxDepartments(maxDept);
                limits.setMaxBranchesPerDept(maxBranch);
                limits.setMaxBatchesPerBranch(maxBatch);
                
                orgLimitsRepository.save(limits);
                System.out.println("✅ ORG LIMITS SAVED -> " + orgId);
            }
            case "ORG_UPDATED" -> {
                String orgId      = getString(event, "organizationId");
                Integer maxDept   = getInt(event, "maxDepartments");
                Integer maxBranch = getInt(event, "maxBranchesPerDept");
                Integer maxBatch  = getInt(event, "maxBatchesPerBranch");

                if (orgId == null) {
                    System.out.println("⚠️ ORG_UPDATED received with no orgId — skipping");
                    return;
                }

                OrgLimits limits = orgLimitsRepository
                    .findById(orgId)
                    .orElse(new OrgLimits());

                limits.setOrganizationId(orgId);
                if (maxDept   != null) limits.setMaxDepartments(maxDept);
                if (maxBranch != null) limits.setMaxBranchesPerDept(maxBranch);
                if (maxBatch  != null) limits.setMaxBatchesPerBranch(maxBatch);

                orgLimitsRepository.save(limits);
                System.out.println("✅ ORG LIMITS UPDATED (via ORG_UPDATED) -> " + orgId);
            }
            // ── USER_CREATED ─────────────────────────────────────────────
            // Fired when:
            //   1. Admin registers trainer/student → orgId is PRESENT
            //   2. Google login student/trainer (new user) → orgId is NULL
            //   3. TENANT_ADMIN google onboarding → orgId is PRESENT (after org auto-create)
            // batch-service has no action needed on creation —
            // assignment happens separately via assignTrainer / assignStudents APIs.
            case "USER_CREATED" -> {
                String email = getString(event, "email");
                String role  = getString(event, "role");
                String orgId = getString(event, "organizationId"); // may be null
                System.out.println("📥 AUTH EVENT: USER_CREATED"
                        + " | email=" + email
                        + " | role="  + role
                        + " | orgId=" + orgId
                        + (orgId == null ? " [standalone — no org]" : " [org user]"));
                // No action needed. Batch assignment is done by admin explicitly.
            }

            // ── USER_DELETED ─────────────────────────────────────────────
            // Fired when:
            //   1. deleteUser(userId) called directly → orgId is NULL in event
            //   2. deleteOrganization() loops users → orgId IS present in event
            // In BOTH cases, batch-service must clean up all mappings for this user
            // regardless of whether orgId is present or not.
            case "USER_DELETED" -> {
                String email = getString(event, "email");
                String role  = getString(event, "role");
                String orgId = getString(event, "organizationId"); // may be null — handle both

                if (email == null) {
                    System.out.println("⚠️ USER_DELETED received with no email — skipping");
                    return;
                }

                System.out.println("📥 AUTH EVENT: USER_DELETED"
                        + " | email=" + email
                        + " | role="  + role
                        + " | orgId=" + orgId
                        + (orgId == null ? " [standalone delete]" : " [org deletion cascade]"));

                if ("TRAINER".equalsIgnoreCase(role)) {
                    handleTrainerDeleted(email);
                } else if ("STUDENT".equalsIgnoreCase(role)) {
                    handleStudentDeleted(email);
                } else {
                    // TENANT_ADMIN / SUPER_ADMIN / BUSINESS — no batch mappings
                    System.out.println("ℹ️ USER_DELETED role=" + role
                            + " — no batch mappings to clean");
                }
            }

            default -> System.out.println(
                    "📥 AUTH EVENT (unhandled type): " + eventType);
        }
    }

    // ── TRAINER DELETED ──────────────────────────────────────────────────────
    // Remove all trainer mappings across all batches.
    // Fire TRAINER_REMOVED for each batch they were in.
    // Fire STUDENT_REMOVED for every student under them in each batch,
    // so course-service / attendance-service clean up those students too.
    private void handleTrainerDeleted(String trainerEmail) {

        // Find ALL mappings where this trainer appears
        List<BatchTrainerStudent> mappings =
                mappingRepo.findByTrainerEmail(trainerEmail);

        if (mappings.isEmpty()) {
            System.out.println("ℹ️ No batch mappings found for trainer: " + trainerEmail);
            lifecycleProducer.trainerDeleted(trainerEmail);
            return;
        }

        // Group by batchId so we fire one TRAINER_REMOVED per batch
        Map<Long, List<BatchTrainerStudent>> byBatch = mappings.stream()
                .collect(Collectors.groupingBy(BatchTrainerStudent::getBatchId));

        for (Map.Entry<Long, List<BatchTrainerStudent>> entry : byBatch.entrySet()) {

            Long batchId = entry.getKey();
            List<BatchTrainerStudent> batchMappings = entry.getValue();

            // orgId may be null for standalone trainer — pass as-is
            // (downstream services handle null orgId gracefully)
            String orgId = null; // standalone trainer delete has no orgId

            // Fire STUDENT_REMOVED for all real students under this trainer in this batch
            for (BatchTrainerStudent m : batchMappings) {
                if (m.getStudentEmail() != null
                        && !m.getStudentEmail().equals("__EMPTY__")) {
                    assignmentProducer.studentRemoved(
                            m.getStudentEmail(), batchId, orgId);
                }
            }

            // Fire TRAINER_REMOVED for this batch
            assignmentProducer.trainerRemoved(trainerEmail, batchId, orgId);
        }

        // Delete all mappings from DB
        mappingRepo.deleteAll(mappings);

        // Fire global lifecycle event (course-service etc. can also listen)
        lifecycleProducer.trainerDeleted(trainerEmail);

        System.out.println("✅ TRAINER DELETED CLEANUP DONE -> " + trainerEmail
                + " | batches cleaned=" + byBatch.size());
    }

    // ── STUDENT DELETED ──────────────────────────────────────────────────────
    // Remove all student mappings across all batches.
    // Fire STUDENT_REMOVED for each batch they were in.
    private void handleStudentDeleted(String studentEmail) {

        List<BatchTrainerStudent> mappings =
                mappingRepo.findByStudentEmail(studentEmail);

        if (mappings.isEmpty()) {
            System.out.println("ℹ️ No batch mappings found for student: " + studentEmail);
            lifecycleProducer.studentDeleted(studentEmail);
            return;
        }

        for (BatchTrainerStudent m : mappings) {
            // orgId may be null for standalone student — pass as-is
            assignmentProducer.studentRemoved(
                    m.getStudentEmail(), m.getBatchId(), null);
        }

        mappingRepo.deleteAll(mappings);

        lifecycleProducer.studentDeleted(studentEmail);

        System.out.println("✅ STUDENT DELETED CLEANUP DONE -> " + studentEmail
                + " | mappings removed=" + mappings.size());
    }

    // ── HELPER ───────────────────────────────────────────────────────────────
    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }
 // ADD THIS — was missing, causes compile error
    private Integer getInt(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof Integer) return (Integer) val;
        try {
            return Integer.parseInt(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
    
}