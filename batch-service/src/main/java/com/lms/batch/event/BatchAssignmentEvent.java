
package com.lms.batch.event;

public class BatchAssignmentEvent {

    private String type;
    private String email;
    private Long   batchId;
    private String role;          // STUDENT or TRAINER
    private String organizationId; // NEW — UUID from auth-service, nullable for standalone trainers

    public BatchAssignmentEvent() {}

    public BatchAssignmentEvent(String type, String email, Long batchId, String role) {
        this.type    = type;
        this.email   = email;
        this.batchId = batchId;
        this.role    = role;
    }

    // NEW — convenience constructor with organizationId
    public BatchAssignmentEvent(String type, String email, Long batchId,
                                String role, String organizationId) {
        this(type, email, batchId, role);
        this.organizationId = organizationId;
    }

    public String getType()           { return type; }
    public void   setType(String t)   { this.type = t; }

    public String getEmail()          { return email; }
    public void   setEmail(String e)  { this.email = e; }

    public Long   getBatchId()        { return batchId; }
    public void   setBatchId(Long b)  { this.batchId = b; }

    public String getRole()           { return role; }
    public void   setRole(String r)   { this.role = r; }

    // NEW
    public String getOrganizationId()              { return organizationId; }
    public void   setOrganizationId(String orgId)  { this.organizationId = orgId; }
}