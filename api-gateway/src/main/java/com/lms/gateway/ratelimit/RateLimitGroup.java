package com.lms.gateway.ratelimit;

/**
 * Token bucket configuration per API group.
 * replenishRate  = tokens added per second
 * burstCapacity  = max tokens (bucket size)
 */
public enum RateLimitGroup {

    // Auth
    AUTH_LOGIN          (1,   5),
    AUTH_REGISTER       (1,   3),
    AUTH_PASSWORD       (0,   3),   // ~1 per 60s — handled via TTL in Lua; here burst=3

    // Public apply
    PUBLIC_APPLY        (1,   5),

    // User / resume
    USER_API            (20,  100),

    // Courses
    COURSE_API          (50,  200),
    CONTENT_API         (50,  200),

    // Video
    VIDEO_API           (20,  100),
    VIDEO_UPLOAD        (1,   5),

    // Notification
    NOTIFICATION_API    (20,  100),

    // Analytics
    ANALYTICS_API       (10,  60),

    // Assessment
    ASSESSMENT_API      (20,  100),
    BULK_UPLOAD         (1,   3),

    // Assignments
    ASSIGNMENT_API      (20,  100),

    // Code / study
    CODE_API            (5,   30),
    STUDY_PLAN_API      (5,   30),

    // Enrollment
    ENROLLMENT_API      (20,  100),

    // Progress
    PROGRESS_API        (50,  200),

    // Search
    SEARCH_API          (10,  60),

    // Payment
    PAYMENT_API         (5,   20),

    // File
    FILE_API            (20,  100),
    FILE_UPLOAD         (1,   10),

    // Student/trainer lists
    STUDENT_API         (20,  100),

    // Attendance
    ATTENDANCE_API      (20,  100),

    // Batch/branch
    BATCH_API           (20,  100),

    // Chat
    CHAT_API            (20,  120),

    // Live session
    LIVE_SESSION_API    (20,  120),

    // Whiteboard REST
    WHITEBOARD_REST     (60,  300),

    // AI companion
    AI_COMPANION        (2,   30),

    // Organization
    ORGANIZATION_API    (20,  100),

    // Fallback
    GENERAL_API         (20,  100);

    private final int replenishRate;
    private final int burstCapacity;

    RateLimitGroup(int replenishRate, int burstCapacity) {
        this.replenishRate  = replenishRate;
        this.burstCapacity  = burstCapacity;
    }

    public int replenishRate()  { return replenishRate; }
    public int burstCapacity()  { return burstCapacity; }
}