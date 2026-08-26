package com.lms.progress.exception;

/**
 * Thrown when an ADMIN/TENANT_ADMIN caller's JWT organizationId does not match the
 * target OrgRoadmap.orgId, or when an ADMIN/TENANT_ADMIN attempts to touch a
 * null-org (orgId == null) roadmap, which is reserved for SUPER_ADMIN. Part 2B maps
 * this to 403 FORBIDDEN.
 */
public class CrossOrgAccessDeniedException extends RuntimeException {

    public CrossOrgAccessDeniedException(String message) {
        super(message);
    }
}