//
//package com.lms.user.dto;
//
//import java.time.Instant;
//
//public class UserResponse {
//
//    private Long    id;
//    private String  email;
//    private String  displayName;
//    private String  roles;
//    private String  tenantId;
//    private Instant createdAt;
//    private String  photoUrl;
//    private String  organizationId; // ← NEW
//
//    // ── Getters & Setters ──────────────────────────────────────────────────
//    public Long getId()                   { return id; }
//    public void setId(Long id)            { this.id = id; }
//
//    public String getEmail()              { return email; }
//    public void setEmail(String email)    { this.email = email; }
//
//    public String getDisplayName()        { return displayName; }
//    public void setDisplayName(String dn) { this.displayName = dn; }
//
//    public String getRoles()              { return roles; }
//    public void setRoles(String roles)    { this.roles = roles; }
//
//    public String getTenantId()           { return tenantId; }
//    public void setTenantId(String tid)   { this.tenantId = tid; }
//
//    public Instant getCreatedAt()              { return createdAt; }
//    public void setCreatedAt(Instant createdAt){ this.createdAt = createdAt; }
//
//    public String getPhotoUrl()           { return photoUrl; }
//    public void setPhotoUrl(String url)   { this.photoUrl = url; }
//
//    public String getOrganizationId()             { return organizationId; }       // ← NEW
//    public void setOrganizationId(String orgId)   { this.organizationId = orgId; } // ← NEW
//}

package com.lms.user.dto;

import java.io.Serializable;
import java.time.Instant;

public class UserResponse implements Serializable {

    private static final long serialVersionUID = 1L;

    private Long    id;
    private String  email;
    private String  displayName;
    private String  roles;
    private String  tenantId;
    private Instant createdAt;
    private String  photoUrl;
    private String  organizationId;

    public UserResponse() {}

    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }
    public String getEmail()               { return email; }
    public void setEmail(String email)     { this.email = email; }
    public String getDisplayName()         { return displayName; }
    public void setDisplayName(String dn)  { this.displayName = dn; }
    public String getRoles()               { return roles; }
    public void setRoles(String roles)     { this.roles = roles; }
    public String getTenantId()            { return tenantId; }
    public void setTenantId(String tid)    { this.tenantId = tid; }
    public Instant getCreatedAt()          { return createdAt; }
    public void setCreatedAt(Instant c)    { this.createdAt = c; }
    public String getPhotoUrl()            { return photoUrl; }
    public void setPhotoUrl(String url)    { this.photoUrl = url; }
    public String getOrganizationId()      { return organizationId; }
    public void setOrganizationId(String o){ this.organizationId = o; }
}