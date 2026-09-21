package com.lms.video.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

// WHY: translates the plan-tier limit exceptions raised by VideoService into
// consistent, machine-readable JSON error bodies for the frontend.
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(VideoSizeLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleVideoSizeLimit(VideoSizeLimitExceededException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "VIDEO_SIZE_LIMIT_EXCEEDED");
        body.put("tier", e.getTier());
        body.put("actualSizeBytes", e.getActualSize());
        body.put("maxAllowedBytes", e.getMaxAllowed());
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(body);
    }

    @ExceptionHandler(VideoStorageLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleStorageLimit(VideoStorageLimitExceededException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "STORAGE_LIMIT_EXCEEDED");
        body.put("tier", e.getTier());
        body.put("currentUsageBytes", e.getCurrentUsage());
        body.put("attemptedAddBytes", e.getAttemptedAdd());
        body.put("capacityBytes", e.getCapacity());
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(VideoCountLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleVideoCountLimit(VideoCountLimitExceededException e) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "VIDEO_COUNT_LIMIT_EXCEEDED");
        body.put("tier", e.getTier());
        body.put("currentCount", e.getCurrentCount());
        body.put("maxAllowed", e.getMaxAllowed());
        body.put("message", e.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
}