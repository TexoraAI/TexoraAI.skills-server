//package com.lms.batch.dto;
//
//public class TrainerDTO {
//
//    private Long id;
//    private String email;
//    private String displayName;
//
//    public TrainerDTO() {}
//
//    public Long getId() { return id; }
//    public void setId(Long id) { this.id = id; }
//
//    public String getEmail() { return email; }
//    public void setEmail(String email) { this.email = email; }
//
//    public String getDisplayName() { return displayName; }
//    public void setDisplayName(String displayName) { this.displayName = displayName; }
//}
package com.lms.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// OPTIMIZATION: Added @JsonIgnoreProperties(ignoreUnknown = true).
// user-service now returns a "roles" field that TrainerDTO doesn't declare.
// Without this, Feign's Jackson decoder throws UnrecognizedPropertyException
// and the whole call fails — even though batch-service never needed "roles" here.
@JsonIgnoreProperties(ignoreUnknown = true)
public class TrainerDTO {

    private Long id;
    private String email;
    private String displayName;

    public TrainerDTO() {}

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}