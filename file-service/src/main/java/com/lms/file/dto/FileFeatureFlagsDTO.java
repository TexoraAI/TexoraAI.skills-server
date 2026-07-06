package com.lms.file.dto;

import java.io.Serializable;
import java.util.Map;

// Implements Serializable — required for Redis caching (feature-flags:file:org and feature-flags:file:user caches).
// Shape: { "enabled": true, "features": { "upload_file": true, "delete_file": false, ... } }
// This flat shape is what the frontend reads and writes directly — no per-service nesting.
public class FileFeatureFlagsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean enabled;
    private Map<String, Boolean> features;

    public FileFeatureFlagsDTO() {}

    public boolean isEnabled()                        { return enabled; }
    public void setEnabled(boolean enabled)           { this.enabled = enabled; }

    public Map<String, Boolean> getFeatures()         { return features; }
    public void setFeatures(Map<String, Boolean> f)   { this.features = f; }
}