

package com.lms.live_session.dto;

import java.util.List;


 
public class AiChatRequest {

    private Long sessionId;

    /** Links this message to an existing conversation thread */
    private Long conversationId;

    
    private String mode;

    /** User's typed message (CUSTOM_QUESTION or writing modes) */
    private String message;

    /** Optional extra context the user adds manually */
    private String additionalContext;

    
    private List<String> sources;

    /** IDs of specific resources selected in the context modal */
    private List<Long> resourceIds;

    /** Whether to persist this exchange to conversation history */
    private Boolean saveToHistory;

    public AiChatRequest() {}

    // ── Getters & Setters ─────────────────────────────────────────────────────

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public Long getConversationId() { return conversationId; }
    public void setConversationId(Long conversationId) { this.conversationId = conversationId; }

    public String getMode() { return mode; }
    public void setMode(String mode) { this.mode = mode; }

    public String getMessage() { return message; }
    public void setMessage(String message) { this.message = message; }

    public String getAdditionalContext() { return additionalContext; }
    public void setAdditionalContext(String additionalContext) {
        this.additionalContext = additionalContext;
    }

    public List<String> getSources() { return sources; }
    public void setSources(List<String> sources) { this.sources = sources; }

    public List<Long> getResourceIds() { return resourceIds; }
    public void setResourceIds(List<Long> resourceIds) { this.resourceIds = resourceIds; }

    public Boolean getSaveToHistory() { return saveToHistory; }
    public void setSaveToHistory(Boolean saveToHistory) { this.saveToHistory = saveToHistory; }
}