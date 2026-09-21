package com.lms.chat.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(NotebookUsageLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleNotebookUsageLimitExceeded(
            NotebookUsageLimitExceededException ex) {

        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "NOTEBOOK_USAGE_LIMIT_REACHED");
        body.put("studentEmail", ex.getStudentEmail());
        body.put("tier", ex.getTier());
        body.put("currentCount", ex.getCurrentCount());
        body.put("maxAllowed", ex.getMaxAllowed());
        body.put("period", ex.getPeriod());
        body.put("message", ex.getMessage());

        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS).body(body);
    }
}