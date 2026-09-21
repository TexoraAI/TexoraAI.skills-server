package com.lms.progress.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(RoadmapUsageLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleRoadmapUsageLimitExceeded(
            RoadmapUsageLimitExceededException ex) {
        Map<String, Object> body = Map.of(
                "error", "ROADMAP_LIMIT_EXCEEDED",
                "ownerId", ex.getOwnerId(),
                "tier", ex.getTier(),
                "currentCount", ex.getCurrentCount(),
                "maxAllowed", ex.getMaxAllowed(),
                "period", ex.getPeriod(),
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }
}