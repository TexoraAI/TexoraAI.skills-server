package com.lms.course.exception;

/**
 * Thrown when a requested CMS resource (page, section, component, media
 * asset, or nav item) cannot be found. Picked up by the existing global
 * exception handler and mapped to HTTP 404 — see the
 * {@code @ExceptionHandler(CmsResourceNotFoundException.class)} method
 * added to that handler alongside this class.
 */
public class CmsResourceNotFoundException extends RuntimeException {

    public CmsResourceNotFoundException(String message) {
        super(message);
    }
}