




package com.lms.batch.controller;

import com.lms.batch.entity.Branch;
import com.lms.batch.service.BranchService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/branch")
public class BranchController {

    private final BranchService branchService;

    public BranchController(BranchService branchService) {
        this.branchService = branchService;
    }

    /* ================= CREATE ================= */
    @PostMapping
    public Branch createBranch(@RequestBody Branch branch) {
        return branchService.createBranch(branch);
    }
    /* ================= DELETE ================= */
    @DeleteMapping("/{id}")
    public void deleteBranch(@PathVariable Long id) {
        branchService.deleteBranch(id);
    }

   

    
    /* ================= READ ================= */
    @GetMapping
    public List<Branch> getBranches() {
        return branchService.getAllBranches();
    }

    /* ================= UPDATE ================= */
    @PutMapping("/{id}")
    public Branch updateBranch(
            @PathVariable Long id,
            @RequestBody Branch branch
    ) {
        return branchService.updateBranch(id, branch);
    }

}
