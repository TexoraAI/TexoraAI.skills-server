package com.lms.assessment.model;

import jakarta.persistence.*;

// Stores feature flag JSON per org (scopeKey = organizationId UUID)
// or per individual user (scopeKey = email, for org-less trainers/students).
//
// No @Index on scope_key because it is the @Id (primary key) — the DB
// auto-creates a unique index on the PK column.
@Entity
@Table(name = "assessment_feature_flags")
public class AssessmentFeatureFlags {

    @Id
    @Column(name = "scope_key", nullable = false, length = 191)
    private String scopeKey; // organizationId OR email

    @Column(columnDefinition = "TEXT")
    private String flagsJson; // serialized AssessmentFeatureFlagsDTO JSON

    public AssessmentFeatureFlags() {}

    public String getScopeKey()                  { return scopeKey; }
    public void setScopeKey(String scopeKey)     { this.scopeKey = scopeKey; }

    public String getFlagsJson()                 { return flagsJson; }
    public void setFlagsJson(String flagsJson)   { this.flagsJson = flagsJson; }
}