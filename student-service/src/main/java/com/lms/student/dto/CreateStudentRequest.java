
package com.lms.student.dto;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public class CreateStudentRequest {

    @NotNull
    private Long userId;

    @NotBlank
    @Email
    private String email; // ✅ NEW

    // ===== getters & setters =====

    public Long getUserId() {
        return userId;
    }

    public void setUserId(Long userId) {
        this.userId = userId;
    }

    public String getEmail() {            // ✅ NEW
        return email;
    }

    public void setEmail(String email) {  // ✅ NEW
        this.email = email;
    }
}
