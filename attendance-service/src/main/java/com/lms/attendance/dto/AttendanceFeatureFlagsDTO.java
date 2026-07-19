package com.lms.attendance.dto;

import java.io.Serializable;
import java.util.Map;

// Implements Serializable — required for Redis caching
// (feature-flags:attendance:org and feature-flags:attendance:user caches).
// Shape: { "enabled": true, "features": { "mark_attendance": true, ... } }
public class AttendanceFeatureFlagsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean enabled;
    private Map<String, Boolean> features;

    public AttendanceFeatureFlagsDTO() {}

    public boolean isEnabled()                      { return enabled; }
    public void setEnabled(boolean enabled)          { this.enabled = enabled; }
    public Map<String, Boolean> getFeatures()        { return features; }
    public void setFeatures(Map<String, Boolean> f)  { this.features = f; }
}