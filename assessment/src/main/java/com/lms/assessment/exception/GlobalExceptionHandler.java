package com.lms.assessment.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(AssessmentUsageLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleUsageLimitExceeded(AssessmentUsageLimitExceededException ex) {
        Map<String, Object> body = Map.of(
                "error", "ASSESSMENT_USAGE_LIMIT_EXCEEDED",
                "email", ex.getEmail(),
                "action", ex.getAction(),
                "tier", ex.getTier(),
                "currentCount", ex.getCurrentCount(),
                "maxAllowed", ex.getMaxAllowed(),
                "period", ex.getPeriod(),
                "message", ex.getMessage()
        );
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }
}