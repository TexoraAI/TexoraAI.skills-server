//package com.lms.assessment.exception;
//
//import com.lms.assessment.model.CodeSubmission.ExecutionStatus;
//import org.springframework.http.HttpStatus;
//import org.springframework.http.ResponseEntity;
//import org.springframework.web.bind.MethodArgumentNotValidException;
//import org.springframework.web.bind.annotation.ExceptionHandler;
//import org.springframework.web.bind.annotation.RestControllerAdvice;
//
//import java.time.LocalDateTime;
//import java.util.LinkedHashMap;
//import java.util.Map;
//
//@RestControllerAdvice
//public class GlobalCodeExceptionHandler {
//
//    @ExceptionHandler(CodeExecutionException.class)
//    public ResponseEntity<Map<String, Object>> handleCodeExecutionException(
//            CodeExecutionException ex) {
//
//        HttpStatus httpStatus = switch (ex.getStatus()) {
//            case INVALID_LANGUAGE -> HttpStatus.BAD_REQUEST;
//            case TIMEOUT          -> HttpStatus.REQUEST_TIMEOUT;
//            default               -> HttpStatus.UNPROCESSABLE_ENTITY;
//        };
//
//        Map<String, Object> body = new LinkedHashMap<>();
//        body.put("timestamp", LocalDateTime.now().toString());
//        body.put("status",    ex.getStatus().name());
//        body.put("message",   ex.getMessage());
//
//        return ResponseEntity.status(httpStatus).body(body);
//    }
//
//    @ExceptionHandler(MethodArgumentNotValidException.class)
//    public ResponseEntity<Map<String, Object>> handleValidation(
//            MethodArgumentNotValidException ex) {
//
//        String firstError = ex.getBindingResult()
//            .getFieldErrors()
//            .stream()
//            .findFirst()
//            .map(e -> e.getField() + ": " + e.getDefaultMessage())
//            .orElse("Validation failed");
//
//        Map<String, Object> body = new LinkedHashMap<>();
//        body.put("timestamp", LocalDateTime.now().toString());
//        body.put("status",    ExecutionStatus.INVALID_LANGUAGE.name());
//        body.put("message",   firstError);
//
//        return ResponseEntity.badRequest().body(body);
//    }
//}


package com.lms.assessment.exception;

import com.lms.assessment.model.CodeSubmission.ExecutionStatus;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.LinkedHashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalCodeExceptionHandler {

    // ── Existing: Code execution errors ───────────────────────────────────
    @ExceptionHandler(CodeExecutionException.class)
    public ResponseEntity<Map<String, Object>> handleCodeExecutionException(
            CodeExecutionException ex) {
        HttpStatus httpStatus = switch (ex.getStatus()) {
            case INVALID_LANGUAGE -> HttpStatus.BAD_REQUEST;
            case TIMEOUT          -> HttpStatus.REQUEST_TIMEOUT;
            default               -> HttpStatus.UNPROCESSABLE_ENTITY;
        };
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    ex.getStatus().name());
        body.put("message",   ex.getMessage());
        return ResponseEntity.status(httpStatus).body(body);
    }

    // ── Existing: Validation errors ───────────────────────────────────────
    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<Map<String, Object>> handleValidation(
            MethodArgumentNotValidException ex) {
        String firstError = ex.getBindingResult()
            .getFieldErrors()
            .stream()
            .findFirst()
            .map(e -> e.getField() + ": " + e.getDefaultMessage())
            .orElse("Validation failed");
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    ExecutionStatus.INVALID_LANGUAGE.name());
        body.put("message",   firstError);
        return ResponseEntity.badRequest().body(body);
    }

    // ── NEW: Problem already assigned → 409 Conflict ──────────────────────
    @ExceptionHandler(AlreadyAssignedException.class)
    public ResponseEntity<Map<String, Object>> handleAlreadyAssigned(
            AlreadyAssignedException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    "ALREADY_ASSIGNED");
        body.put("message",   ex.getMessage());
        return ResponseEntity.status(HttpStatus.CONFLICT).body(body);
    }

    // ── NEW: Generic not-found / bad input → 400 ─────────────────────────
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<Map<String, Object>> handleRuntime(
            RuntimeException ex) {
        Map<String, Object> body = new LinkedHashMap<>();
        body.put("timestamp", LocalDateTime.now().toString());
        body.put("status",    "BAD_REQUEST");
        body.put("message",   ex.getMessage());
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(body);
    }
}