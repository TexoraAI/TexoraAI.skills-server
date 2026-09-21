package com.lms.auth.consumer;

import com.lms.auth.constants.IndividualPlanLimits;
import com.lms.auth.constants.PlanLimits;
import com.lms.auth.event.AuthEvent;
import com.lms.auth.model.Organization;
import com.lms.auth.model.User;
import com.lms.auth.producer.AuthEventProducer;
import com.lms.auth.repository.OrganizationRepository;
import com.lms.auth.repository.UserRepository;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.time.LocalDate;

@Component
public class PaymentEventConsumer {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final AuthEventProducer authEventProducer;

    public PaymentEventConsumer(
            OrganizationRepository organizationRepository,
            UserRepository userRepository,
            AuthEventProducer authEventProducer
    ) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.authEventProducer = authEventProducer;
    }

    @KafkaListener(topics = "payment-events", groupId = "auth-service-group")
    @Transactional
    public void handlePaymentEvent(Map<String, Object> event) {

        if (event == null) return;

        String purpose = getString(event, "purpose");

        if ("PLAN_UPGRADE".equals(purpose)) {
            String orgId = getString(event, "orgId");
            if (orgId != null && !orgId.isBlank()) {
                handleOrgPlanUpgrade(event, orgId);
            } else {
                handleIndividualPlanUpgrade(event);
            }
            return;
        }

        if ("RESUME_PLAN_UPGRADE".equals(purpose)) {
            handleResumePlanUpgrade(event);
            return;
        }

        // Ignore COURSE_PURCHASE, WALLET_TOPUP, ORG_COURSE_LICENSE, SEAT_UPGRADE, etc.
    }

    private void handleOrgPlanUpgrade(Map<String, Object> event, String orgId) {
        String referenceId = getString(event, "referenceId");
        if (referenceId == null || referenceId.isBlank()) {
            System.out.println("⚠️ PLAN_UPGRADE (org) — missing referenceId, skipping");
            return;
        }
        String[] parts = referenceId.split(":");
        String targetPlanName = parts[0];
        int durationMonths = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;

        PlanLimits target;
        try {
            target = PlanLimits.fromPlanName(targetPlanName);
        } catch (IllegalArgumentException ex) {
            System.out.println("❌ PLAN_UPGRADE (org) — unknown plan: " + targetPlanName);
            return;
        }
        Optional<Organization> orgOpt;
        try {
            orgOpt = organizationRepository.findById(UUID.fromString(orgId));
        } catch (IllegalArgumentException ex) {
            System.out.println("❌ PLAN_UPGRADE (org) — invalid orgId: " + orgId);
            return;
        }

        if (orgOpt.isEmpty()) {
            System.out.println("⚠️ PLAN_UPGRADE (org) — organization not found: " + orgId);
            return;
        }
        Organization org = orgOpt.get();
        org.setPlan(target.getPlanName());
        org.setMaxStudents(target.getMaxStudents());
        org.setMaxTrainers(target.getMaxTrainers());
        org.setMaxDepartments(target.getMaxDepartments());
        org.setMaxBranchesPerDept(target.getMaxBranchesPerDept());
        org.setMaxBatchesPerBranch(target.getMaxBatchesPerBranch());
        org.setPlanExpiryDate(LocalDate.now().plusMonths(durationMonths));

        Organization saved = organizationRepository.save(org);

        AuthEvent orgEvent = new AuthEvent(
            "ORG_UPDATED",
            null,
            saved.getEmail(),
            null,
            saved.getName(),
            orgId,
            saved.getMaxDepartments(),
            saved.getMaxBranchesPerDept(),
            saved.getMaxBatchesPerBranch()
        );
        orgEvent.setPlan(saved.getPlan());
        orgEvent.setExpiresAt(saved.getPlanExpiryDate().toString());
        authEventProducer.sendEvent(orgEvent);

        System.out.println("✅ PLAN UPGRADED -> orgId=" + orgId + " newPlan=" + targetPlanName);
    }

    private void handleIndividualPlanUpgrade(Map<String, Object> event) {
        Long userId = getLong(event, "userId");
        if (userId == null) {
            System.out.println("⚠️ PLAN_UPGRADE (individual) — missing userId, skipping");
            return;
        }
        String referenceId = getString(event, "referenceId");
        if (referenceId == null || referenceId.isBlank()) {
            System.out.println("⚠️ PLAN_UPGRADE (individual) — missing referenceId, skipping");
            return;
        }
        String[] parts = referenceId.split(":");
        String targetPlanName = parts[0];
        int durationMonths = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;

        if (!IndividualPlanLimits.isValidPlanName(targetPlanName)) {
            System.out.println("❌ PLAN_UPGRADE (individual) — unknown plan: " + targetPlanName);
            return;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            System.out.println("⚠️ PLAN_UPGRADE (individual) — user not found: " + userId);
            return;
        }

        User user = userOpt.get();
        user.setPlan(targetPlanName.toLowerCase());
        user.setPlanExpiryDate(LocalDate.now().plusMonths(durationMonths));
        userRepository.save(user);

        AuthEvent userEvent = new AuthEvent(
            "USER_PLAN_UPDATED",
            user.getId(),
            user.getEmail(),
            user.getRole().name(),
            user.getName(),
            null
        );
        userEvent.setPlan(user.getPlan());
        userEvent.setExpiresAt(user.getPlanExpiryDate().toString());
        authEventProducer.sendEvent(userEvent);

        System.out.println("✅ INDIVIDUAL PLAN UPGRADED -> userId=" + userId + " newPlan=" + targetPlanName);
    }

    private void handleResumePlanUpgrade(Map<String, Object> event) {
        Long userId = getLong(event, "userId");
        if (userId == null) {
            System.out.println("⚠️ RESUME_PLAN_UPGRADE — missing userId, skipping");
            return;
        }

        String referenceId = getString(event, "referenceId");
        if (referenceId == null || referenceId.isBlank()) {
            System.out.println("⚠️ RESUME_PLAN_UPGRADE — missing referenceId, skipping");
            return;
        }
        String[] parts = referenceId.split(":");
        String targetPlanName = parts[0];
        int durationMonths = parts.length > 1 ? Integer.parseInt(parts[1]) : 1;

        if (!IndividualPlanLimits.isValidPlanName(targetPlanName)) {
            System.out.println("❌ RESUME_PLAN_UPGRADE — unknown plan: " + targetPlanName);
            return;
        }

        Optional<User> userOpt = userRepository.findById(userId);
        if (userOpt.isEmpty()) {
            System.out.println("⚠️ RESUME_PLAN_UPGRADE — user not found: " + userId);
            return;
        }

        User user = userOpt.get();

        LocalDate expiresAt = LocalDate.now().plusMonths(durationMonths);

        AuthEvent resumeEvent = new AuthEvent(
            "USER_RESUME_PLAN_UPDATED",
            user.getId(),
            user.getEmail(),
            user.getRole().name(),
            user.getName(),
            user.getOrganizationId() != null ? user.getOrganizationId().toString() : null
        );
        resumeEvent.setPlan(targetPlanName.toLowerCase());
        resumeEvent.setExpiresAt(expiresAt.toString());
        authEventProducer.sendEvent(resumeEvent);

        System.out.println("✅ RESUME PLAN UPGRADED -> userId=" + userId + " newPlan=" + targetPlanName);
    }

    private String getString(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : null;
    }

    private Long getLong(Map<String, Object> map, String key) {
        Object val = map.get(key);
        if (val == null) return null;
        if (val instanceof Number) return ((Number) val).longValue();
        try {
            return Long.parseLong(val.toString());
        } catch (NumberFormatException e) {
            return null;
        }
    }
}