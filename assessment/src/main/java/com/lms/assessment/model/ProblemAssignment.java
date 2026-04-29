package com.lms.assessment.model;

import jakarta.persistence.*;
import java.time.LocalDateTime;

@Entity
@Table(name = "problem_assignments", indexes = {
    @Index(name = "idx_pa_batch_id",    columnList = "batchId"),
    @Index(name = "idx_pa_problem_id",  columnList = "problem_id")
},
uniqueConstraints = {
    @UniqueConstraint(name = "uq_problem_batch",
        columnNames = {"problem_id", "batchId"})
})
public class ProblemAssignment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "problem_id", nullable = false)
    private CodingProblem problem;

    @Column(nullable = false)
    private String batchId;

    @Column(nullable = false)
    private String assignedByEmail;

    @Column(nullable = false)
    private LocalDateTime assignedAt;

    private LocalDateTime dueDate;

    @Column(nullable = false)
    private Boolean isActive;

    @PrePersist
    protected void onCreate() {
        this.assignedAt = LocalDateTime.now();
        if (this.isActive == null) this.isActive = true;
    }

    // ── Constructors ──────────────────────────────
    public ProblemAssignment() {}

    // ── Getters ───────────────────────────────────
    public Long getId()                   { return id; }
    public CodingProblem getProblem()     { return problem; }
    public String getBatchId()            { return batchId; }
    public String getAssignedByEmail()    { return assignedByEmail; }
    public LocalDateTime getAssignedAt()  { return assignedAt; }
    public LocalDateTime getDueDate()     { return dueDate; }
    public Boolean getIsActive()          { return isActive; }

    // ── Setters ───────────────────────────────────
    public void setId(Long id)                          { this.id = id; }
    public void setProblem(CodingProblem problem)       { this.problem = problem; }
    public void setBatchId(String batchId)              { this.batchId = batchId; }
    public void setAssignedByEmail(String email)        { this.assignedByEmail = email; }
    public void setAssignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; }
    public void setDueDate(LocalDateTime dueDate)       { this.dueDate = dueDate; }
    public void setIsActive(Boolean isActive)           { this.isActive = isActive; }

    // ── Builder ───────────────────────────────────
    public static Builder builder() { return new Builder(); }

    public static class Builder {
        private Long id;
        private CodingProblem problem;
        private String batchId;
        private String assignedByEmail;
        private LocalDateTime assignedAt;
        private LocalDateTime dueDate;
        private Boolean isActive;

        public Builder id(Long id)                          { this.id = id; return this; }
        public Builder problem(CodingProblem problem)       { this.problem = problem; return this; }
        public Builder batchId(String batchId)              { this.batchId = batchId; return this; }
        public Builder assignedByEmail(String email)        { this.assignedByEmail = email; return this; }
        public Builder assignedAt(LocalDateTime assignedAt) { this.assignedAt = assignedAt; return this; }
        public Builder dueDate(LocalDateTime dueDate)       { this.dueDate = dueDate; return this; }
        public Builder isActive(Boolean isActive)           { this.isActive = isActive; return this; }

        public ProblemAssignment build() {
            ProblemAssignment pa = new ProblemAssignment();
            pa.id = id; pa.problem = problem; pa.batchId = batchId;
            pa.assignedByEmail = assignedByEmail; pa.assignedAt = assignedAt;
            pa.dueDate = dueDate; pa.isActive = isActive;
            return pa;
        }
    }
}