//
//package com.lms.user.model;
//
//import jakarta.persistence.*;
//import java.time.Instant;
//
//@Entity
//@Table(name = "users", uniqueConstraints = {
//        @UniqueConstraint(columnNames = {"email"})
//})
//public class User {
//
//    @Id
//    @GeneratedValue(strategy = GenerationType.IDENTITY)
//    private Long id;
//
//    @Column(name = "tenant_id")
//    private String tenantId;
//
//    @Column(nullable = false)
//    private String email;
//
//    @Column(name = "password_hash")
//    private String passwordHash;
//
//    @Column(name = "display_name")
//    private String displayName;
//
//    private String roles; // e.g. ROLE_STUDENT, ROLE_TRAINER
//
//    @Column(name = "photo_url", columnDefinition = "TEXT")
//    private String photoUrl;
//
//    /**
//     * ← NEW: links this user to an Organization (UUID stored as String).
//     * Populated by:
//     *   - AuthEventConsumer when USER_CREATED event includes organizationId
//     *   - CreateUserRequest when an org admin manually creates a user
//     */
//    @Column(name = "organization_id")
//    private String organizationId;
//
//    @Column(name = "created_at")
//    private Instant createdAt = Instant.now();
//
//    public User() {}
//
//    // ── Getters & Setters ──────────────────────────────────────────────────
//    public Long getId()                    { return id; }
//    public void setId(Long id)             { this.id = id; }
//
//    public String getTenantId()            { return tenantId; }
//    public void setTenantId(String tid)    { this.tenantId = tid; }
//
//    public String getEmail()               { return email; }
//    public void setEmail(String email)     { this.email = email; }
//
//    public String getPasswordHash()        { return passwordHash; }
//    public void setPasswordHash(String ph) { this.passwordHash = ph; }
//
//    public String getDisplayName()         { return displayName; }
//    public void setDisplayName(String dn)  { this.displayName = dn; }
//
//    public String getRoles()               { return roles; }
//    public void setRoles(String roles)     { this.roles = roles; }
//
//    public String getPhotoUrl()            { return photoUrl; }
//    public void setPhotoUrl(String url)    { this.photoUrl = url; }
//
//    public String getOrganizationId()              { return organizationId; }       // ← NEW
//    public void setOrganizationId(String orgId)    { this.organizationId = orgId; } // ← NEW
//
//    public Instant getCreatedAt()              { return createdAt; }
//    public void setCreatedAt(Instant createdAt){ this.createdAt = createdAt; }
//}
package com.lms.user.model;

import jakarta.persistence.*;
import java.time.Instant;

// WHY: Central user identity for all roles (Student, Trainer, Admin) in the LMS
@Entity
@Table(name = "users",
    indexes = {
        // WHY: Login and lookup by email is the most frequent operation across all services
        @Index(name = "idx_users_email", columnList = "email", unique = true),
        // WHY: Batch/course/enrollment services look up users by org to scope data
        @Index(name = "idx_users_organization_id", columnList = "organization_id"),
        // WHY: Role-based filtering used by admin dashboards and internal APIs
        @Index(name = "idx_users_roles", columnList = "roles"),
        // WHY: Composite for org+role queries — "get all students in org X"
        @Index(name = "idx_users_org_roles", columnList = "organization_id, roles")
    })
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    // WHY: Multi-tenant LMS — tenantId scopes data to a specific LMS instance
    @Column(name = "tenant_id")
    private String tenantId;

    // WHY: Primary identity and login credential across all LMS services
    @Column(nullable = false)
    private String email;

    // WHY: Stored by auth-service; user-service mirrors user without password
    @Column(name = "password_hash")
    private String passwordHash;

    // WHY: Shown in batch lists, course pages, and live session participant lists
    @Column(name = "display_name")
    private String displayName;

    // WHY: Single string role (ROLE_STUDENT/ROLE_TRAINER/ROLE_ADMIN) drives all authorization
    private String roles;

    // WHY: Profile photo shown in dashboards, chat, and live sessions
    @Column(name = "photo_url", columnDefinition = "TEXT")
    private String photoUrl;

    // WHY: Links user to an Organization for multi-tenant org-scoped queries
    @Column(name = "organization_id")
    private String organizationId;

    // WHY: Audit trail for user creation — used in analytics and admin reports
    @Column(name = "created_at")
    private Instant createdAt = Instant.now();

    public User() {}

    public Long getId()                    { return id; }
    public void setId(Long id)             { this.id = id; }
    public String getTenantId()            { return tenantId; }
    public void setTenantId(String tid)    { this.tenantId = tid; }
    public String getEmail()               { return email; }
    public void setEmail(String email)     { this.email = email; }
    public String getPasswordHash()        { return passwordHash; }
    public void setPasswordHash(String ph) { this.passwordHash = ph; }
    public String getDisplayName()         { return displayName; }
    public void setDisplayName(String dn)  { this.displayName = dn; }
    public String getRoles()               { return roles; }
    public void setRoles(String roles)     { this.roles = roles; }
    public String getPhotoUrl()            { return photoUrl; }
    public void setPhotoUrl(String url)    { this.photoUrl = url; }
    public String getOrganizationId()      { return organizationId; }
    public void setOrganizationId(String o){ this.organizationId = o; }
    public Instant getCreatedAt()          { return createdAt; }
    public void setCreatedAt(Instant c)    { this.createdAt = c; }
}