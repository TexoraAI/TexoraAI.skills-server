package com.lms.course.model;

import jakarta.persistence.*;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(
    name = "program_wishlist",
    uniqueConstraints = @UniqueConstraint(columnNames = {"program_id", "user_email"})
)
public class ProgramWishlist {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "program_id", nullable = false)
    private FeaturedProgram program;

    @Column(name = "user_email", nullable = false)
    private String userEmail;

    private String userName;

    @CreationTimestamp
    @Column(updatable = false)
    private LocalDateTime createdAt;

    public ProgramWishlist() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public FeaturedProgram getProgram() { return program; }
    public void setProgram(FeaturedProgram program) { this.program = program; }

    public String getUserEmail() { return userEmail; }
    public void setUserEmail(String userEmail) { this.userEmail = userEmail; }

    public String getUserName() { return userName; }
    public void setUserName(String userName) { this.userName = userName; }

    public LocalDateTime getCreatedAt() { return createdAt; }
    public void setCreatedAt(LocalDateTime createdAt) { this.createdAt = createdAt; }
}