package com.lms.user.dto;

import java.util.List;

/**
 * Response DTOs for AI-powered resume features.
 */
public class AIResumeResponseDTO {

    // ─────────────────────────────────────────────────────────────────────────
    // ATS Tips Response  →  { score, tips[] }
    // ─────────────────────────────────────────────────────────────────────────
    public static class AtsTipsResponse {

        private int        score;
        private List<Tip>  tips;

        public AtsTipsResponse() {}

        public AtsTipsResponse(int score, List<Tip> tips) {
            this.score = score;
            this.tips  = tips;
        }

        public int getScore() { return score; }
        public void setScore(int score) { this.score = score; }

        public List<Tip> getTips() { return tips; }
        public void setTips(List<Tip> tips) { this.tips = tips; }

        @Override
        public String toString() {
            return "AtsTipsResponse{score=" + score + ", tips=" + tips + "}";
        }

        // ── Inner: single tip ─────────────────────────────────────────────────
        public static class Tip {

            /** "error" | "warning" | "success" */
            private String type;
            private String title;
            private String detail;

            public Tip() {}

            public Tip(String type, String title, String detail) {
                this.type   = type;
                this.title  = title;
                this.detail = detail;
            }

            public String getType() { return type; }
            public void setType(String type) { this.type = type; }

            public String getTitle() { return title; }
            public void setTitle(String title) { this.title = title; }

            public String getDetail() { return detail; }
            public void setDetail(String detail) { this.detail = detail; }

            @Override
            public String toString() {
                return "Tip{type='" + type + "', title='" + title + "'}";
            }
        }
    }

    // ─────────────────────────────────────────────────────────────────────────
    // Generic AI Error Wrapper
    // ─────────────────────────────────────────────────────────────────────────
    public static class AIErrorResponse {

        private String error;
        private String message;

        public AIErrorResponse() {}

        public AIErrorResponse(String error, String message) {
            this.error   = error;
            this.message = message;
        }

        public String getError() { return error; }
        public void setError(String error) { this.error = error; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }

        @Override
        public String toString() {
            return "AIErrorResponse{error='" + error + "', message='" + message + "'}";
        }
    }
}