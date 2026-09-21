package com.lms.chat.entity;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "notebook_collaborators")
public class NotebookCollaborator {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "notebook_id", nullable = false)
    private Notebook notebook;

    @Column(name = "email", nullable = false)
    private String email;

    @Column(name = "added_at", nullable = false, updatable = false)
    private LocalDateTime addedAt;

    @PrePersist
    protected void onCreate() {
        this.addedAt = LocalDateTime.now();
    }

    // ===== Getters =====
    public Long getId() { return id; }
    public Notebook getNotebook() { return notebook; }
    public String getEmail() { return email; }
    public LocalDateTime getAddedAt() { return addedAt; }

    // ===== Setters =====
    public void setId(Long id) { this.id = id; }
    public void setNotebook(Notebook notebook) { this.notebook = notebook; }
    public void setEmail(String email) { this.email = email; }
    public void setAddedAt(LocalDateTime addedAt) { this.addedAt = addedAt; }
}