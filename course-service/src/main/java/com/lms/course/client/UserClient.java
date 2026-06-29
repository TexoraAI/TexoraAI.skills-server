package com.lms.course.client;

import com.lms.course.config.FeignAuthConfig;
import com.lms.course.dto.PageResponse;
import com.lms.course.dto.TrainerDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(
        name = "user-service",
        url = "${user-service.url}",
        configuration = FeignAuthConfig.class
)
public interface UserClient {

    @GetMapping("/api/users/by-org/{orgId}/role/{role}")
    PageResponse<TrainerDTO> getTrainersByOrg(
            @PathVariable String orgId,
            @PathVariable String role
    );
}