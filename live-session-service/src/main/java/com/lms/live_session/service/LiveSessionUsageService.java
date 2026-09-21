package com.lms.live_session.service;

import com.lms.live_session.constants.LiveSessionTierLimits;
import com.lms.live_session.entity.LiveSessionUsageTracking;
import com.lms.live_session.exception.AiCompanionNotAvailableException;
import com.lms.live_session.exception.AiCompanionUsageLimitExceededException;
import com.lms.live_session.exception.CalendarResyncLimitExceededException;
import com.lms.live_session.exception.CalendarSyncNotAvailableException;
import com.lms.live_session.exception.EmailDispatchLimitExceededException;
import com.lms.live_session.exception.LiveClassLimitExceededException;
import com.lms.live_session.exception.MeetingLimitExceededException;
import com.lms.live_session.exception.RecordingDurationLimitExceededException;
import com.lms.live_session.exception.RecordingStorageLimitExceededException;
import com.lms.live_session.exception.WhiteboardNotAvailableException;
import com.lms.live_session.repository.LiveSessionUsageTrackingRepository;
import com.lms.live_session.repository.RecordingRepository;
import org.springframework.stereotype.Service;

import java.time.YearMonth;
import java.util.Map;

/**
 * Plan-tier enforcement for live-session actions: class creation, AI
 * Companion access, recording storage/duration, whiteboard access,
 * meeting creation, dashboard email dispatch, and calendar sync.
 */
@Service
public class LiveSessionUsageService {

    private final LiveSessionUsageTrackingRepository usageRepo;
    private final LiveSessionTierResolver tierResolver;
    private final RecordingRepository recordingRepository;

    public LiveSessionUsageService(LiveSessionUsageTrackingRepository usageRepo,
                                    LiveSessionTierResolver tierResolver,
                                    RecordingRepository recordingRepository) {
        this.usageRepo = usageRepo;
        this.tierResolver = tierResolver;
        this.recordingRepository = recordingRepository;
    }

    public void checkAndIncrementClassCreation(Long organizationId, String email) {
        String tier = tierResolver.resolveTier(organizationId, email);
        int max = LiveSessionTierLimits.maxClassesFor(tier);
        if (max == -1) {
            recordUsageOnly(email, "CLASS_CREATE");
            return;
        }
        String period = YearMonth.now().toString();
        LiveSessionUsageTracking usage = findOrCreate(email, period, "CLASS_CREATE");
        if (usage.getUsageCount() >= max) {
            throw new LiveClassLimitExceededException(email, tier, usage.getUsageCount(), max, period);
        }
        usage.setUsageCount(usage.getUsageCount() + 1);
        usage.setUpdatedAt(java.time.LocalDateTime.now());
        usageRepo.save(usage);
    }

    public void checkAiCompanionAccess(Long organizationId, String email) {
        String tier = tierResolver.resolveTier(organizationId, email);
        if (!LiveSessionTierLimits.aiCompanionEnabledFor(tier)) {
            throw new AiCompanionNotAvailableException(email, tier,
                    "AI Companion is not available on the free plan. Upgrade to access it.");
        }
        if ("premium".equalsIgnoreCase(tier)) {
            recordUsageOnly(email, "AI_COMPANION_USE");
            return;
        }
        int max = LiveSessionTierLimits.aiCompanionMonthlyLimitFor(tier);
        String period = YearMonth.now().toString();
        LiveSessionUsageTracking usage = findOrCreate(email, period, "AI_COMPANION_USE");
        if (usage.getUsageCount() >= max) {
            throw new AiCompanionUsageLimitExceededException(email, tier, usage.getUsageCount(), max, period);
        }
        usage.setUsageCount(usage.getUsageCount() + 1);
        usage.setUpdatedAt(java.time.LocalDateTime.now());
        usageRepo.save(usage);
    }

    public void checkRecordingLimits(Long organizationId, String trainerEmail,
                                      Integer durationMinutes, long attemptedAddBytes) {
        String tier = tierResolver.resolveTier(organizationId, trainerEmail);
        int maxMinutes = LiveSessionTierLimits.maxRecordingMinutesFor(tier);
        if (maxMinutes != -1 && durationMinutes != null && durationMinutes > maxMinutes) {
            throw new RecordingDurationLimitExceededException(durationMinutes, maxMinutes, tier);
        }
        long capacity = LiveSessionTierLimits.storageCapFor(tier);
        long currentUsage = recordingRepository.sumFileSizeByTrainer(trainerEmail);
        if (currentUsage + attemptedAddBytes > capacity) {
            throw new RecordingStorageLimitExceededException(currentUsage, attemptedAddBytes, capacity, tier);
        }
    }

    public void checkWhiteboardAccess(Long organizationId, String trainerEmail) {
        String tier = tierResolver.resolveTier(organizationId, trainerEmail);
        if (!LiveSessionTierLimits.whiteboardEnabledFor(tier)) {
            throw new WhiteboardNotAvailableException(trainerEmail, tier,
                    "Whiteboard is not available on the free plan. Upgrade to access it.");
        }
    }

    // Non-throwing companion to checkWhiteboardAccess — used by the frontend
    // badge to show tier/availability without triggering a 403. Resolves
    // tier from the exact same (organizationId, trainerEmail) pair
    // checkWhiteboardAccess uses, so the two never disagree.
    public Map<String, Object> getWhiteboardAccessStatus(Long organizationId, String trainerEmail) {
        String tier = tierResolver.resolveTier(organizationId, trainerEmail);
        boolean available = LiveSessionTierLimits.whiteboardEnabledFor(tier);
        return Map.of("tier", tier, "available", available);
    }

    // --- Feature 2: Meetings/Calendar plan awareness ---

    public void checkAndIncrementMeetingCreation(Long organizationId, String email) {
        String tier = tierResolver.resolveTier(organizationId, email);
        int max = LiveSessionTierLimits.maxMeetingsFor(tier);
        if (max == -1) {
            recordUsageOnly(email, "MEETING_CREATE");
            return;
        }
        String period = YearMonth.now().toString();
        LiveSessionUsageTracking usage = findOrCreate(email, period, "MEETING_CREATE");
        if (usage.getUsageCount() >= max) {
            throw new MeetingLimitExceededException(email, tier, usage.getUsageCount(), max, period);
        }
        usage.setUsageCount(usage.getUsageCount() + 1);
        usage.setUpdatedAt(java.time.LocalDateTime.now());
        usageRepo.save(usage);
    }

    public void checkAndIncrementEmailDispatch(Long organizationId, String email) {
        String tier = tierResolver.resolveTier(organizationId, email);
        int max = LiveSessionTierLimits.maxEmailsFor(tier);
        if (max == -1) {
            recordUsageOnly(email, "EMAIL_DISPATCH");
            return;
        }
        String period = YearMonth.now().toString();
        LiveSessionUsageTracking usage = findOrCreate(email, period, "EMAIL_DISPATCH");
        if (usage.getUsageCount() >= max) {
            throw new EmailDispatchLimitExceededException(email, tier, usage.getUsageCount(), max, period);
        }
        usage.setUsageCount(usage.getUsageCount() + 1);
        usage.setUpdatedAt(java.time.LocalDateTime.now());
        usageRepo.save(usage);
    }

    public void checkCalendarSyncAccess(Long organizationId, String email) {
        String tier = tierResolver.resolveTier(organizationId, email);
        if (!LiveSessionTierLimits.calendarSyncEnabledFor(tier)) {
            throw new CalendarSyncNotAvailableException(email, tier,
                    "Calendar sync is not available on the free plan. Upgrade to connect your calendar.");
        }
        // Connecting itself isn't metered, only gated — no increment here.
    }

    public void checkAndIncrementCalendarResync(Long organizationId, String email) {
        String tier = tierResolver.resolveTier(organizationId, email);
        int max = LiveSessionTierLimits.calendarResyncMaxFor(tier);
        if (max == -1) {
            recordUsageOnly(email, "CALENDAR_RESYNC");
            return;
        }
        String period = YearMonth.now().toString();
        LiveSessionUsageTracking usage = findOrCreate(email, period, "CALENDAR_RESYNC");
        if (usage.getUsageCount() >= max) {
            throw new CalendarResyncLimitExceededException(email, tier, usage.getUsageCount(), max, period);
        }
        usage.setUsageCount(usage.getUsageCount() + 1);
        usage.setUpdatedAt(java.time.LocalDateTime.now());
        usageRepo.save(usage);
    }

    private LiveSessionUsageTracking findOrCreate(String email, String period, String action) {
        return usageRepo.findByEmailAndPeriodAndAction(email, period, action)
                .orElseGet(() -> {
                    LiveSessionUsageTracking u = new LiveSessionUsageTracking();
                    u.setEmail(email);
                    u.setPeriod(period);
                    u.setAction(action);
                    u.setUsageCount(0);
                    return u;
                });
    }

    private void recordUsageOnly(String email, String action) {
        String period = YearMonth.now().toString();
        LiveSessionUsageTracking usage = findOrCreate(email, period, action);
        usage.setUsageCount(usage.getUsageCount() + 1);
        usage.setUpdatedAt(java.time.LocalDateTime.now());
        usageRepo.save(usage);
    }

    public Map<String, Object> getUsageStatus(Long organizationId, String email, String action) {
        String tier = tierResolver.resolveTier(organizationId, email);
        String period = YearMonth.now().toString();
        int used = usageRepo.findByEmailAndPeriodAndAction(email, period, action)
                .map(LiveSessionUsageTracking::getUsageCount).orElse(0);
        int limit = switch (action) {
            case "CLASS_CREATE" -> LiveSessionTierLimits.maxClassesFor(tier);
            case "MEETING_CREATE" -> LiveSessionTierLimits.maxMeetingsFor(tier);
            case "EMAIL_DISPATCH" -> LiveSessionTierLimits.maxEmailsFor(tier);
            default -> LiveSessionTierLimits.aiCompanionMonthlyLimitFor(tier);
        };
        return Map.of("action", action, "tier", tier, "used", used,
                "limit", limit == -1 ? "unlimited" : limit, "period", period);
    }
    public Map<String, Object> getUsageStatus(String organizationId, String email, String action) {
        String tier = tierResolver.resolveTier(organizationId, email);
        String period = YearMonth.now().toString();
        int used = usageRepo.findByEmailAndPeriodAndAction(email, period, action)
                .map(LiveSessionUsageTracking::getUsageCount).orElse(0);
        int limit = switch (action) {
            case "CLASS_CREATE" -> LiveSessionTierLimits.maxClassesFor(tier);
            case "MEETING_CREATE" -> LiveSessionTierLimits.maxMeetingsFor(tier);
            case "EMAIL_DISPATCH" -> LiveSessionTierLimits.maxEmailsFor(tier);
            default -> LiveSessionTierLimits.aiCompanionMonthlyLimitFor(tier);
        };
        return Map.of("action", action, "tier", tier, "used", used,
                "limit", limit == -1 ? "unlimited" : limit, "period", period);
    }
}
