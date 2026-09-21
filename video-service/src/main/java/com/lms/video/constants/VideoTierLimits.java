package com.lms.video.constants;

// WHY: Centralized per-tier limits for video storage, per-video size, video count,
// and student-visible video count. Keep in sync with pricing/plan definitions.
public final class VideoTierLimits {

    // Storage in bytes. 1 GB = 1_073_741_824L
    public static final long FREE_STORAGE_BYTES = 1_073_741_824L;
    public static final long PRO_STORAGE_BYTES = 3L * 1_073_741_824L;
    public static final long PREMIUM_STORAGE_BYTES = 6L * 1_073_741_824L;

    public static final long FREE_MAX_VIDEO_SIZE_BYTES = 100L * 1_048_576L;    // 100 MB
    public static final long PRO_MAX_VIDEO_SIZE_BYTES = 200L * 1_048_576L;     // 200 MB
    public static final long PREMIUM_MAX_VIDEO_SIZE_BYTES = 300L * 1_048_576L; // 300 MB

    public static final int FREE_MAX_VIDEO_COUNT = 5;
    public static final int PRO_MAX_VIDEO_COUNT = 20;
    public static final int PREMIUM_MAX_VIDEO_COUNT = 35;

    public static final int FREE_STUDENT_VISIBLE_COUNT = 10;
    public static final int PRO_STUDENT_VISIBLE_COUNT = 25;
    public static final int PREMIUM_STUDENT_VISIBLE_COUNT = -1; // -1 = unlimited

    public static long storageCapFor(String tier) {
        return switch (tier.toLowerCase()) {
            case "premium" -> PREMIUM_STORAGE_BYTES;
            case "pro" -> PRO_STORAGE_BYTES;
            default -> FREE_STORAGE_BYTES;
        };
    }

    public static long maxVideoSizeFor(String tier) {
        return switch (tier.toLowerCase()) {
            case "premium" -> PREMIUM_MAX_VIDEO_SIZE_BYTES;
            case "pro" -> PRO_MAX_VIDEO_SIZE_BYTES;
            default -> FREE_MAX_VIDEO_SIZE_BYTES;
        };
    }

    public static int maxVideoCountFor(String tier) {
        return switch (tier.toLowerCase()) {
            case "premium" -> PREMIUM_MAX_VIDEO_COUNT;
            case "pro" -> PRO_MAX_VIDEO_COUNT;
            default -> FREE_MAX_VIDEO_COUNT;
        };
    }

    public static int studentVisibleCountFor(String tier) {
        return switch (tier.toLowerCase()) {
            case "premium" -> PREMIUM_STUDENT_VISIBLE_COUNT;
            case "pro" -> PRO_STUDENT_VISIBLE_COUNT;
            default -> FREE_STUDENT_VISIBLE_COUNT;
        };
    }

    private VideoTierLimits() {}
}