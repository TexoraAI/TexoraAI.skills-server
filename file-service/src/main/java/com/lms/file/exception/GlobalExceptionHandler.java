package com.lms.file.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(FileSizeLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleFileSizeLimitExceeded(FileSizeLimitExceededException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "FILE_SIZE_LIMIT_EXCEEDED");
        body.put("tier", ex.getTier());
        body.put("actualSizeBytes", ex.getActualSize());
        body.put("maxAllowedBytes", ex.getMaxAllowed());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.PAYLOAD_TOO_LARGE).body(body);
    }

    @ExceptionHandler(FileStorageLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleFileStorageLimitExceeded(FileStorageLimitExceededException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "STORAGE_LIMIT_EXCEEDED");
        body.put("tier", ex.getTier());
        body.put("currentUsageBytes", ex.getCurrentUsage());
        body.put("attemptedAddBytes", ex.getAttemptedAdd());
        body.put("capacityBytes", ex.getCapacity());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(FileCountLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleFileCountLimitExceeded(FileCountLimitExceededException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "FILE_COUNT_LIMIT_EXCEEDED");
        body.put("tier", ex.getTier());
        body.put("currentCount", ex.getCurrentCount());
        body.put("maxAllowed", ex.getMaxAllowed());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
}
