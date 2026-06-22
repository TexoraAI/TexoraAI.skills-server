package com.lms.course.constants;

import java.util.Set;

public class CourseFeatureKeys {

    // ───────── TRAINER ─────────
    public static final String GET_MY_COURSES          = "get_my_courses";
    public static final String CREATE_COURSE           = "create_course";
    public static final String UPDATE_COURSE           = "update_course";
    public static final String DELETE_COURSE           = "delete_course";

    // ───────── STUDENT ─────────
    public static final String GET_STUDENT_COURSES      = "get_student_courses";
    public static final String GET_STUDENT_STUDY_PLANS  = "get_student_study_plans";
    public static final String MARK_STUDY_PLAN_PROGRESS = "mark_study_plan_progress";

    // ───────── ADMIN ─────────
    public static final String GET_ALL_COURSES          = "get_all_courses";
    public static final String GET_COURSES_BY_CATEGORY  = "get_courses_by_category";

    // Full set — used to build default (all-enabled) flags
    public static final Set<String> ALL_KEYS = Set.of(
            GET_MY_COURSES, CREATE_COURSE, UPDATE_COURSE, DELETE_COURSE,
            GET_STUDENT_COURSES, GET_STUDENT_STUDY_PLANS, MARK_STUDY_PLAN_PROGRESS,
            GET_ALL_COURSES, GET_COURSES_BY_CATEGORY
    );

    public static final Set<String> TRAINER_KEYS = Set.of(
            GET_MY_COURSES, CREATE_COURSE, UPDATE_COURSE, DELETE_COURSE
    );

    public static final Set<String> STUDENT_KEYS = Set.of(
            GET_STUDENT_COURSES, GET_STUDENT_STUDY_PLANS, MARK_STUDY_PLAN_PROGRESS
    );

    public static final Set<String> ADMIN_KEYS = Set.of(
            GET_ALL_COURSES, GET_COURSES_BY_CATEGORY
    );

    private CourseFeatureKeys() {}
}