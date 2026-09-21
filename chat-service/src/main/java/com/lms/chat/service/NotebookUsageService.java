package com.lms.chat.service;

import com.lms.chat.constants.NotebookTierResolver;
import com.lms.chat.constants.NotebookUsageLimits;
import com.lms.chat.entity.NotebookUsageTracking;
import com.lms.chat.exception.NotebookUsageLimitExceededException;
import com.lms.chat.repository.NotebookUsageTrackingRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Map;

@Service
public class NotebookUsageService {

    private final NotebookUsageTrackingRepository usageRepo;
    private final NotebookTierResolver notebookTierResolver;

    public NotebookUsageService(NotebookUsageTrackingRepository usageRepo,
                                 NotebookTierResolver notebookTierResolver) {
        this.usageRepo = usageRepo;
        this.notebookTierResolver = notebookTierResolver;
    }

    // NOTE: same known concurrency limitation as ResumeUsageService (read-then-write,
    // not row-locked) — acceptable for this traffic pattern, flag if a stricter
    // lock is wanted later.
    public void checkAndIncrement(String studentEmail, String organizationId) {
        String tier = notebookTierResolver.resolveTier(organizationId, studentEmail);
        String period = YearMonth.now().toString();

        if (NotebookUsageLimits.isUnlimited(tier)) {
            recordUsageOnly(studentEmail, period);
            return;
        }

        NotebookUsageTracking usage = usageRepo
                .findByStudentEmailAndPeriod(studentEmail, period)
                .orElseGet(() -> {
                    NotebookUsageTracking u = new NotebookUsageTracking();
                    u.setStudentEmail(studentEmail);
                    u.setPeriod(period);
                    u.setActionCount(0);
                    return u;
                });

        int limit = NotebookUsageLimits.limitFor(tier);
        if (usage.getActionCount() >= limit) {
            throw new NotebookUsageLimitExceededException(
                    studentEmail, tier, usage.getActionCount(), limit, period);
        }

        usage.setActionCount(usage.getActionCount() + 1);
        usageRepo.save(usage);
    }

    private void recordUsageOnly(String studentEmail, String period) {
        NotebookUsageTracking usage = usageRepo
                .findByStudentEmailAndPeriod(studentEmail, period)
                .orElseGet(() -> {
                    NotebookUsageTracking u = new NotebookUsageTracking();
                    u.setStudentEmail(studentEmail);
                    u.setPeriod(period);
                    u.setActionCount(0);
                    return u;
                });
        usage.setActionCount(usage.getActionCount() + 1);
        usageRepo.save(usage);
    }

    public Map<String, Object> getUsageStatus(String studentEmail, String organizationId) {
        String tier = notebookTierResolver.resolveTier(organizationId, studentEmail);
        String period = YearMonth.now().toString();
        int used = usageRepo.findByStudentEmailAndPeriod(studentEmail, period)
                .map(NotebookUsageTracking::getActionCount)
                .orElse(0);
        int limit = NotebookUsageLimits.limitFor(tier);
        return Map.of(
                "tier", tier,
                "used", used,
                "limit", NotebookUsageLimits.isUnlimited(tier) ? "unlimited" : limit,
                "period", period
        );
    }
}