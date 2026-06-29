package com.lms.video.dto;

import java.io.Serializable;
import java.util.Map;

// Implements Serializable — required for Redis caching (feature-flags:video:org and feature-flags:video:user caches).
// Shape: { "enabled": true, "features": { "upload_video": true, "delete_video": false, ... } }
// This flat shape is what the frontend reads and writes directly — no per-service nesting.
public class VideoFeatureFlagsDTO implements Serializable {

    private static final long serialVersionUID = 1L;

    private boolean enabled;
    private Map<String, Boolean> features;

    public VideoFeatureFlagsDTO() {}

    public boolean isEnabled()                        { return enabled; }
    public void setEnabled(boolean enabled)           { this.enabled = enabled; }

    public Map<String, Boolean> getFeatures()         { return features; }
    public void setFeatures(Map<String, Boolean> f)   { this.features = f; }
}