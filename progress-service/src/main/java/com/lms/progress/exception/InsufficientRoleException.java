package com.lms.progress.exception;

/**
 * Thrown when the caller's role is categorically not permitted to invoke an
 * operation (e.g. a non-SUPER_ADMIN calling a template-management method, or a
 * role outside {ADMIN, TENANT_ADMIN, SUPER_ADMIN} calling an org-roadmap method).
 * Distinct from {@link CrossOrgAccessDeniedException}, which is for org-mismatch
 * cases where the role itself is otherwise eligible. Part 2B maps this to
 * 403 FORBIDDEN.
 */
public class InsufficientRoleException extends RuntimeException {

    public InsufficientRoleException(String message) {
        super(message);
    }
}