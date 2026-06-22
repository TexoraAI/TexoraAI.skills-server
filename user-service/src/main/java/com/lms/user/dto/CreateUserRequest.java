//package com.lms.user.dto;
//
//import jakarta.validation.constraints.Email;
//import jakarta.validation.constraints.NotBlank;
//import jakarta.validation.constraints.Size;
//
//public class CreateUserRequest {
//
//    @Email
//    @NotBlank
//    private String email;
////hshgdh
//    @Size(min = 6, message = "password must be at least 6 characters")
//    private String password;
////dcddf
//    private String displayName;
//    private String tenantId;
//    private String roles;
////fvfvf
//    public CreateUserRequest() {}
//
//    public String getEmail() { return email; }
//    public void setEmail(String email) { this.email = email; }
//
//    public String getPassword() { return password; }
//    public void setPassword(String password) { this.password = password; }
//
//    public String getDisplayName() { return displayName; }
//    public void setDisplayName(String displayName) { this.displayName = displayName; }
//
//    public String getTenantId() { return tenantId; }
//    public void setTenantId(String tenantId) { this.tenantId = tenantId; }
//
//    public String getRoles() { return roles; }
//    public void setRoles(String roles) { this.roles = roles; }
//}
package com.lms.user.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

public class CreateUserRequest {

    @NotBlank
    @Email
    private String email;

    private String displayName;
    private String tenantId;
    private String roles;           // e.g. "ROLE_STUDENT"
    private String organizationId;  // ← NEW: UUID string of the owning org

    // ── Getters & Setters ──────────────────────────────────────────────────
    public String getEmail()              { return email; }
    public void setEmail(String email)    { this.email = email; }

    public String getDisplayName()        { return displayName; }
    public void setDisplayName(String dn) { this.displayName = dn; }

    public String getTenantId()           { return tenantId; }
    public void setTenantId(String tid)   { this.tenantId = tid; }

    public String getRoles()              { return roles; }
    public void setRoles(String roles)    { this.roles = roles; }

    public String getOrganizationId()             { return organizationId; }       // ← NEW
    public void setOrganizationId(String orgId)   { this.organizationId = orgId; } // ← NEW
}