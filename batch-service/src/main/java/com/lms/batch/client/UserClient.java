package com.lms.batch.client;

import com.lms.batch.config.FeignAuthConfig;
import com.lms.batch.dto.StudentDTO;
import com.lms.batch.dto.TrainerDTO;
import com.lms.batch.dto.UserDTO;

import java.util.List;

import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        url = "http://localhost:8082",
        configuration = FeignAuthConfig.class
)
public interface UserClient {

    // EXISTING (DO NOT REMOVE)
	 @GetMapping("/api/users/internal/students")
	    List<StudentDTO> getAllStudents();    

    // 🆕 NEW — EMAIL BASED
    @GetMapping("/api/users/email/{email}")
    UserDTO getUserByEmail(@PathVariable String email);
    
    @GetMapping("/api/users/internal/trainers")
    List<TrainerDTO> getAllTrainers();
}
