//package com.lms.batch.dto;
//
//public class StudentDTO {
//
//    private String email;
//    private String displayName;
//
//    public StudentDTO() {}
//
//    public StudentDTO(String email, String displayName) {
//        this.email = email;
//        this.displayName = displayName;
//    }
//
//    public String getEmail() { return email; }
//    public void setEmail(String email) { this.email = email; }
//
//    public String getDisplayName() { return displayName; }
//    public void setDisplayName(String displayName) { this.displayName = displayName; }
//}
package com.lms.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// OPTIMIZATION: Defensive — same fix as TrainerDTO. Prevents future breakage
// if user-service adds fields to the student response that this DTO doesn't track.
@JsonIgnoreProperties(ignoreUnknown = true)
public class StudentDTO {

    private String email;
    private String displayName;

    public StudentDTO() {}

    public StudentDTO(String email, String displayName) {
        this.email = email;
        this.displayName = displayName;
    }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
}