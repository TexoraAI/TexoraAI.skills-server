package com.lms.attendance.entity;

import jakarta.persistence.*;

// Stores feature flag JSON per org (scopeKey = organizationId)
// or per individual user (scopeKey = email, for org-less trainers/students).
//
// No @Index on scope_key — it is the @Id (primary key); DB auto-indexes the PK.
@Entity
@Table(name = "attendance_feature_flags")
public class AttendanceFeatureFlags {

    @Id
    @Column(name = "scope_key", nullable = false, length = 191)
    private String scopeKey; // organizationId OR email

    @Column(columnDefinition = "TEXT")
    private String flagsJson; // serialized AttendanceFeatureFlagsDTO JSON

    public AttendanceFeatureFlags() {}

    public String getScopeKey()                 { return scopeKey; }
    public void setScopeKey(String scopeKey)     { this.scopeKey = scopeKey; }

    public String getFlagsJson()                { return flagsJson; }
    public void setFlagsJson(String flagsJson)   { this.flagsJson = flagsJson; }
}