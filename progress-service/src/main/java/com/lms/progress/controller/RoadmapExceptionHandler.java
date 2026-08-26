package com.lms.progress.controller;

import com.lms.progress.dto.ApiErrorResponse;
import com.lms.progress.exception.CrossOrgAccessDeniedException;
import com.lms.progress.exception.DuplicateSlugException;
import com.lms.progress.exception.InsufficientRoleException;
import com.lms.progress.exception.OwnershipViolationException;
import com.lms.progress.exception.RoadmapCycleException;
import com.lms.progress.exception.RoadmapNotFoundException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@RestControllerAdvice(basePackages = "com.lms.progress.controller")
public class RoadmapExceptionHandler {

    private static final Logger log = LoggerFactory.getLogger(RoadmapExceptionHandler.class);

    @ExceptionHandler(CrossOrgAccessDeniedException.class)
    public ResponseEntity<ApiErrorResponse> handleCrossOrgAccessDenied(CrossOrgAccessDeniedException ex) {
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN_CROSS_ORG_ACCESS", ex.getMessage());
    }

    @ExceptionHandler(RoadmapNotFoundException.class)
    public ResponseEntity<ApiErrorResponse> handleRoadmapNotFound(RoadmapNotFoundException ex) {
        return build(HttpStatus.NOT_FOUND, "NOT_FOUND", ex.getMessage());
    }

    @ExceptionHandler(OwnershipViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleOwnershipViolation(OwnershipViolationException ex) {
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN_NOT_OWNER", ex.getMessage());
    }

    @ExceptionHandler(RoadmapCycleException.class)
    public ResponseEntity<ApiErrorResponse> handleRoadmapCycle(RoadmapCycleException ex) {
        return build(HttpStatus.BAD_REQUEST, "INVALID_NODE_GRAPH", ex.getMessage());
    }

    @ExceptionHandler(DuplicateSlugException.class)
    public ResponseEntity<ApiErrorResponse> handleDuplicateSlug(DuplicateSlugException ex) {
        return build(HttpStatus.CONFLICT, "SLUG_CONFLICT", ex.getMessage());
    }

    @ExceptionHandler(InsufficientRoleException.class)
    public ResponseEntity<ApiErrorResponse> handleInsufficientRole(InsufficientRoleException ex) {
        return build(HttpStatus.FORBIDDEN, "FORBIDDEN_ROLE", ex.getMessage());
    }

    /**
     * Persistence-layer failures (not-null / unique / FK / type constraint violations).
     * These are almost always a data or schema problem rather than a client mistake, so we
     * log the full stack AND surface the most-specific DB cause message to the caller. This
     * is what makes an otherwise-opaque 500 diagnosable — Postgres puts the offending column
     * name in the message (e.g. "null value in column \"x\" violates not-null constraint").
     */
    @ExceptionHandler(DataIntegrityViolationException.class)
    public ResponseEntity<ApiErrorResponse> handleDataIntegrityViolation(DataIntegrityViolationException ex) {
        Throwable root = ex.getMostSpecificCause();
        String detail = root != null ? root.getMessage() : ex.getMessage();
        log.error("Data integrity violation while persisting roadmap entity: {}", detail, ex);
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "DATA_INTEGRITY_VIOLATION",
                detail != null ? detail : "Database constraint violation.");
    }

    // Generic fallback — must stay last so specific handlers above take precedence.
    // Logs the full stack trace so unexpected 500s are never silent in the server log.
    @ExceptionHandler(RuntimeException.class)
    public ResponseEntity<ApiErrorResponse> handleGenericRuntimeException(RuntimeException ex) {
        log.error("Unhandled runtime exception in roadmap controller layer", ex);
        String message = ex.getMessage() != null ? ex.getMessage() : ex.getClass().getSimpleName();
        return build(HttpStatus.INTERNAL_SERVER_ERROR, "INTERNAL_ERROR", message);
    }

    private ResponseEntity<ApiErrorResponse> build(HttpStatus status, String code, String message) {
        ApiErrorResponse response = new ApiErrorResponse(code, message, status.value());
        return ResponseEntity.status(status).body(response);
    }
}