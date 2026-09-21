package com.lms.live_session.security;

import com.lms.live_session.entity.TrainerBatchMap;
import com.lms.live_session.exception.LiveSessionAccessDeniedException;
import com.lms.live_session.repository.TrainerBatchMapRepository;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Component
public class LiveSessionAccessValidator {

    private final TrainerBatchMapRepository trainerBatchMapRepository;
    private final JwtUtil jwtUtil;

    public LiveSessionAccessValidator(TrainerBatchMapRepository trainerBatchMapRepository,
                                       JwtUtil jwtUtil) {
        this.trainerBatchMapRepository = trainerBatchMapRepository;
        this.jwtUtil = jwtUtil;
    }

    /**
     * Validates that the trainer creating a session for `batchId` belongs to the
     * same organization as THEIR OWN assignment to that batch. No-op (returns
     * null) for non-org trainers.
     *
     * @param token   raw JWT (no "Bearer " prefix)
     * @param batchId the batch the session is being created for
     * @return the resolved organizationId to stamp onto the LiveSession (may be null)
     * @throws LiveSessionAccessDeniedException if trainer org and batch org mismatch,
     *         or if this trainer has no assignment to the given batch at all
     */
    public Long validateAndResolveOrganizationId(String token, Long batchId) {
        String orgIdStr = jwtUtil.extractOrganizationId(token);

        // Non-org user (Super Admin-created, Google Sign-In, self-registered) → no restriction
        if (orgIdStr == null) {
            return null;
        }

        Long trainerOrgId;
        try {
            trainerOrgId = Long.parseLong(orgIdStr);
        } catch (NumberFormatException e) {
            throw new LiveSessionAccessDeniedException(
                "Malformed organizationId claim in token: " + orgIdStr);
        }

        if (batchId == null) {
            // No batch attached to this session (e.g. published/global session) —
            // nothing to cross-check against. Org-scoped trainer still gets stamped.
            return trainerOrgId;
        }

        String trainerEmail = jwtUtil.extractEmail(token);

        // CHANGED — look up THIS trainer's own mapping to THIS batch, not an
        // arbitrary row for the batch. Fixes the ambiguity where a batch with
        // multiple trainers could validate against the wrong trainer's org.
        Optional<TrainerBatchMap> batchMapping =
            trainerBatchMapRepository.findByTrainerEmailAndBatchId(trainerEmail, batchId);

        if (batchMapping.isEmpty()) {
            // This trainer has no recorded assignment to this batch at all.
            // Previously (via findFirstByBatchId) this case was invisible —
            // an unassigned trainer could still slip through if *some other*
            // trainer's row existed for the batch. Now we explicitly deny.
            throw new LiveSessionAccessDeniedException(
                "Trainer " + trainerEmail + " is not assigned to batch " + batchId);
        }

        Long batchOrgId = batchMapping.get().getOrganizationId();

        // This trainer's mapping row exists but has no org (e.g. pre-Kafka-fix
        // legacy data) → treat as unrestricted, same "null = no isolation"
        // rule applied elsewhere. Backward-compat choice — revisit once all
        // rows are backfilled.
        if (batchOrgId == null) {
            return trainerOrgId;
        }

        if (!batchOrgId.equals(trainerOrgId)) {
            throw new LiveSessionAccessDeniedException(
                "Trainer belongs to organization " + trainerOrgId +
                " but batch " + batchId + " belongs to organization " + batchOrgId);
        }

        return trainerOrgId;
    }
}