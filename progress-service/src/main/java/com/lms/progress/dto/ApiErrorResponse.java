package com.lms.progress.dto;

/**
 * Top-level error envelope. Serializes to:
 * { "error": { "code": "FORBIDDEN_CROSS_ORG_ACCESS", "message": "...", "status": 403 } }
 */
public class ApiErrorResponse {

    private ApiError error;

    public ApiErrorResponse() {
    }

    public ApiErrorResponse(ApiError error) {
        this.error = error;
    }

    public ApiErrorResponse(String code, String message, int status) {
        this.error = new ApiError(code, message, status);
    }

    public ApiError getError() {
        return error;
    }

    public void setError(ApiError error) {
        this.error = error;
    }

    public static class ApiError {

        private String code;
        private String message;
        private int status;

        public ApiError() {
        }

        public ApiError(String code, String message, int status) {
            this.code = code;
            this.message = message;
            this.status = status;
        }

        public String getCode() {
            return code;
        }

        public void setCode(String code) {
            this.code = code;
        }

        public String getMessage() {
            return message;
        }

        public void setMessage(String message) {
            this.message = message;
        }

        public int getStatus() {
            return status;
        }

        public void setStatus(int status) {
            this.status = status;
        }
    }
}
