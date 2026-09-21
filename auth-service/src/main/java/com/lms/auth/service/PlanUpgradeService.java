package com.lms.auth.service;

import com.lms.auth.constants.IndividualPlanLimits;
import com.lms.auth.constants.PlanLimits;
import com.lms.auth.dto.IndividualUpgradePreviewResponse;
import com.lms.auth.dto.UpgradePreviewResponse;
import com.lms.auth.model.Organization;
import com.lms.auth.model.User;
import com.lms.auth.repository.OrganizationRepository;
import com.lms.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;
import com.lms.auth.dto.ResumePlanPreviewResponse;
import java.util.UUID;
import com.lms.auth.constants.PlanDurationLimits;
import java.time.LocalDate;
@Service
public class PlanUpgradeService {

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;

    public PlanUpgradeService(OrganizationRepository organizationRepository,
                               UserRepository userRepository) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
    }

    // ─────────────────────── PART A — ORG-LEVEL PREVIEW ───────────────────────
    public UpgradePreviewResponse previewUpgrade(UUID orgId, String targetPlan, int durationMonths) {
        PlanLimits target;
        try {
            target = PlanLimits.fromPlanName(targetPlan);
        } catch (IllegalArgumentException ex) {
            return new UpgradePreviewResponse(
                    orgId.toString(), null, targetPlan, 0, false, "Unknown plan");
        }

        if (targetPlan.equalsIgnoreCase("free") || targetPlan.equalsIgnoreCase("trial")) {
            return new UpgradePreviewResponse(
                    orgId.toString(), null, targetPlan, 0, false, "Free plan has no duration");
        }

        if (!PlanDurationLimits.isValidDuration(durationMonths)) {
            return new UpgradePreviewResponse(
                    orgId.toString(), null, targetPlan, 0, false,
                    "Invalid duration — choose 1, 6, or 12 months");
        }

        Organization org = organizationRepository.findById(orgId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "Organization not found"));

        String currentPlanName = org.getPlan();

        if (currentPlanName != null && currentPlanName.equalsIgnoreCase(targetPlan)) {
            return new UpgradePreviewResponse(
                    orgId.toString(), currentPlanName, targetPlan, 0, false, "Already on this plan");
        }

        try {
            PlanLimits current = PlanLimits.fromPlanName(currentPlanName);
            if (target.getPrice() < current.getPrice()) {
                return new UpgradePreviewResponse(
                        orgId.toString(), currentPlanName, targetPlan, 0, false,
                        "Downgrades not supported via this flow");
            }
        } catch (IllegalArgumentException ex) {
            // Current plan name on the org doesn't map to a known PlanLimits value.
            // Allow the upgrade preview to proceed rather than blocking on unknown state.
        }

        int finalPrice = PlanDurationLimits.calculatePrice(target.getPrice(), durationMonths);
        LocalDate expiresAt = LocalDate.now().plusMonths(durationMonths);

        return new UpgradePreviewResponse(
                orgId.toString(), currentPlanName, targetPlan, finalPrice, true, null,
                durationMonths, expiresAt);
    }
    // ───────────────────── PART B — INDIVIDUAL-LEVEL PREVIEW ───────────────────
    public IndividualUpgradePreviewResponse previewIndividualUpgrade(Long userId, String targetPlan, int durationMonths) {
        if (!IndividualPlanLimits.isValidPlanName(targetPlan)) {
            return new IndividualUpgradePreviewResponse(
                    userId, null, targetPlan, 0, false, "Unknown plan");
        }

        if (targetPlan.equalsIgnoreCase("free")) {
            return new IndividualUpgradePreviewResponse(
                    userId, null, targetPlan, 0, false, "Free plan has no duration");
        }

        if (!PlanDurationLimits.isValidDuration(durationMonths)) {
            return new IndividualUpgradePreviewResponse(
                    userId, null, targetPlan, 0, false,
                    "Invalid duration — choose 1, 6, or 12 months");
        }
        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        if (user.getOrganizationId() != null) {
            return new IndividualUpgradePreviewResponse(
                    userId, user.getPlan(), targetPlan, 0, false,
                    "Org-bound users upgrade via their organization, not individually");
        }

        String currentPlan = (user.getPlan() == null) ? "free" : user.getPlan();

        if (currentPlan.equalsIgnoreCase(targetPlan)) {
            return new IndividualUpgradePreviewResponse(
                    userId, currentPlan, targetPlan, 0, false, "Already on this plan");
        }
        int targetPrice;
        try {
            targetPrice = IndividualPlanLimits.resolvePrice(targetPlan, user.getRole().name());
        } catch (IllegalArgumentException ex) {
            return new IndividualUpgradePreviewResponse(
                    userId, currentPlan, targetPlan, 0, false, "Plan not available for this role");
        }

        try {
            int currentPrice = IndividualPlanLimits.resolvePrice(currentPlan, user.getRole().name());
            if (targetPrice < currentPrice) {
                return new IndividualUpgradePreviewResponse(
                        userId, currentPlan, targetPlan, 0, false,
                        "Downgrades not supported via this flow");
            }
        } catch (IllegalArgumentException ex) {
            // Current plan on the user doesn't map to a known price (unexpected
            // stale data) — skip the downgrade check and allow the upgrade
            // preview to proceed, matching the same fallback used in the
            // org-level previewUpgrade().
        }

//       
        int finalPrice = PlanDurationLimits.calculatePrice(targetPrice, durationMonths);
        LocalDate expiresAt = LocalDate.now().plusMonths(durationMonths);

        return new IndividualUpgradePreviewResponse(
                userId, currentPlan, targetPlan, finalPrice, true, null,
                durationMonths, expiresAt);
    }
    // ─────────────────── PART D — RESUME-PLAN PREVIEW (org-bound users allowed) ───────────────────
    public ResumePlanPreviewResponse previewResumeUpgrade(Long userId, String targetPlan, int durationMonths) {
        if (!IndividualPlanLimits.isValidPlanName(targetPlan)) {
            return new ResumePlanPreviewResponse(
                    userId, null, targetPlan, 0, false, "Unknown plan");
        }

        if (targetPlan.equalsIgnoreCase("free")) {
            return new ResumePlanPreviewResponse(
                    userId, null, targetPlan, 0, false, "Free plan has no duration");
        }

        if (!PlanDurationLimits.isValidDuration(durationMonths)) {
            return new ResumePlanPreviewResponse(
                    userId, null, targetPlan, 0, false,
                    "Invalid duration — choose 1, 6, or 12 months");
        }

        User user = userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found"));

        // No organizationId check here — org-bound users are explicitly
        // allowed through this path. This is the entire point of this method.

        int price;
        try {
            price = IndividualPlanLimits.resolvePrice(targetPlan, user.getRole().name());
        } catch (IllegalArgumentException ex) {
            return new ResumePlanPreviewResponse(
                    userId, null, targetPlan, 0, false, "Plan not available for this role");
        }

        // auth-service does not track the user's current resume plan (that
        // lives in user-service), so currentResumePlan is left null and no
        // "already on this plan" / downgrade check is performed here.
        int finalPrice = PlanDurationLimits.calculatePrice(price, durationMonths);
        LocalDate expiresAt = LocalDate.now().plusMonths(durationMonths);

        return new ResumePlanPreviewResponse(
                userId, null, targetPlan, finalPrice, true, null,
                durationMonths, expiresAt);
    }

}