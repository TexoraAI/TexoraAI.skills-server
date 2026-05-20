package com.lms.live_session.dto;

public class WhiteboardEvent {

    private Long sessionId;

    // EventType: DRAW | FULL_STATE | CLEAR | CURSOR | POINTER
    private String eventType;

    // Excalidraw elements JSON string
    // Contains full serialized Excalidraw elements array
    private String elements;

    // App state (viewport, zoom, etc.)
    private String appState;

    // Files (images embedded in whiteboard)
    private String files;

    // User who made the change
    private String userId;
    private String userName;
    private String userRole; // "TRAINER" | "STUDENT"

    // Cursor position for live cursor sync
    private Double cursorX;
    private Double cursorY;

    private String timestamp;

    public WhiteboardEvent() {}

    // ── Getters & Setters ──────────────────────────────────────────────────────

    public Long getSessionId() { return sessionId; }
    public void setSessionId(Long sessionId) { this.sessionId = sessionId; }

    public String getEventType() { return eventType; }
    public void setEventType(String eventType) { this.eventType = eventType; }

    public String getElements() { return elements; }
    public void setElements(String elements) { this.elements = elements; }

    public String getAppState() { return appState; }
    public void setAppState(String appState) { this.appState = appState; }

    public String getFiles() { return files; }
    public void setFiles(String files) { this.files = files; }

    public String getUserId() { return userId; }
    public void setUserId(String userId) { this.userId = userId; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public String getUserRole() { return userRole; }
    public void setUserRole(String userRole) { this.userRole = userRole; }

    public Double getCursorX() { return cursorX; }
    public void setCursorX(Double cursorX) { this.cursorX = cursorX; }

    public Double getCursorY() { return cursorY; }
    public void setCursorY(Double cursorY) { this.cursorY = cursorY; }

    public String getTimestamp() { return timestamp; }
    public void setTimestamp(String timestamp) { this.timestamp = timestamp; }
}