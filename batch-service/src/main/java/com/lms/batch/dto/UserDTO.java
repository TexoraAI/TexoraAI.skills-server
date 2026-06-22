//
//
//package com.lms.batch.dto;
//
//public class UserDTO {
//
//    private Long id;
//    private String email;
//    private String displayName;   // 🔥 ADD THIS
//    private String role;
//
//    public Long getId() {
//        return id;
//    }
//
//    public void setId(Long id) {
//        this.id = id;
//    }
//
//    public String getEmail() {
//        return email;
//    }
//
//    public void setEmail(String email) {
//        this.email = email;
//    }
//
//    public String getDisplayName() {     // 🔥 ADD
//        return displayName;
//    }
//
//    public void setDisplayName(String displayName) {   // 🔥 ADD
//        this.displayName = displayName;
//    }
//
//    public String getRole() {
//        return role;
//    }
//
//    public void setRole(String role) {
//        this.role = role;
//    }
//}
package com.lms.batch.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

// OPTIMIZATION: Defensive — same reasoning as TrainerDTO/StudentDTO.
@JsonIgnoreProperties(ignoreUnknown = true)
public class UserDTO {
    private Long id;
    private String email;
    private String displayName;
    private String role;

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }
    public String getDisplayName() { return displayName; }
    public void setDisplayName(String displayName) { this.displayName = displayName; }
    public String getRole() { return role; }
    public void setRole(String role) { this.role = role; }
}