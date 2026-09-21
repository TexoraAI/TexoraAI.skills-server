package com.lms.course.constants;

public class CourseTierLimits {

    public static final int FREE_MAX_COURSES = 2;
    public static final int PRO_MAX_COURSES = 5;
    public static final int PREMIUM_MAX_COURSES = 10;

    public static final int FREE_MAX_MODULES_PER_COURSE = 8;
    public static final int PRO_MAX_MODULES_PER_COURSE = 14;
    public static final int PREMIUM_MAX_MODULES_PER_COURSE = 20;

    private CourseTierLimits() {}

    public static int maxCoursesFor(String tier) {
        if (tier == null) return FREE_MAX_COURSES;
        return switch (tier.toLowerCase()) {
            case "premium" -> PREMIUM_MAX_COURSES;
            case "pro"     -> PRO_MAX_COURSES;
            default        -> FREE_MAX_COURSES;
        };
    }

       public static int maxModulesPerCourseFor(String tier) {
        if (tier == null) return FREE_MAX_MODULES_PER_COURSE;
        return switch (tier.toLowerCase()) {
            case "premium" -> PREMIUM_MAX_MODULES_PER_COURSE;
            case "pro"     -> PRO_MAX_MODULES_PER_COURSE;
            default        -> FREE_MAX_MODULES_PER_COURSE;
        };
    }

    // NEW — separate pool from maxCoursesFor (trainer creation limit).
    // Caps how many enrolled courses a STUDENT can see, mirrors the
    // studentVisibleCountFor pattern already used in video-service/file-service.
    public static final int FREE_STUDENT_VISIBLE_COUNT = 10;
    public static final int PRO_STUDENT_VISIBLE_COUNT = 25;
    public static final int PREMIUM_STUDENT_VISIBLE_COUNT = -1; // unlimited

    public static int studentVisibleCountFor(String tier) {
        if (tier == null) return FREE_STUDENT_VISIBLE_COUNT;
        return switch (tier.toLowerCase()) {
            case "premium" -> PREMIUM_STUDENT_VISIBLE_COUNT;
            case "pro"     -> PRO_STUDENT_VISIBLE_COUNT;
            default        -> FREE_STUDENT_VISIBLE_COUNT;
        };
    }
}
