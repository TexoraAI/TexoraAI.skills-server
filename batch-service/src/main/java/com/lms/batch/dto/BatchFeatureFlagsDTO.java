//package com.lms.batch.dto;
//
//import java.util.Map;
//
//public class BatchFeatureFlagsDTO {
//
//    private boolean enabled;
//    private Map<String, Boolean> features; // featureKey -> on/off
//
//    public boolean isEnabled() { return enabled; }
//    public void setEnabled(boolean enabled) { this.enabled = enabled; }
//
//    public Map<String, Boolean> getFeatures() { return features; }
//    public void setFeatures(Map<String, Boolean> features) { this.features = features; }
//}
package com.lms.batch.dto;

import java.io.Serializable;
import java.util.Map;

// OPTIMIZATION: Implements Serializable required for Redis caching (feature-flags caches).
public class BatchFeatureFlagsDTO implements Serializable {
    private static final long serialVersionUID = 1L;

    private boolean enabled;
    private Map<String, Boolean> features;

    public BatchFeatureFlagsDTO() {}

    public boolean isEnabled()                         { return enabled; }
    public void setEnabled(boolean enabled)            { this.enabled = enabled; }
    public Map<String, Boolean> getFeatures()          { return features; }
    public void setFeatures(Map<String, Boolean> f)    { this.features = f; }
}