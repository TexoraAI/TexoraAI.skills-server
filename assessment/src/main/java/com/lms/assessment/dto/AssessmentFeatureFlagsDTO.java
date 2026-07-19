package com.lms.assessment.dto;

import java.io.Serializable;
import java.util.Map;

// Implements Serializable — required for Redis caching (feature-flags:assessment:org and
// feature-flags:assessment:user caches).
// Shape: { "enabled": true, "features": { "create_quiz": true, "attempt_quiz": false, ... } }
// This flat shape is what the frontend reads and writes directly — no per-service nesting.
public class AssessmentFeatureFlagsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean enabled;
    private Map<String, Boolean> features;

    public AssessmentFeatureFlagsDTO() {}

    public boolean isEnabled()                        { return enabled; }
    public void setEnabled(boolean enabled)           { this.enabled = enabled; }

    public Map<String, Boolean> getFeatures()         { return features; }
    public void setFeatures(Map<String, Boolean> f)   { this.features = f; }
}