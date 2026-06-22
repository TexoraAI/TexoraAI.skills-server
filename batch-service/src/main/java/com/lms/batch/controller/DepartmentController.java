//package com.lms.batch.controller;
//
//import com.lms.batch.entity.Department;
//import com.lms.batch.service.DepartmentService;
//import org.springframework.http.ResponseEntity;
//import org.springframework.security.core.Authentication;
//import org.springframework.security.core.context.SecurityContextHolder;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//
//@RestController
//@RequestMapping("/api/departments")
//public class DepartmentController {
//
//    private final DepartmentService departmentService;
//
//    // Inject your JwtUtil so we can extract organizationId from the token
//    private final com.lms.batch.security.JwtUtil jwtUtil;
//
//    public DepartmentController(DepartmentService departmentService,
//                                com.lms.batch.security.JwtUtil jwtUtil) {
//        this.departmentService = departmentService;
//        this.jwtUtil = jwtUtil;
//    }
//
//    @PostMapping
//    public ResponseEntity<Department> createDepartment(
//            @RequestBody Department department,
//            @RequestHeader("Authorization") String authHeader) {
//
//        // Extract organizationId from JWT on the server side
//        // This is always reliable — frontend never needs to send it
//        if (authHeader != null && authHeader.startsWith("Bearer ")) {
//            String token = authHeader.substring(7);
//            String orgId = jwtUtil.extractOrganizationId(token);
//            if (orgId != null) {
//                department.setOrganizationId(orgId);
//            }
//        }
//
//        return ResponseEntity.ok(departmentService.createDepartment(department));
//    }
//
////    @GetMapping
////    public ResponseEntity<List<Department>> getAllDepartments() {
////        return ResponseEntity.ok(departmentService.getAllDepartments());
////    }
//    @GetMapping
//    public ResponseEntity<List<Department>> getAllDepartments(
//            @RequestHeader("Authorization") String authHeader) {
//
//        String token = authHeader.substring(7);
//        String orgId = jwtUtil.extractOrganizationId(token);
//
//        return ResponseEntity.ok(departmentService.getDepartmentsByOrganization(orgId));
//    }
//
//    @GetMapping("/{id}")
//    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
//        return ResponseEntity.ok(departmentService.getDepartmentById(id));
//    }
//
//    @GetMapping("/by-org/{organizationId}")
//    public ResponseEntity<List<Department>> getDepartmentsByOrg(
//            @PathVariable String organizationId) {
//        return ResponseEntity.ok(
//                departmentService.getDepartmentsByOrganization(organizationId));
//    }
//
//    @PutMapping("/{id}")
//    public ResponseEntity<Department> updateDepartment(
//            @PathVariable Long id,
//            @RequestBody Department updated) {
//        return ResponseEntity.ok(departmentService.updateDepartment(id, updated));
//    }
//
//    @DeleteMapping("/{id}")
//    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
//        departmentService.deleteDepartment(id);
//        return ResponseEntity.noContent().build();
//    }
//    /* ===== SUPERADMIN ONLY — global departments (organizationId = null) ===== */
//    @GetMapping("/global")
//    public ResponseEntity<List<Department>> getGlobalDepartments() {
//        return ResponseEntity.ok(departmentService.getGlobalDepartments());
//    }
//}





package com.lms.batch.controller;

import com.lms.batch.entity.Department;
import com.lms.batch.security.JwtUtil;
import com.lms.batch.service.DepartmentService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/departments")
public class DepartmentController {

    private final DepartmentService departmentService;
    private final JwtUtil jwtUtil;

    public DepartmentController(DepartmentService departmentService, JwtUtil jwtUtil) {
        this.departmentService = departmentService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * POST /api/departments
     * Feature: create_department — org injected from JWT into department body,
     * enforcement happens inside DepartmentService.createDepartment.
     */
    @PostMapping
    public ResponseEntity<Department> createDepartment(
            @RequestBody Department department,
            @RequestHeader("Authorization") String authHeader) {
        if (authHeader != null && authHeader.startsWith("Bearer ")) {
            String orgId = jwtUtil.extractOrganizationId(authHeader.substring(7));
            if (orgId != null) {
                department.setOrganizationId(orgId);
            }
        }
        return ResponseEntity.ok(departmentService.createDepartment(department));
    }

    /**
     * GET /api/departments
     * Feature: get_departments — org comes from JWT.
     * Enforcement happens inside DepartmentService.getDepartmentsByOrganization.
     */
    @GetMapping
    public ResponseEntity<List<Department>> getAllDepartments(
            @RequestHeader("Authorization") String authHeader) {
        String orgId = jwtUtil.extractOrganizationId(authHeader.substring(7));
        return ResponseEntity.ok(departmentService.getDepartmentsByOrganization(orgId));
    }

    /**
     * GET /api/departments/{id}
     * Feature: get_department_by_id — org resolved inside service from the entity itself.
     */
    @GetMapping("/{id}")
    public ResponseEntity<Department> getDepartmentById(@PathVariable Long id) {
        return ResponseEntity.ok(departmentService.getDepartmentById(id));
    }

    /**
     * PUT /api/departments/{id}
     * Feature: update_department — org resolved inside service from the existing entity.
     */
    @PutMapping("/{id}")
    public ResponseEntity<Department> updateDepartment(
            @PathVariable Long id,
            @RequestBody Department updated) {
        return ResponseEntity.ok(departmentService.updateDepartment(id, updated));
    }

    /**
     * DELETE /api/departments/{id}
     * Feature: delete_department — org resolved inside service from the existing entity.
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteDepartment(@PathVariable Long id) {
        departmentService.deleteDepartment(id);
        return ResponseEntity.noContent().build();
    }

    /* ===== SUPERADMIN — NO enforcement ===== */

    @GetMapping("/by-org/{organizationId}")
    public ResponseEntity<List<Department>> getDepartmentsByOrg(
            @PathVariable String organizationId) {
        // SuperAdmin viewing another org — bypass enforcement, use raw repo method via service
        return ResponseEntity.ok(
                departmentService.getDepartmentsByOrganizationNoEnforce(organizationId));
    }

    @GetMapping("/global")
    public ResponseEntity<List<Department>> getGlobalDepartments() {
        return ResponseEntity.ok(departmentService.getGlobalDepartments());
    }
}