package com.lms.file.constants;

import java.util.Set;

public class FileFeatureKeys {

    // ───────── TRAINER ─────────
    public static final String UPLOAD_FILE       = "upload_file";
    public static final String EDIT_FILE         = "edit_file";
    public static final String DELETE_FILE       = "delete_file";
    public static final String ASSIGN_BATCH      = "assign_batch";
    public static final String PUBLISH_FILE      = "publish_file";
    public static final String GET_TRAINER_FILES = "get_trainer_files";

    // ───────── STUDENT ─────────
    public static final String GET_STUDENT_FILES = "get_student_files";
    public static final String DOWNLOAD_FILE     = "download_file";
    public static final String VIEW_FILE         = "view_file";

    // ───────── ADMIN ─────────
    public static final String GET_ALL_FILES     = "get_all_files";

    // Full set — used to build default (all-enabled) flags
    public static final Set<String> ALL_KEYS = Set.of(
            UPLOAD_FILE, EDIT_FILE, DELETE_FILE, ASSIGN_BATCH, PUBLISH_FILE,
            GET_TRAINER_FILES, GET_STUDENT_FILES, DOWNLOAD_FILE, VIEW_FILE,
            GET_ALL_FILES
    );

    public static final Set<String> TRAINER_KEYS = Set.of(
            UPLOAD_FILE, EDIT_FILE, DELETE_FILE, ASSIGN_BATCH,
            PUBLISH_FILE, GET_TRAINER_FILES
    );

    public static final Set<String> STUDENT_KEYS = Set.of(
            GET_STUDENT_FILES, DOWNLOAD_FILE, VIEW_FILE
    );

    public static final Set<String> ADMIN_KEYS = Set.of(
            GET_ALL_FILES, DELETE_FILE
    );

    private FileFeatureKeys() {}
}