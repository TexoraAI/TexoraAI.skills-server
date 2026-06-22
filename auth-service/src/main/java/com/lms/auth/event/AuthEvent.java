


package com.lms.auth.event;

public class AuthEvent {

    private String eventType;
    private Long userId;
    private String email;
    private String role;
    private String displayName;
    private String organizationId;

    // NEW — batch limit fields (null for all non-org events)
    private Integer maxDepartments;
    private Integer maxBranchesPerDept;
    private Integer maxBatchesPerBranch;

    // ── EXISTING constructor — DO NOT TOUCH, used everywhere ──
    public AuthEvent(String eventType, Long userId, String email,
                     String role, String displayName, String organizationId) {
        this.eventType      = eventType;
        this.userId         = userId;
        this.email          = email;
        this.role           = role;
        this.displayName    = displayName;
        this.organizationId = organizationId;
    }

    // ── NEW constructor — used only for ORG_CREATED / ORG_UPDATED with limits ──
    public AuthEvent(String eventType, Long userId, String email,
                     String role, String displayName, String organizationId,
                     Integer maxDepartments, Integer maxBranchesPerDept,
                     Integer maxBatchesPerBranch) {
        this.eventType           = eventType;
        this.userId              = userId;
        this.email               = email;
        this.role                = role;
        this.displayName         = displayName;
        this.organizationId      = organizationId;
        this.maxDepartments      = maxDepartments;
        this.maxBranchesPerDept  = maxBranchesPerDept;
        this.maxBatchesPerBranch = maxBatchesPerBranch;
    }

    // ── Getters & Setters ──
    public String getEventType()      { return eventType; }
    public void setEventType(String v){ this.eventType = v; }

    public Long getUserId()           { return userId; }
    public void setUserId(Long v)     { this.userId = v; }

    public String getEmail()          { return email; }
    public void setEmail(String v)    { this.email = v; }

    public String getRole()           { return role; }
    public void setRole(String v)     { this.role = v; }

    public String getDisplayName()    { return displayName; }
    public void setDisplayName(String v){ this.displayName = v; }

    public String getOrganizationId() { return organizationId; }
    public void setOrganizationId(String v){ this.organizationId = v; }

    public Integer getMaxDepartments()       { return maxDepartments; }
    public void setMaxDepartments(Integer v) { this.maxDepartments = v; }

    public Integer getMaxBranchesPerDept()       { return maxBranchesPerDept; }
    public void setMaxBranchesPerDept(Integer v) { this.maxBranchesPerDept = v; }

    public Integer getMaxBatchesPerBranch()       { return maxBatchesPerBranch; }
    public void setMaxBatchesPerBranch(Integer v) { this.maxBatchesPerBranch = v; }
}