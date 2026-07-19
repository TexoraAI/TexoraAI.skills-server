package com.lms.attendance.constants;

import java.util.Set;

public class AttendanceFeatureKeys {

    // ───────── STUDENT ─────────
    public static final String GET_MONTHLY_ATTENDANCE   = "get_monthly_attendance";
    public static final String GET_STUDENT_HISTORY      = "get_student_history";
    public static final String DOWNLOAD_STUDENT_REPORT  = "download_student_report";

    // ───────── TRAINER ─────────
    public static final String MARK_ATTENDANCE                    = "mark_attendance";
    public static final String MARK_TRAINER_SESSION                = "mark_trainer_session";
    public static final String GET_TRAINER_SESSION_HISTORY         = "get_trainer_session_history";
    public static final String GET_TRAINER_HISTORY                 = "get_trainer_history";
    public static final String GET_TRAINER_SESSION_HISTORY_FILTER  = "get_trainer_session_history_filter";
    public static final String DOWNLOAD_TRAINER_REPORT             = "download_trainer_report";

    // ───────── ADMIN ─────────
    public static final String GET_ADMIN_OVERVIEW       = "get_admin_overview";
    public static final String GET_ADMIN_BATCH_DETAIL   = "get_admin_batch_detail";
    public static final String GET_ADMIN_HISTORY        = "get_admin_history";
    public static final String DOWNLOAD_ADMIN_REPORT    = "download_admin_report";

    // NOTE: No SUPER_ADMIN keys — super admin endpoints are never enforced.

    public static final Set<String> STUDENT_KEYS = Set.of(
            GET_MONTHLY_ATTENDANCE, GET_STUDENT_HISTORY, DOWNLOAD_STUDENT_REPORT
    );

    public static final Set<String> TRAINER_KEYS = Set.of(
            MARK_ATTENDANCE, MARK_TRAINER_SESSION, GET_TRAINER_SESSION_HISTORY,
            GET_TRAINER_HISTORY, GET_TRAINER_SESSION_HISTORY_FILTER, DOWNLOAD_TRAINER_REPORT
    );

    public static final Set<String> ADMIN_KEYS = Set.of(
            GET_ADMIN_OVERVIEW, GET_ADMIN_BATCH_DETAIL, GET_ADMIN_HISTORY, DOWNLOAD_ADMIN_REPORT
    );

    public static final Set<String> ALL_KEYS = Set.of(
            GET_MONTHLY_ATTENDANCE, GET_STUDENT_HISTORY, DOWNLOAD_STUDENT_REPORT,
            MARK_ATTENDANCE, MARK_TRAINER_SESSION, GET_TRAINER_SESSION_HISTORY,
            GET_TRAINER_HISTORY, GET_TRAINER_SESSION_HISTORY_FILTER, DOWNLOAD_TRAINER_REPORT,
            GET_ADMIN_OVERVIEW, GET_ADMIN_BATCH_DETAIL, GET_ADMIN_HISTORY, DOWNLOAD_ADMIN_REPORT
    );

    private AttendanceFeatureKeys() {}
}