package com.lms.video.constants;

import java.util.Set;

public class VideoFeatureKeys {

    // ───────── TRAINER ─────────
    public static final String UPLOAD_VIDEO         = "upload_video";
    public static final String UPLOAD_VIDEO_URL     = "upload_video_url";
    public static final String EDIT_VIDEO           = "edit_video";
    public static final String EDIT_VIDEO_URL       = "edit_video_url";
    public static final String DELETE_VIDEO         = "delete_video";
    public static final String ASSIGN_BATCH         = "assign_batch";
    public static final String PUBLISH_VIDEO        = "publish_video";

    // ───────── STUDENT ─────────
    public static final String GET_STUDENT_VIDEOS   = "get_student_videos";

    // ───────── ADMIN / TRAINER (read) ─────────
    public static final String GET_ALL_VIDEOS       = "get_all_videos";
    public static final String GET_TRAINER_VIDEOS   = "get_trainer_videos";
    public static final String PLAY_VIDEO           = "play_video";

    // Full set — used to build default (all-enabled) flags
    public static final Set<String> ALL_KEYS = Set.of(
            UPLOAD_VIDEO, UPLOAD_VIDEO_URL, EDIT_VIDEO, EDIT_VIDEO_URL,
            DELETE_VIDEO, ASSIGN_BATCH, PUBLISH_VIDEO,
            GET_STUDENT_VIDEOS, GET_ALL_VIDEOS, GET_TRAINER_VIDEOS, PLAY_VIDEO
    );

    public static final Set<String> TRAINER_KEYS = Set.of(
            UPLOAD_VIDEO, UPLOAD_VIDEO_URL, EDIT_VIDEO, EDIT_VIDEO_URL,
            DELETE_VIDEO, ASSIGN_BATCH, PUBLISH_VIDEO, GET_TRAINER_VIDEOS
    );

    public static final Set<String> STUDENT_KEYS = Set.of(
            GET_STUDENT_VIDEOS, PLAY_VIDEO
    );

    public static final Set<String> ADMIN_KEYS = Set.of(
            GET_ALL_VIDEOS, DELETE_VIDEO
    );

    private VideoFeatureKeys() {}
}