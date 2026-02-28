package com.lms.auth.controller;

import com.lms.auth.dto.BusinessApplyRequest;
import com.lms.auth.service.BusinessService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/business")
public class BusinessController {

    private final BusinessService businessService;

    public BusinessController(BusinessService businessService) {
        this.businessService = businessService;
    }

    @PostMapping("/apply")
    public ResponseEntity<?> applyBusiness(@RequestBody BusinessApplyRequest request) {
        businessService.applyBusiness(request);
        return ResponseEntity.ok("Business application submitted. Waiting for approval.");
    }
}
