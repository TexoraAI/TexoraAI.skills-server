
package com.lms.auth.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(
    name = "users",
    indexes = {
        @Index(name = "idx_users_email",
               columnList = "email", unique = true),
        @Index(name = "idx_users_org_id",
               columnList = "organization_id"),
        @Index(name = "idx_users_org_role",
               columnList = "organization_id, role"),
        @Index(name = "idx_users_role",
               columnList = "role"),
        @Index(name = "idx_users_role_approved",
               columnList = "role, approved"),
        @Index(name = "idx_users_role_approved_verified",
               columnList = "role, approved, email_verified"),
        @Index(name = "idx_users_role_approved_org",
               columnList = "role, approved, organization_id"),
        @Index(name = "idx_users_created_by",
               columnList = "created_by"),
        @Index(name = "idx_users_created_at",
               columnList = "created_at")
    }
)
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Role role;

    @Column(nullable = false)
    private boolean approved = false;

    @Column(nullable = false)
    private boolean emailVerified = false;

    @Column(name = "organization_id")
    private UUID organizationId;

    @Column(name = "is_blocked", nullable = false)
    private boolean blocked = false;

    @Column(name = "onboarding_answers", columnDefinition = "TEXT")
    private String onboardingAnswers;

    @Column(name = "onboarding_status", length = 20)
    private String onboardingStatus = "PENDING";
    
    // NEW — tracks whether the role-specific Details/Org form has been filled.
    // Intentionally separate from onboardingStatus, which only tracks the
    // signup role-quiz (CompleteProfile.jsx) and was incorrectly being reused
    // as "profileCompleted" everywhere else.
    @Column(name = "profile_completed", nullable = false)
    private boolean profileCompleted = false;

    @Column(name = "is_google_user", nullable = false)
    private Boolean googleUser = false;

    @Column(name = "created_by")
    private Long createdBy;

    @Column(name = "created_at")
    private LocalDateTime createdAt;
    @Column
    private Boolean passwordSet = true;
    public Boolean getPasswordSet() { return passwordSet; }
    public void setPasswordSet(Boolean passwordSet) { this.passwordSet = passwordSet; }
    
    public User() {}

    public User(String name, String email, String password, Role role) {
        this.name = name;
        this.email = email;
        this.password = password;
        this.role = role;
        this.approved = false;
        this.emailVerified = false;
        this.createdAt = LocalDateTime.now();
        this.googleUser = false;
    }

    @PrePersist
    public void prePersist() {
        if (this.createdAt == null) this.createdAt = LocalDateTime.now();
        if (this.onboardingStatus == null) this.onboardingStatus = "PENDING";
        if (this.googleUser == null) this.googleUser = false;
    }

    // All existing getters/setters unchanged
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }
    public Role getRole() { return role; }
    public void setRole(Role role) { this.role = role; }
    public boolean isApproved() { return approved; }
    public void setApproved(boolean approved) { this.approved = approved; }
    public boolean isEmailVerified() { return emailVerified; }
    public void setEmailVerified(boolean emailVerified) { this.emailVerified = emailVerified; }
    public UUID getOrganizationId() { return organizationId; }
    public void setOrganizationId(UUID organizationId) { this.organizationId = organizationId; }
    public String getOnboardingAnswers() { return onboardingAnswers; }
    public void setOnboardingAnswers(String onboardingAnswers) { this.onboardingAnswers = onboardingAnswers; }
    public String getOnboardingStatus() { return onboardingStatus; }
    public void setOnboardingStatus(String onboardingStatus) { this.onboardingStatus = onboardingStatus; }
    public Boolean isGoogleUser() { return googleUser; }
    public void setGoogleUser(Boolean googleUser) { this.googleUser = (googleUser == null) ? false : googleUser; }
    public Long getCreatedBy() { return createdBy; }
    public void setCreatedBy(Long createdBy) { this.createdBy = createdBy; }
    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = LocalDateTime.now(); }
    public boolean isBlocked() { return blocked; }
    public void setBlocked(boolean blocked) { this.blocked = blocked; }
    public boolean isProfileCompleted() { return profileCompleted; }
    public void setProfileCompleted(boolean profileCompleted) { this.profileCompleted = profileCompleted; }
}