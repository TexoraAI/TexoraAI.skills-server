package com.lms.chat.entity;

import jakarta.persistence.*;

// Stores feature flag JSON per org (scopeKey = organizationId)
// or per individual user (scopeKey = email, for org-less trainers/students).
// scope_key is the @Id, so it already gets a unique index from the PK.
@Entity
@Table(name = "chat_feature_flags")
public class ChatFeatureFlags {

    @Id
    @Column(name = "scope_key", nullable = false, length = 191)
    private String scopeKey; // organizationId OR email

    @Column(columnDefinition = "TEXT")
    private String flagsJson; // serialized ChatFeatureFlagsDTO JSON

    public ChatFeatureFlags() {}

    public String getScopeKey()                 { return scopeKey; }
    public void setScopeKey(String scopeKey)    { this.scopeKey = scopeKey; }

    public String getFlagsJson()                { return flagsJson; }
    public void setFlagsJson(String flagsJson)  { this.flagsJson = flagsJson; }
}