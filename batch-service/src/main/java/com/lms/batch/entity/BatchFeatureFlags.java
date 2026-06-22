package com.lms.batch.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "batch_feature_flags")
public class BatchFeatureFlags {

    @Id
    @Column(name = "scope_key", nullable = false, length = 191)
    private String scopeKey; // organizationId (UUID) OR user email (org-less trainer/student)

    @Column(columnDefinition = "TEXT")
    private String flagsJson;

    public String getScopeKey() { return scopeKey; }
    public void setScopeKey(String scopeKey) { this.scopeKey = scopeKey; }

    public String getFlagsJson() { return flagsJson; }
    public void setFlagsJson(String flagsJson) { this.flagsJson = flagsJson; }
}