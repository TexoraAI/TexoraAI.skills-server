
package com.lms.chat.constants;

import java.util.Set;

public class ChatFeatureKeys {

    // ───────── CHAT — role-specific to allow independent toggles ─────────
    public static final String SEND_MESSAGE_TRAINER      = "send_message_trainer";
    public static final String SEND_MESSAGE_STUDENT      = "send_message_student";
    public static final String GET_CONVERSATION_TRAINER  = "get_conversation_trainer";
    public static final String GET_CONVERSATION_STUDENT  = "get_conversation_student";
    public static final String GET_TRAINER_STUDENTS      = "get_trainer_students";
    public static final String GET_STUDENT_TRAINER       = "get_student_trainer";
    public static final String GET_STUDENT_CONTEXT       = "get_student_context";

    // ───────── FEEDBACK — STUDENT ─────────
    public static final String SUBMIT_FEEDBACK            = "submit_feedback";
    public static final String CHECK_FEEDBACK_STATUS      = "check_feedback_status";
    public static final String GET_MY_FEEDBACK            = "get_my_feedback";
    public static final String GET_MY_FEEDBACK_BY_BATCH   = "get_my_feedback_by_batch";

    // ───────── FEEDBACK — TRAINER ─────────
    public static final String GET_TRAINER_FEEDBACK           = "get_trainer_feedback";
    public static final String GET_TRAINER_FEEDBACK_BY_BATCH  = "get_trainer_feedback_by_batch";
    public static final String GET_TRAINER_FEEDBACK_SUMMARY   = "get_trainer_feedback_summary";

    // ───────── FEEDBACK — ADMIN (org admin only, NOT super admin) ─────────
    public static final String GET_BATCH_FEEDBACK          = "get_batch_feedback";
    public static final String GET_BATCH_SUMMARIES         = "get_batch_summaries";
    public static final String UPDATE_FEEDBACK_STATUS      = "update_feedback_status";
    public static final String CREATE_UPDATE_ALERT_CONFIG  = "create_update_alert_config";
    public static final String GET_ALERT_CONFIG            = "get_alert_config";
    public static final String DELETE_ALERT_CONFIG         = "delete_alert_config";

    // ───────── NOTEBOOK AI (student-facing) ─────────
    public static final String GET_MY_NOTEBOOKS   = "get_my_notebooks";
    public static final String GET_NOTEBOOK       = "get_notebook";
    public static final String CREATE_NOTEBOOK    = "create_notebook";
    public static final String UPDATE_NOTEBOOK    = "update_notebook";
    public static final String DELETE_NOTEBOOK    = "delete_notebook";
    public static final String ADD_SECTION        = "add_section";
    public static final String UPDATE_SECTION     = "update_section";
    public static final String DELETE_SECTION     = "delete_section";
    public static final String ADD_PAGE           = "add_page";
    public static final String SAVE_PAGE          = "save_page";
    public static final String DELETE_PAGE        = "delete_page";
    public static final String ADD_URL_SOURCE     = "add_url_source";
    public static final String ADD_FILE_SOURCE    = "add_file_source";
    public static final String DELETE_SOURCE      = "delete_source";
    public static final String NOTEBOOK_AI_CHAT   = "notebook_ai_chat";

    // ───────── GROUPINGS ─────────

    public static final Set<String> TRAINER_KEYS = Set.of(
            SEND_MESSAGE_TRAINER, GET_CONVERSATION_TRAINER, GET_TRAINER_STUDENTS,
            GET_TRAINER_FEEDBACK, GET_TRAINER_FEEDBACK_BY_BATCH, GET_TRAINER_FEEDBACK_SUMMARY
    );

    public static final Set<String> STUDENT_KEYS = Set.of(
            SEND_MESSAGE_STUDENT, GET_CONVERSATION_STUDENT, GET_STUDENT_TRAINER, GET_STUDENT_CONTEXT,
            SUBMIT_FEEDBACK, CHECK_FEEDBACK_STATUS, GET_MY_FEEDBACK, GET_MY_FEEDBACK_BY_BATCH,
            GET_MY_NOTEBOOKS, GET_NOTEBOOK, CREATE_NOTEBOOK, UPDATE_NOTEBOOK, DELETE_NOTEBOOK,
            ADD_SECTION, UPDATE_SECTION, DELETE_SECTION, ADD_PAGE, SAVE_PAGE, DELETE_PAGE,
            ADD_URL_SOURCE, ADD_FILE_SOURCE, DELETE_SOURCE, NOTEBOOK_AI_CHAT
    );

    public static final Set<String> ADMIN_KEYS = Set.of(
            GET_BATCH_FEEDBACK, GET_BATCH_SUMMARIES, UPDATE_FEEDBACK_STATUS,
            CREATE_UPDATE_ALERT_CONFIG, GET_ALERT_CONFIG, DELETE_ALERT_CONFIG
    );

    public static final Set<String> NOTEBOOK_KEYS = Set.of(
            GET_MY_NOTEBOOKS, GET_NOTEBOOK, CREATE_NOTEBOOK, UPDATE_NOTEBOOK, DELETE_NOTEBOOK,
            ADD_SECTION, UPDATE_SECTION, DELETE_SECTION, ADD_PAGE, SAVE_PAGE, DELETE_PAGE,
            ADD_URL_SOURCE, ADD_FILE_SOURCE, DELETE_SOURCE, NOTEBOOK_AI_CHAT
    );

    // Full set — used to build default (all-enabled) flags.
    // NOTE: Super Admin endpoints intentionally have NO keys here — they are
    // never enforced, so they don't need to appear in the default flag set.
    public static final Set<String> ALL_KEYS = Set.of(
            SEND_MESSAGE_TRAINER, SEND_MESSAGE_STUDENT,
            GET_CONVERSATION_TRAINER, GET_CONVERSATION_STUDENT,
            GET_TRAINER_STUDENTS, GET_STUDENT_TRAINER, GET_STUDENT_CONTEXT,
            SUBMIT_FEEDBACK, CHECK_FEEDBACK_STATUS, GET_MY_FEEDBACK, GET_MY_FEEDBACK_BY_BATCH,
            GET_TRAINER_FEEDBACK, GET_TRAINER_FEEDBACK_BY_BATCH, GET_TRAINER_FEEDBACK_SUMMARY,
            GET_BATCH_FEEDBACK, GET_BATCH_SUMMARIES, UPDATE_FEEDBACK_STATUS,
            CREATE_UPDATE_ALERT_CONFIG, GET_ALERT_CONFIG, DELETE_ALERT_CONFIG,
            GET_MY_NOTEBOOKS, GET_NOTEBOOK, CREATE_NOTEBOOK, UPDATE_NOTEBOOK, DELETE_NOTEBOOK,
            ADD_SECTION, UPDATE_SECTION, DELETE_SECTION, ADD_PAGE, SAVE_PAGE, DELETE_PAGE,
            ADD_URL_SOURCE, ADD_FILE_SOURCE, DELETE_SOURCE, NOTEBOOK_AI_CHAT
    );

    private ChatFeatureKeys() {}
}