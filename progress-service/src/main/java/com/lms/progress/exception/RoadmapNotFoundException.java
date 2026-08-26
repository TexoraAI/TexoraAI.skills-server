package com.lms.progress.exception;

/**
 * Thrown when a requested roadmap-domain entity (template, template node, template
 * resource, org roadmap, org node, org resource, or progress row) cannot be found.
 * Part 2B maps this to 404 NOT_FOUND.
 */
public class RoadmapNotFoundException extends RuntimeException {

    public RoadmapNotFoundException(String message) {
        super(message);
    }
}