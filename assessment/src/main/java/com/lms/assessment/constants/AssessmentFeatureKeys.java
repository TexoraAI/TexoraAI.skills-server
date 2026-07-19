package com.lms.assessment.constants;

import java.util.Set;

public class AssessmentFeatureKeys {

    // ───────── TRAINER (4) ─────────
    public static final String CREATE_QUIZ           = "create_quiz";
    public static final String CREATE_ASSIGNMENT     = "create_assignment";
    public static final String CREATE_CODING_PROBLEM = "create_coding_problem";
    public static final String CREATE_STUDY_PLAN     = "create_study_plan";

    // ───────── STUDENT (4) ─────────
    public static final String ATTEMPT_QUIZ          = "attempt_quiz";
    public static final String SUBMIT_ASSIGNMENT     = "submit_assignment";
    public static final String SOLVE_CODING_PROBLEM  = "solve_coding_problem";
    public static final String ACCESS_STUDY_PLAN     = "access_study_plan";

    // ───────── ADMIN (4) — TENANT_ADMIN "/admin" report endpoints only ─────────
    // These are SEPARATE from the trainer/student keys above on purpose:
    // an org disabling "Create Quiz" for trainers must NOT also blind that
    // org's own admin from viewing the quiz report. SUPER_ADMIN ("/superadmin")
    // endpoints are NEVER gated and have no key at all.
    public static final String VIEW_QUIZ_ADMIN_REPORT           = "view_quiz_admin_report";
    public static final String VIEW_ASSIGNMENT_ADMIN_REPORT     = "view_assignment_admin_report";
    public static final String VIEW_CODING_PROBLEM_ADMIN_REPORT = "view_coding_problem_admin_report";
    public static final String VIEW_STUDY_PLAN_ADMIN_REPORT     = "view_study_plan_admin_report";

    // Full set — used to build default (all-enabled) flags.
    public static final Set<String> ALL_KEYS = Set.of(
            CREATE_QUIZ, CREATE_ASSIGNMENT, CREATE_CODING_PROBLEM, CREATE_STUDY_PLAN,
            ATTEMPT_QUIZ, SUBMIT_ASSIGNMENT, SOLVE_CODING_PROBLEM, ACCESS_STUDY_PLAN,
            VIEW_QUIZ_ADMIN_REPORT, VIEW_ASSIGNMENT_ADMIN_REPORT,
            VIEW_CODING_PROBLEM_ADMIN_REPORT, VIEW_STUDY_PLAN_ADMIN_REPORT
    );

    public static final Set<String> TRAINER_KEYS = Set.of(
            CREATE_QUIZ, CREATE_ASSIGNMENT, CREATE_CODING_PROBLEM, CREATE_STUDY_PLAN
    );

    public static final Set<String> STUDENT_KEYS = Set.of(
            ATTEMPT_QUIZ, SUBMIT_ASSIGNMENT, SOLVE_CODING_PROBLEM, ACCESS_STUDY_PLAN
    );

    public static final Set<String> ADMIN_KEYS = Set.of(
            VIEW_QUIZ_ADMIN_REPORT, VIEW_ASSIGNMENT_ADMIN_REPORT,
            VIEW_CODING_PROBLEM_ADMIN_REPORT, VIEW_STUDY_PLAN_ADMIN_REPORT
    );

    private AssessmentFeatureKeys() {}
}