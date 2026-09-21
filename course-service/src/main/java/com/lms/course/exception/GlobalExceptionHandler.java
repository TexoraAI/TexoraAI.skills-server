package com.lms.course.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(CourseCountLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleCourseLimit(CourseCountLimitExceededException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "COURSE_LIMIT_EXCEEDED");
        body.put("tier", ex.getTier());
        body.put("currentCount", ex.getCurrentCount());
        body.put("maxAllowed", ex.getMaxAllowed());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }

    @ExceptionHandler(ModuleCountLimitExceededException.class)
    public ResponseEntity<Map<String, Object>> handleModuleLimit(ModuleCountLimitExceededException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("error", "MODULE_LIMIT_EXCEEDED");
        body.put("courseId", ex.getCourseId());
        body.put("tier", ex.getTier());
        body.put("currentCount", ex.getCurrentCount());
        body.put("maxAllowed", ex.getMaxAllowed());
        body.put("message", ex.getMessage());
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(body);
    }
}