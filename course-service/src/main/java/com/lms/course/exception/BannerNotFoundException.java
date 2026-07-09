package com.lms.course.exception;

/**
 * Thrown when a requested Banner Studio banner does not exist.
 * Follows the same lightweight runtime-exception pattern used elsewhere
 * in the service layer (see OpenAIService), so no extra exception-handling
 * package is introduced.
 */
public class BannerNotFoundException extends RuntimeException {

    public BannerNotFoundException(Long id) {
        super("Banner not found with id: " + id);
    }

    public BannerNotFoundException(String message) {
        super(message);
    }
}