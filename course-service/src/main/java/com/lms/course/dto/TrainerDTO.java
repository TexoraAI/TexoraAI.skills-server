package com.lms.course.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

@JsonIgnoreProperties(ignoreUnknown = true)
public class TrainerDTO {
    private Long id;
    private String email;
    private String displayName;   // ← MUST match UserResponse field name
    private String organizationId;

    public Long getId()                            { return id; }
    public void setId(Long id)                     { this.id = id; }
    public String getEmail()                       { return email; }
    public void setEmail(String email)             { this.email = email; }
    public String getDisplayName()                 { return displayName; }
    public void setDisplayName(String name)        { this.displayName = name; }
    public String getOrganizationId()              { return organizationId; }
    public void setOrganizationId(String orgId)    { this.organizationId = orgId; }
}