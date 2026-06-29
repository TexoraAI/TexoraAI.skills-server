
package com.lms.batch.client;

import com.lms.batch.config.FeignAuthConfig;
import com.lms.batch.dto.PageResponse;
import com.lms.batch.dto.StudentDTO;
import com.lms.batch.dto.TrainerDTO;
import com.lms.batch.dto.UserDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        url = "${USER_SERVICE_URL:http://localhost:8082}",
        configuration = FeignAuthConfig.class
)
public interface UserClient {

    @GetMapping("/api/users/internal/students")
    PageResponse<StudentDTO> getAllStudents();       // was List<StudentDTO>

    @GetMapping("/api/users/email/{email}")
    UserDTO getUserByEmail(@PathVariable String email);

    @GetMapping("/api/users/internal/trainers")
    PageResponse<TrainerDTO> getAllTrainers();       // was List<TrainerDTO>
    
    @GetMapping("/api/users/by-org/{orgId}/role/{role}")
    PageResponse<TrainerDTO> getTrainersByOrg(
        @PathVariable String orgId,
        @PathVariable String role
    );
    @GetMapping("/api/users/by-org/{orgId}/role/{role}")
    PageResponse<StudentDTO> getStudentsByOrg(
        @PathVariable String orgId,
        @PathVariable String role
    );
    //super admin gets the trainers who does not have ord id 
    @GetMapping("/api/users/no-org/role/{role}")
    PageResponse<TrainerDTO> getTrainersWithoutOrg(@PathVariable String role);
    //super admin gets the students  who does not have ord id 
    @GetMapping("/api/users/no-org/role/{role}")
    PageResponse<StudentDTO> getStudentsWithoutOrg(@PathVariable String role);
}