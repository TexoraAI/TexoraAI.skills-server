

package com.lms.live_session.dto;

import java.util.List;


public class AiChatResponse {

    private Long conversationId;
    private Long messageId;
    private String response;
    private String mode;
    private Long sessionId;
    private String sessionTitle;
    private boolean success;
    private String error;
    private List<String> sourcesUsed;

    public AiChatResponse() {}

    /** Success constructor */
    public AiChatResponse(String response, String mode, Long sessionId, String sessionTitle) {
        this.response = response;
        this.mode = mode;
        this.sessionId = sessionId;
        this.sessionTitle = sessionTitle;
        this.success = true;
    }

    /** Full constructor with conversation tracking */
    public AiChatResponse(Long conversationId, Long messageId, String response,
                          String mode, Long sessionId, String sessionTitle,
                          List<String> sourcesUsed) {
        this.conversationId = conversationId;
        this.messageId = messageId;
        this.response = response;
        this.mode = mode;
        this.sessionId = sessionId;
        this.sessionTitle = sessionTitle;
        this.sourcesUsed = sourcesUsed;
        this.success = true;
    }

    public static AiChatResponse error(String errorMsg) {
        AiChatResponse res = new AiChatResponse();
        res.success = false;
        res.error = errorMsg;
        return res;
    }

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public Long getMessageId() { return messageId; }
    public void setMessageId(Long messageId) { this.messageId = messageId; }

    public String getResponse() { return response; }
    public void setResponse(String response) { this.response = response; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getSessionTitle() { return sessionTitle; }
    public void setSessionTitle(String sessionTitle) { this.sessionTitle = sessionTitle; }

    public boolean isSuccess() { return success; }
    public void setSuccess(boolean success) { this.success = success; }

    public String getError() { return error; }
    public void setError(String error) { this.error = error; }

    public List<String> getSourcesUsed() { return sourcesUsed; }
    public void setSourcesUsed(List<String> sourcesUsed) { this.sourcesUsed = sourcesUsed; }
}