package com.lms.notification.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(
    name = "newsletter_subscribers",
    uniqueConstraints = @UniqueConstraint(columnNames = "email")
)
public class NewsletterSubscriber {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, unique = true)
    private String email;

    @Column(name = "subscribed_at")
    private LocalDateTime subscribedAt;

    @Column(name = "is_active")
    private boolean active = true;

    @PrePersist
    public void prePersist() {
        this.subscribedAt = LocalDateTime.now();
    }

    // ── Constructors ──────────────────────────────────────────
    public NewsletterSubscriber() {}

    public NewsletterSubscriber(String email, boolean active) {
        this.email  = email;
        this.active = active;
    }

    // ── Getters & Setters ─────────────────────────────────────
    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public LocalDateTime getSubscribedAt() { return subscribedAt; }
    public void setSubscribedAt(LocalDateTime subscribedAt) { this.subscribedAt = subscribedAt; }

    public boolean isActive() { return active; }
    public void setActive(boolean active) { this.active = active; }

    @Override
    public String toString() {
        return "NewsletterSubscriber{id=" + id + ", email='" + email + "', active=" + active + "}";
    }
}