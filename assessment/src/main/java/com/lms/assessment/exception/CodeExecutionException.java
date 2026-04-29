package com.lms.assessment.exception;

import com.lms.assessment.model.CodeSubmission.ExecutionStatus;

public class CodeExecutionException extends RuntimeException {

    private final ExecutionStatus status;

    public CodeExecutionException(ExecutionStatus status, String message) {
        super(message);
        this.status = status;
    }

    public ExecutionStatus getStatus() {
        return status;
    }
}