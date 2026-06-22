//
//
//
//
//
//package com.lms.batch.controller;
//
//import com.lms.batch.entity.Branch;
//import com.lms.batch.service.BranchService;
//import org.springframework.web.bind.annotation.*;
//
//import java.util.List;
//import com.lms.batch.security.JwtUtil;
//@RestController
//@RequestMapping("/api/branch")
//public class BranchController {
//
//    private final BranchService branchService;
//    private final JwtUtil jwtUtil;
//    public BranchController(BranchService branchService,JwtUtil jwtUtil) {
//        this.branchService = branchService;
//        this.jwtUtil = jwtUtil;
//    }
//
//    /* ================= CREATE ================= */
//    @PostMapping
//    public Branch createBranch(@RequestBody Branch branch) {
//        return branchService.createBranch(branch);
//    }
//    /* ================= DELETE ================= */
//    @DeleteMapping("/{id}")
//    public void deleteBranch(@PathVariable Long id) {
//        branchService.deleteBranch(id);
//    }
//
//   
//
//    
//    /* ================= READ ================= */
////    @GetMapping
////    public List<Branch> getBranches() {
////        return branchService.getAllBranches();
////    }
//    @GetMapping
//    public List<Branch> getBranches(@RequestHeader("Authorization") String authHeader) {
//        String orgId = jwtUtil.extractOrganizationId(authHeader.substring(7));
//        return branchService.getAllBranches(orgId);
//    }
//
//    /* ================= UPDATE ================= */
//    @PutMapping("/{id}")
//    public Branch updateBranch(
//            @PathVariable Long id,
//            @RequestBody Branch branch
//    ) {
//        return branchService.updateBranch(id, branch);
//    }
//    /* ===== SUPERADMIN ONLY — global branches (organizationId = null) ===== */
//    @GetMapping("/global")
//    public List<Branch> getGlobalBranches() {
//        return branchService.getGlobalBranches();
//    }
// // ===== SUPERADMIN — get branches by orgId path variable =====
// // Used for SuperAdmin viewing another org's branches
// // Add this to BranchController.java
// @GetMapping("/by-org/{organizationId}")
// public List<Branch> getBranchesByOrg(
//         @PathVariable String organizationId) {
//     return branchService.getBranchesByOrg(organizationId);
// }
//}
package com.lms.batch.controller;

import com.lms.batch.entity.Branch;
import com.lms.batch.security.JwtUtil;
import com.lms.batch.service.BranchService;
import org.springframework.web.bind.annotation.*;
import java.util.List;

@RestController
@RequestMapping("/api/branch")
public class BranchController {

    private final BranchService branchService;
    private final JwtUtil jwtUtil;

    public BranchController(BranchService branchService, JwtUtil jwtUtil) {
        this.branchService = branchService;
        this.jwtUtil = jwtUtil;
    }

    /**
     * POST /api/branch
     * Feature: create_branch — org resolved inside service from the department entity.
     * No extra params needed here; departmentId is on the branch body.
     */
    @PostMapping
    public Branch createBranch(@RequestBody Branch branch) {
        return branchService.createBranch(branch);
    }

    /**
     * DELETE /api/branch/{id}
     * Feature: delete_branch — org resolved inside service from the branch entity.
     */
    @DeleteMapping("/{id}")
    public void deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
    }

    /**
     * GET /api/branch
     * Feature: get_branches — org comes from JWT.
     */
    @GetMapping
    public List<Branch> getBranches(@RequestHeader("Authorization") String authHeader) {
        String orgId = jwtUtil.extractOrganizationId(authHeader.substring(7));
        // enforcement happens inside BranchService.getAllBranches
        return branchService.getAllBranches(orgId);
    }

    /**
     * PUT /api/branch/{id}
     * Feature: update_branch — org resolved inside service from the branch entity.
     */
    @PutMapping("/{id}")
    public Branch updateBranch(
            @PathVariable Long id,
            @RequestBody Branch branch) {
        return branchService.updateBranch(id, branch);
    }

    /* ===== SUPERADMIN ONLY — NO enforcement ===== */

    @GetMapping("/global")
    public List<Branch> getGlobalBranches() {
        return branchService.getGlobalBranches();
    }

    @GetMapping("/by-org/{organizationId}")
    public List<Branch> getBranchesByOrg(@PathVariable String organizationId) {
        return branchService.getBranchesByOrg(organizationId);
    }
}