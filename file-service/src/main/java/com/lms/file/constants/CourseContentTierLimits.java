package com.lms.file.constants;

// WHY: Separate limit pool for course-module file uploads, kept independent
// from FileTierLimits (normal uploads). Do not merge these two.
public final class CourseContentTierLimits {

    public static final long FREE_STORAGE_BYTES = 1_073_741_824L;              // 1 GB
    public static final long PRO_STORAGE_BYTES = 8L * 1_073_741_824L;          // 8 GB
    public static final long PREMIUM_STORAGE_BYTES = 40L * 1_073_741_824L;     // 40 GB

    public static final long FREE_MAX_SIZE_BYTES = 50L * 1_048_576L;           // 50 MB
    public static final long PRO_MAX_SIZE_BYTES = 100L * 1_048_576L;           // 100 MB
    public static final long PREMIUM_MAX_SIZE_BYTES = 200L * 1_048_576L;       // 200 MB

    public static long storageCapFor(String tier) {
        return switch (tier.toLowerCase()) {
            case "premium" -> PREMIUM_STORAGE_BYTES;
            case "pro" -> PRO_STORAGE_BYTES;
            default -> FREE_STORAGE_BYTES;
        };
    }

    public static long maxSizeFor(String tier) {
        return switch (tier.toLowerCase()) {
            case "premium" -> PREMIUM_MAX_SIZE_BYTES;
            case "pro" -> PRO_MAX_SIZE_BYTES;
            default -> FREE_MAX_SIZE_BYTES;
        };
    }

    private CourseContentTierLimits() {}
}