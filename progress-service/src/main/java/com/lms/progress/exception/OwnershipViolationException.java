package com.lms.progress.exception;

/**
 * Thrown when a TRAINER attempts to create/update/delete a node or resource on an
 * OrgRoadmap they did not create (OrgRoadmap.createdBy != callerUserId). Part 2B
 * maps this to 403 FORBIDDEN.
 */
public class OwnershipViolationException extends RuntimeException {

    public OwnershipViolationException(String message) {
        super(message);
    }
}