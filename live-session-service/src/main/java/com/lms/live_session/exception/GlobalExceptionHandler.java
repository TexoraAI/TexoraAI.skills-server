package com.lms.live_session.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * NOTE: I don't have the existing live-session-service source tree in
 * context, so I can't confirm whether a GlobalExceptionHandler already
 * exists in this package. If one does, merge these six @ExceptionHandler
 * methods into it instead of adding this file, to avoid two
 * @RestControllerAdvice beans handling overlapping exception types.
 */
@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(LiveClassLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleLiveClassLimitExceeded(LiveClassLimitExceededException ex) {
        return build(HttpStatus.TOO_MANY_REQUESTS, "CLASS_LIMIT_EXCEEDED", ex.getMessage(), Map.of(
                "email", ex.getEmail(),
                "tier", ex.getTier(),
                "currentCount", ex.getCurrentCount(),
                "maxAllowed", ex.getMaxAllowed(),
                "period", ex.getPeriod()
        ));
    }

    @ExceptionHandler(AiCompanionNotAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleAiCompanionNotAvailable(AiCompanionNotAvailableException ex) {
        return build(HttpStatus.FORBIDDEN, "AI_COMPANION_NOT_AVAILABLE", ex.getMessage(), Map.of(
                "email", ex.getEmail(),
                "tier", ex.getTier()
        ));
    }

    @ExceptionHandler(AiCompanionUsageLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleAiCompanionUsageLimitExceeded(AiCompanionUsageLimitExceededException ex) {
        return build(HttpStatus.TOO_MANY_REQUESTS, "AI_COMPANION_LIMIT_EXCEEDED", ex.getMessage(), Map.of(
                "email", ex.getEmail(),
                "tier", ex.getTier(),
                "currentCount", ex.getCurrentCount(),
                "maxAllowed", ex.getMaxAllowed(),
                "period", ex.getPeriod()
        ));
    }

    @ExceptionHandler(RecordingStorageLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRecordingStorageLimitExceeded(RecordingStorageLimitExceededException ex) {
        return build(HttpStatus.FORBIDDEN, "STORAGE_LIMIT_EXCEEDED", ex.getMessage(), Map.of(
                "tier", ex.getTier(),
                "currentUsage", ex.getCurrentUsage(),
                "attemptedAdd", ex.getAttemptedAdd(),
                "capacity", ex.getCapacity()
        ));
    }

    @ExceptionHandler(RecordingDurationLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRecordingDurationLimitExceeded(RecordingDurationLimitExceededException ex) {
        return build(HttpStatus.FORBIDDEN, "RECORDING_DURATION_LIMIT_EXCEEDED", ex.getMessage(), Map.of(
                "tier", ex.getTier(),
                "actualMinutes", ex.getActualMinutes(),
                "maxAllowed", ex.getMaxAllowed()
        ));
    }

    @ExceptionHandler(WhiteboardNotAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleWhiteboardNotAvailable(WhiteboardNotAvailableException ex) {
        return build(HttpStatus.FORBIDDEN, "WHITEBOARD_NOT_AVAILABLE", ex.getMessage(), Map.of(
                "email", ex.getEmail(),
                "tier", ex.getTier()
        ));
    }

    @ExceptionHandler(MeetingLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleMeetingLimitExceeded(MeetingLimitExceededException ex) {
        return build(HttpStatus.TOO_MANY_REQUESTS, "MEETING_LIMIT_EXCEEDED", ex.getMessage(), Map.of(
                "email", ex.getEmail(),
                "tier", ex.getTier(),
                "currentCount", ex.getCurrentCount(),
                "maxAllowed", ex.getMaxAllowed(),
                "period", ex.getPeriod()
        ));
    }

    @ExceptionHandler(EmailDispatchLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleEmailDispatchLimitExceeded(EmailDispatchLimitExceededException ex) {
        return build(HttpStatus.TOO_MANY_REQUESTS, "EMAIL_DISPATCH_LIMIT_EXCEEDED", ex.getMessage(), Map.of(
                "email", ex.getEmail(),
                "tier", ex.getTier(),
                "currentCount", ex.getCurrentCount(),
                "maxAllowed", ex.getMaxAllowed(),
                "period", ex.getPeriod()
        ));
    }

    @ExceptionHandler(CalendarSyncNotAvailableException.class)
    public ResponseEntity<Map<String, Object>> handleCalendarSyncNotAvailable(CalendarSyncNotAvailableException ex) {
        return build(HttpStatus.FORBIDDEN, "CALENDAR_SYNC_NOT_AVAILABLE", ex.getMessage(), Map.of(
                "email", ex.getEmail(),
                "tier", ex.getTier()
        ));
    }

    @ExceptionHandler(CalendarResyncLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleCalendarResyncLimitExceeded(CalendarResyncLimitExceededException ex) {
        return build(HttpStatus.TOO_MANY_REQUESTS, "CALENDAR_RESYNC_LIMIT_EXCEEDED", ex.getMessage(), Map.of(
                "email", ex.getEmail(),
                "tier", ex.getTier(),
                "currentCount", ex.getCurrentCount(),
                "maxAllowed", ex.getMaxAllowed(),
                "period", ex.getPeriod()
        ));
    }

    private ResponseEntity<Map<String, Object>> build(HttpStatus status, String errorCode, String message,
                                                        Map<String, Object> details) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", errorCode);
        body.put("message", message);
        body.put("timestamp", LocalDateTime.now().toString());
        body.putAll(details);
        return ResponseEntity.status(status).body(body);
    }
}
