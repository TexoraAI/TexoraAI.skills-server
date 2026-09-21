package com.lms.user.service;

import com.lms.user.constants.ResumeAiUsageLimits;
import com.lms.user.exception.AiGenerationLimitExceededException;
import com.lms.user.model.ResumeUsageTracking;
import com.lms.user.model.User;
import com.lms.user.repo.ResumeUsageTrackingRepository;
import com.lms.user.repo.UserRepository;
import java.util.Map;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import com.lms.user.constants.ResumeTierResolver;
@Service
public class ResumeUsageService {

    private final ResumeUsageTrackingRepository usageRepo;
    private final UserRepository userRepository;

    @Autowired
    public ResumeUsageService(ResumeUsageTrackingRepository usageRepo,
                               UserRepository userRepository) {
        this.usageRepo = usageRepo;
        this.userRepository = userRepository;
    }

    @Transactional
    public void checkAndIncrement(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        String plan = (user != null) ? ResumeTierResolver.resolveEffectiveTier(user) : "free";

        if (ResumeAiUsageLimits.isUnlimited(plan)) {
            // still record usage for analytics, but never block
            recordUsageOnly(userId);
            return;
        }

        String period = java.time.YearMonth.now().toString(); // "2026-09"
        ResumeUsageTracking usage = usageRepo.findByUserIdAndPeriod(userId, period)
                .orElseGet(() -> {
                    ResumeUsageTracking u = new ResumeUsageTracking();
                    u.setUserId(userId);
                    u.setPeriod(period);
                    u.setAiGenerationCount(0);
                    return u;
                });

        int limit = ResumeAiUsageLimits.limitFor(plan);
        if (usage.getAiGenerationCount() >= limit) {
            throw new AiGenerationLimitExceededException(
                userId, plan, usage.getAiGenerationCount(), limit, period);
        }

        usage.setAiGenerationCount(usage.getAiGenerationCount() + 1);
        usageRepo.save(usage);
    }

    // NOTE: checkAndIncrement is NOT perfectly race-safe under high concurrency
    // (read-then-write without a row lock) — acceptable for this feature's actual
    // traffic pattern (one person generating one resume at a time), but flagged as
    // a known limitation, same category as noted elsewhere in this codebase
    // (e.g. payment-wallet-service's wallet lock). If concurrent double-submission
    // becomes a real issue later, add @Lock(PESSIMISTIC_WRITE) on the repository
    // find method.

    private void recordUsageOnly(Long userId) {
        String period = java.time.YearMonth.now().toString();
        ResumeUsageTracking usage = usageRepo.findByUserIdAndPeriod(userId, period)
                .orElseGet(() -> {
                    ResumeUsageTracking u = new ResumeUsageTracking();
                    u.setUserId(userId);
                    u.setPeriod(period);
                    u.setAiGenerationCount(0);
                    return u;
                });
        usage.setAiGenerationCount(usage.getAiGenerationCount() + 1);
        usageRepo.save(usage);
    }

    public Map<String, Object> getUsageStatus(Long userId) {
        User user = userRepository.findById(userId).orElse(null);
        String plan = (user != null) ? ResumeTierResolver.resolveEffectiveTier(user) : "free";
        String period = java.time.YearMonth.now().toString();
        int used = usageRepo.findByUserIdAndPeriod(userId, period)
                .map(ResumeUsageTracking::getAiGenerationCount)
                .orElse(0);
        int limit = ResumeAiUsageLimits.limitFor(plan);
        return Map.of(
            "plan", plan,
            "used", used,
            "limit", ResumeAiUsageLimits.isUnlimited(plan) ? "unlimited" : limit,
            "period", period
        );
    }
}