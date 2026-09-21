package com.lms.live_session.constants;

/**
 * Plan-tier limits for live-session functionality: class creation, AI
 * Companion access, recording storage/duration, and whiteboard access.
 */
public final class LiveSessionTierLimits {

    private LiveSessionTierLimits() {
    }

    // --- Live classes per month (-1 = unlimited) ---
    public static final int FREE_MAX_CLASSES = 3;
    public static final int PRO_MAX_CLASSES = 15;
    public static final int PREMIUM_MAX_CLASSES = -1;

    // --- AI Companion ---
    public static final boolean FREE_AI_COMPANION_ENABLED = false;
    public static final boolean PRO_AI_COMPANION_ENABLED = true;
    public static final boolean PREMIUM_AI_COMPANION_ENABLED = true;
    public static final int PRO_AI_COMPANION_MAX_SESSIONS = 15;

    // --- Recording storage / duration (-1 = unlimited minutes) ---
    public static final long FREE_STORAGE_BYTES = 1_073_741_824L;
    public static final long PRO_STORAGE_BYTES = 5L * 1_073_741_824L;
    public static final long PREMIUM_STORAGE_BYTES = 20L * 1_073_741_824L;
    public static final int FREE_MAX_RECORDING_MINUTES = 30;
    public static final int PRO_MAX_RECORDING_MINUTES = 90;
    public static final int PREMIUM_MAX_RECORDING_MINUTES = -1;

    // --- Whiteboard ---
    public static final boolean FREE_WHITEBOARD_ENABLED = false;
    public static final boolean PRO_WHITEBOARD_ENABLED = true;
    public static final boolean PREMIUM_WHITEBOARD_ENABLED = true;

    public static int maxClassesFor(String tier) {
        return switch (tier == null ? "" : tier.toLowerCase()) {
            case "pro" -> PRO_MAX_CLASSES;
            case "premium" -> PREMIUM_MAX_CLASSES;
            default -> FREE_MAX_CLASSES;
        };
    }

    public static boolean aiCompanionEnabledFor(String tier) {
        return switch (tier == null ? "" : tier.toLowerCase()) {
            case "pro" -> PRO_AI_COMPANION_ENABLED;
            case "premium" -> PREMIUM_AI_COMPANION_ENABLED;
            default -> FREE_AI_COMPANION_ENABLED;
        };
    }

    public static int aiCompanionMonthlyLimitFor(String tier) {
        return switch (tier == null ? "" : tier.toLowerCase()) {
            case "pro" -> PRO_AI_COMPANION_MAX_SESSIONS;
            case "premium" -> -1; // unlimited
            default -> 0;
        };
    }

    public static long storageCapFor(String tier) {
        return switch (tier == null ? "" : tier.toLowerCase()) {
            case "pro" -> PRO_STORAGE_BYTES;
            case "premium" -> PREMIUM_STORAGE_BYTES;
            default -> FREE_STORAGE_BYTES;
        };
    }

    public static int maxRecordingMinutesFor(String tier) {
        return switch (tier == null ? "" : tier.toLowerCase()) {
            case "pro" -> PRO_MAX_RECORDING_MINUTES;
            case "premium" -> PREMIUM_MAX_RECORDING_MINUTES;
            default -> FREE_MAX_RECORDING_MINUTES;
        };
    }

    public static boolean whiteboardEnabledFor(String tier) {
        return switch (tier == null ? "" : tier.toLowerCase()) {
            case "pro" -> PRO_WHITEBOARD_ENABLED;
            case "premium" -> PREMIUM_WHITEBOARD_ENABLED;
            default -> FREE_WHITEBOARD_ENABLED;
        };
    }

    // --- Meetings per month (shared counter: instant/scheduled/event/schedule-linked; -1 = unlimited) ---
    public static final int FREE_MAX_MEETINGS = 5;
    public static final int PRO_MAX_MEETINGS = 25;
    public static final int PREMIUM_MAX_MEETINGS = -1;

    // --- Dashboard email dispatch per month (-1 = unlimited) ---
    public static final int FREE_MAX_EMAILS = 10;
    public static final int PRO_MAX_EMAILS = 50;
    public static final int PREMIUM_MAX_EMAILS = -1;

    // --- Google Calendar sync ---
    public static final boolean FREE_CALENDAR_SYNC_ENABLED = false;
    public static final boolean PRO_CALENDAR_SYNC_ENABLED = true;
    public static final boolean PREMIUM_CALENDAR_SYNC_ENABLED = true;
    public static final int PRO_CALENDAR_RESYNC_MAX = 3;
    public static final int PREMIUM_CALENDAR_RESYNC_MAX = -1;

    public static int maxMeetingsFor(String tier) {
        return switch (tier == null ? "" : tier.toLowerCase()) {
            case "pro" -> PRO_MAX_MEETINGS;
            case "premium" -> PREMIUM_MAX_MEETINGS;
            default -> FREE_MAX_MEETINGS;
        };
    }

    public static int maxEmailsFor(String tier) {
        return switch (tier == null ? "" : tier.toLowerCase()) {
            case "pro" -> PRO_MAX_EMAILS;
            case "premium" -> PREMIUM_MAX_EMAILS;
            default -> FREE_MAX_EMAILS;
        };
    }

    public static boolean calendarSyncEnabledFor(String tier) {
        return switch (tier == null ? "" : tier.toLowerCase()) {
            case "pro" -> PRO_CALENDAR_SYNC_ENABLED;
            case "premium" -> PREMIUM_CALENDAR_SYNC_ENABLED;
            default -> FREE_CALENDAR_SYNC_ENABLED;
        };
    }

    public static int calendarResyncMaxFor(String tier) {
        return switch (tier == null ? "" : tier.toLowerCase()) {
            case "pro" -> PRO_CALENDAR_RESYNC_MAX;
            case "premium" -> PREMIUM_CALENDAR_RESYNC_MAX;
            // free never reaches this check (calendarSyncEnabledFor gates it first),
            // but return 0, not -1, so it can never accidentally read as unlimited.
            default -> 0;
        };
    }
}
