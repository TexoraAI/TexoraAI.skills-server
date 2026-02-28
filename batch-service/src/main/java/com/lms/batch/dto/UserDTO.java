//package com.lms.batch.dto;
//
//public class UserDTO {
//
//    private Long id;
//    private String email;
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
//    public String getRole() {
//        return role;
//    }
//
//    public void setRole(String role) {
//        this.role = role;
//    }
//}


package com.lms.batch.dto;

public class UserDTO {

    private Long id;
    private String email;
    private String displayName;   // 🔥 ADD THIS
    private String role;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getDisplayName() {     // 🔥 ADD
        return displayName;
    }

    public void setDisplayName(String displayName) {   // 🔥 ADD
        this.displayName = displayName;
    }

    public String getRole() {
        return role;
    }

    public void setRole(String role) {
        this.role = role;
    }
}
