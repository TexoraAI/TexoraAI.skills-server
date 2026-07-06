package com.lms.course.controller;

import com.lms.course.dto.CompanyRequest;
import com.lms.course.dto.CompanyResponse;
import com.lms.course.dto.CompanyStatsResponse;
import com.lms.course.dto.PageResponse;
import com.lms.course.service.CompanyService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/companies")
public class CompanyController {

    private final CompanyService companyService;

    public CompanyController(CompanyService companyService) {
        this.companyService = companyService;
    }

    @PostMapping
    public ResponseEntity<CompanyResponse> create(@Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.status(HttpStatus.CREATED).body(companyService.create(request));
    }

    @PutMapping("/{id}")
    public ResponseEntity<CompanyResponse> update(@PathVariable Long id, @Valid @RequestBody CompanyRequest request) {
        return ResponseEntity.ok(companyService.update(id, request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> delete(@PathVariable Long id) {
        companyService.delete(id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyResponse> getById(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.getById(id));
    }

    @GetMapping
    public ResponseEntity<PageResponse<CompanyResponse>> getAll(
            @RequestParam(required = false) String search,
            @RequestParam(required = false) String category,
            @RequestParam(defaultValue = "all") String status,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        return ResponseEntity.ok(companyService.getAll(search, category, status, page, size));
    }

    @PatchMapping("/{id}/toggle-status")
    public ResponseEntity<CompanyResponse> toggleStatus(@PathVariable Long id) {
        return ResponseEntity.ok(companyService.toggleStatus(id));
    }

    @GetMapping("/stats")
    public ResponseEntity<CompanyStatsResponse> getStats() {
        return ResponseEntity.ok(companyService.getStats());
    }

    @GetMapping("/public/active")
    public ResponseEntity<Map<String, List<CompanyResponse>>> getActiveForLandingPage() {
        return ResponseEntity.ok(companyService.getActiveForLandingPage());
    }
}