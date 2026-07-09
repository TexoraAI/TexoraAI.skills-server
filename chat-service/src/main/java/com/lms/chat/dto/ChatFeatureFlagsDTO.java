package com.lms.chat.dto;

import java.io.Serializable;
import java.util.Map;

// Serializable kept for parity with VideoFeatureFlagsDTO in case Redis caching
// is added to this service later. Flat shape read/written directly by frontend:
// { "enabled": true, "features": { "send_message": true, "notebook_ai_chat": false, ... } }
public class ChatFeatureFlagsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean enabled;
    private Map<String, Boolean> features;

    public ChatFeatureFlagsDTO() {}

    public boolean isEnabled()                      { return enabled; }
    public void setEnabled(boolean enabled)         { this.enabled = enabled; }

    public Map<String, Boolean> getFeatures()       { return features; }
    public void setFeatures(Map<String, Boolean> f) { this.features = f; }
}