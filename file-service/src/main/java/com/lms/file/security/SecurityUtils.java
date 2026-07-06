package com.lms.file.security;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

/**
 * Small helper so service-layer code can read the current request's
 * organizationId the same way it already reads the trainer/student email
 * via SecurityContextHolder.getContext().getAuthentication().getName().
 *
 * Returns null for standalone users — callers must treat null as
 * "skip tenant validation", per existing standalone-user behavior.
 */
public class SecurityUtils {

    public static String getCurrentOrganizationId() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;

        HttpServletRequest request = attrs.getRequest();
        Object orgId = request.getAttribute("organizationId");
        return orgId != null ? orgId.toString() : null;
    }
 // ADD this method to the existing class
    public static String getCurrentRole() {
        ServletRequestAttributes attrs =
                (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attrs == null) return null;

        HttpServletRequest request = attrs.getRequest();
        Object role = request.getAttribute("role");
        return role != null ? role.toString() : null;
    }
}