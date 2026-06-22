package com.lms.batch.constants;

import java.util.Set;

public class BatchFeatureKeys {

    // ───────── TRAINER ─────────
    public static final String GET_TRAINER_BATCHES    = "get_trainer_batches";
    public static final String GET_TRAINER_DASHBOARD  = "get_trainer_dashboard";
    public static final String GET_BATCH_STUDENTS     = "get_batch_students"; // trainer: view students in batch

    // ───────── STUDENT ─────────
    public static final String GET_STUDENT_BATCH      = "get_student_batch";
    public static final String GET_STUDENT_CLASSROOM  = "get_student_classroom";

    // ───────── ADMIN ─────────
    public static final String CREATE_BATCH           = "create_batch";
    public static final String DELETE_BATCH           = "delete_batch";
    public static final String GET_ALL_BATCHES        = "get_all_batches";
    public static final String ASSIGN_TRAINER         = "assign_trainer";
    public static final String REMOVE_TRAINER         = "remove_trainer";
    public static final String ASSIGN_STUDENTS        = "assign_students";
    public static final String REMOVE_STUDENT         = "remove_student";
    public static final String GET_TRAINER_STUDENTS   = "get_trainer_students";
    public static final String GET_AVAILABLE_STUDENTS = "get_available_students";
    public static final String GET_AVAILABLE_TRAINERS = "get_available_trainers";
    public static final String GET_BRANCHES           = "get_branches";
    public static final String CREATE_BRANCH          = "create_branch";
    public static final String DELETE_BRANCH          = "delete_branch";
    public static final String UPDATE_BRANCH          = "update_branch";
    public static final String CREATE_DEPARTMENT      = "create_department";
    public static final String GET_DEPARTMENTS        = "get_departments";
    public static final String GET_DEPARTMENT_BY_ID   = "get_department_by_id";
    public static final String UPDATE_DEPARTMENT      = "update_department";
    public static final String DELETE_DEPARTMENT      = "delete_department";

    // Full set — used to build default (all-enabled) flags
    public static final Set<String> ALL_KEYS = Set.of(
            GET_TRAINER_BATCHES, GET_TRAINER_DASHBOARD, GET_BATCH_STUDENTS,
            GET_STUDENT_BATCH, GET_STUDENT_CLASSROOM,
            CREATE_BATCH, DELETE_BATCH, GET_ALL_BATCHES, ASSIGN_TRAINER,
            REMOVE_TRAINER, ASSIGN_STUDENTS, REMOVE_STUDENT, GET_TRAINER_STUDENTS,
            GET_AVAILABLE_STUDENTS, GET_AVAILABLE_TRAINERS, GET_BRANCHES,
            CREATE_BRANCH, DELETE_BRANCH, UPDATE_BRANCH, CREATE_DEPARTMENT,
            GET_DEPARTMENTS, GET_DEPARTMENT_BY_ID, UPDATE_DEPARTMENT, DELETE_DEPARTMENT
    );

    // Subset relevant to org-less individual trainer/student users
    // (admin keys are irrelevant for org-less individuals — they're never org admins)
    public static final Set<String> TRAINER_KEYS = Set.of(
            GET_TRAINER_BATCHES, GET_TRAINER_DASHBOARD, GET_BATCH_STUDENTS
    );

    public static final Set<String> STUDENT_KEYS = Set.of(
            GET_STUDENT_BATCH, GET_STUDENT_CLASSROOM
    );

    private BatchFeatureKeys() {}
}