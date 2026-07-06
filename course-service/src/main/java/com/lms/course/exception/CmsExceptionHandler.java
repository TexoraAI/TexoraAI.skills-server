package com.lms.course.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

/**
 * Fallback exception handler scoped to CMS resource lookups, only needed
 * if no global @ControllerAdvice already exists in this codebase to
 * attach the CmsResourceNotFoundException handler method to.
 */
@ControllerAdvice
public class CmsExceptionHandler {

    @ExceptionHandler(CmsResourceNotFoundException.class)
    public ResponseEntity<Map<String, Object>> handleCmsResourceNotFound(CmsResourceNotFoundException ex) {
        Map<String, Object> body = new HashMap<>();
        body.put("message", ex.getMessage());
        body.put("timestamp", LocalDateTime.now());
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(body);
    }
}