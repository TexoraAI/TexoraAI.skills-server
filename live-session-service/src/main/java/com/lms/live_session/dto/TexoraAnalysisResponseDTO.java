package com.lms.live_session.dto;

public class TexoraAnalysisResponseDTO {
    private String meetingId;
    private String externalRef;
    private String status;
    private EndedBlock ended;
    private AnalysisBlock analysis;
    private FailureBlock failure;

    public TexoraAnalysisResponseDTO() {}

    public String getMeetingId() { return meetingId; }
    public void setMeetingId(String meetingId) { this.meetingId = meetingId; }

    public String getExternalRef() { return externalRef; }
    public void setExternalRef(String externalRef) { this.externalRef = externalRef; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }

    public EndedBlock getEnded() { return ended; }
    public void setEnded(EndedBlock ended) { this.ended = ended; }

    public AnalysisBlock getAnalysis() { return analysis; }
    public void setAnalysis(AnalysisBlock analysis) { this.analysis = analysis; }

    public FailureBlock getFailure() { return failure; }
    public void setFailure(FailureBlock failure) { this.failure = failure; }

    public static class EndedBlock {
        private String startedAt;
        private String endedAt;

        public EndedBlock() {}
        public EndedBlock(String startedAt, String endedAt) {
            this.startedAt = startedAt;
            this.endedAt = endedAt;
        }

        public String getStartedAt() { return startedAt; }
        public void setStartedAt(String startedAt) { this.startedAt = startedAt; }

        public String getEndedAt() { return endedAt; }
        public void setEndedAt(String endedAt) { this.endedAt = endedAt; }
    }

    public static class AnalysisBlock {
        private Integer analysisVersion;
        private Object analysis;

        public AnalysisBlock() {}
        public AnalysisBlock(Integer analysisVersion, Object analysis) {
            this.analysisVersion = analysisVersion;
            this.analysis = analysis;
        }

        public Integer getAnalysisVersion() { return analysisVersion; }
        public void setAnalysisVersion(Integer analysisVersion) { this.analysisVersion = analysisVersion; }

        public Object getAnalysis() { return analysis; }
        public void setAnalysis(Object analysis) { this.analysis = analysis; }
    }

    public static class FailureBlock {
        private String reason;
        private String message;

        public FailureBlock() {}
        public FailureBlock(String reason, String message) {
            this.reason = reason;
            this.message = message;
        }

        public String getReason() { return reason; }
        public void setReason(String reason) { this.reason = reason; }

        public String getMessage() { return message; }
        public void setMessage(String message) { this.message = message; }
    }
}