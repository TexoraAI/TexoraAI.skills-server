//
//
//package com.lms.batch.service;
//
//import com.lms.batch.entity.Branch;
//import com.lms.batch.entity.Department;
//import com.lms.batch.entity.OrgLimits;
//import com.lms.batch.kafka.BatchLifecycleProducer;
//import com.lms.batch.repository.BatchRepository;
//import com.lms.batch.repository.BranchRepository;
//import com.lms.batch.repository.DepartmentRepository;
//import com.lms.batch.repository.OrgLimitsRepository;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//
//import java.util.List;
//import org.springframework.web.server.ResponseStatusException;
//@Service
//public class BranchService {
//
//    private final BranchRepository      branchRepository;
//    private final BatchRepository       batchrepository;
//    private final BatchLifecycleProducer batchlifecycleproducer;
//    private final DepartmentRepository  departmentRepository; // NEW
//    private final OrgLimitsRepository  orgLimitsRepository;
//    public BranchService(
//            BranchRepository branchRepository,
//            BatchRepository batchrepository,
//            BatchLifecycleProducer batchlifecycleproducer,
//            DepartmentRepository departmentRepository ,  // NEW
//            OrgLimitsRepository  orgLimitsRepository
//    ) {
//        this.batchrepository        = batchrepository;
//        this.branchRepository       = branchRepository;
//        this.batchlifecycleproducer = batchlifecycleproducer;
//        this.departmentRepository   = departmentRepository;  // NEW
//        this.orgLimitsRepository=orgLimitsRepository;
//    }
//
//    /* ================= CREATE ================= */
//
//      
//    
//    public Branch createBranch(Branch branch) {
//        if (branch.getDepartmentId() != null) {
//            departmentRepository.findById(branch.getDepartmentId())
//                .ifPresent(dept -> branch.setOrganizationId(dept.getOrganizationId()));
//        }
//
//        String orgId = branch.getOrganizationId();
//
//        if (orgId != null) {
//            OrgLimits limits = orgLimitsRepository.findById(orgId).orElse(null);
//            if (limits != null && limits.getMaxBranchesPerDept() != null) {
//                long count = branchRepository.countByDepartmentId(branch.getDepartmentId());
//                if (count >= limits.getMaxBranchesPerDept()) {
//                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
//                        "Branch limit reached for this department. Max: "
//                        + limits.getMaxBranchesPerDept());
//                }
//            }
//        }
//
//        return branchRepository.save(branch);
//    }
//    /* ================= DELETE ================= */
//
//    @Transactional
//    public void deleteBranch(Long id) {
//
//        Branch branch = branchRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Branch not found"));
//
//        // Fire BRANCH_DELETED → BranchLifecycleConsumer calls
//        // deleteAllBatchesUnderBranch (already working ✅)
//        batchlifecycleproducer.branchDeleted(id);
//
//        branchRepository.delete(branch);
//
//        System.out.println("🔥 BRANCH DELETED FROM DB -> " + id);
//    }
//
//    /* ================= READ ================= */
//
////    public List<Branch> getAllBranches() {
////        return branchRepository.findAll();
////    }
//    public List<Branch> getAllBranches(String organizationId) {
//        return branchRepository.findByOrganizationId(organizationId);
//    }
//
//    // NEW — fetch branches by department
//    public List<Branch> getBranchesByDepartment(Long departmentId) {
//        return branchRepository.findByDepartmentId(departmentId);
//    }
//
//    /* ================= UPDATE ================= */
//
//    public Branch updateBranch(Long id, Branch updated) {
//
//        Branch existing = branchRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Branch not found"));
//
//        existing.setName(updated.getName());
//        existing.setCity(updated.getCity());
//        // departmentId and organizationId are immutable after creation — do NOT update
//
//        return branchRepository.save(existing);
//    }
//
//    public boolean existsById(Long id) {
//        return branchRepository.existsById(id);
//    }
//    
//    /* ===== SUPERADMIN — global (organizationId = null) branches ===== */
//    public List<Branch> getGlobalBranches() {
//        return branchRepository.findByOrganizationIdIsNull();
//    }
// // ===== SUPERADMIN — get branches by orgId (called via path variable, not JWT) =====
// // Used for SuperAdmin viewing another org's branches in OrganizationDetailsPage
// public List<Branch> getBranchesByOrg(String organizationId) {
//     return branchRepository.findByOrganizationId(organizationId);
// }
//}

package com.lms.batch.service;

import com.lms.batch.constants.BatchFeatureKeys;
import com.lms.batch.entity.Branch;
import com.lms.batch.entity.Department;
import com.lms.batch.entity.OrgLimits;
import com.lms.batch.kafka.BatchLifecycleProducer;
import com.lms.batch.repository.BatchRepository;
import com.lms.batch.repository.BranchRepository;
import com.lms.batch.repository.DepartmentRepository;
import com.lms.batch.repository.OrgLimitsRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BranchService {

    private final BranchRepository      branchRepository;
    private final BatchRepository       batchrepository;
    private final BatchLifecycleProducer batchlifecycleproducer;
    private final DepartmentRepository  departmentRepository;
    private final OrgLimitsRepository  orgLimitsRepository;
    private final BatchFeatureFlagsService flagsService; // NEW

    public BranchService(
            BranchRepository branchRepository,
            BatchRepository batchrepository,
            BatchLifecycleProducer batchlifecycleproducer,
            DepartmentRepository departmentRepository,
            OrgLimitsRepository  orgLimitsRepository,
            BatchFeatureFlagsService flagsService // NEW
    ) {
        this.batchrepository        = batchrepository;
        this.branchRepository       = branchRepository;
        this.batchlifecycleproducer = batchlifecycleproducer;
        this.departmentRepository   = departmentRepository;
        this.orgLimitsRepository=orgLimitsRepository;
        this.flagsService = flagsService; // NEW
    }

    /* ================= ADMIN: CREATE BRANCH ================= */
    // Feature key: create_branch — org-scoped (resolved from department's organizationId)
    public Branch createBranch(Branch branch) {
        if (branch.getDepartmentId() != null) {
            departmentRepository.findById(branch.getDepartmentId())
                .ifPresent(dept -> branch.setOrganizationId(dept.getOrganizationId()));
        }

        String orgId = branch.getOrganizationId();

        // NEW — enforce create_branch for this org
        flagsService.enforce(orgId, null, BatchFeatureKeys.CREATE_BRANCH);

        if (orgId != null) {
            OrgLimits limits = orgLimitsRepository.findById(orgId).orElse(null);
            if (limits != null && limits.getMaxBranchesPerDept() != null) {
                long count = branchRepository.countByDepartmentId(branch.getDepartmentId());
                if (count >= limits.getMaxBranchesPerDept()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Branch limit reached for this department. Max: "
                        + limits.getMaxBranchesPerDept());
                }
            }
        }

        return branchRepository.save(branch);
    }

    /* ================= ADMIN: DELETE BRANCH ================= */
    // Feature key: delete_branch — org-scoped (resolved from branch's organizationId)
    @Transactional
    public void deleteBranch(Long id) {

        Branch branch = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        // NEW — enforce delete_branch for this org
        flagsService.enforce(branch.getOrganizationId(), null, BatchFeatureKeys.DELETE_BRANCH);

        // Fire BRANCH_DELETED → BranchLifecycleConsumer calls
        // deleteAllBatchesUnderBranch (already working ✅)
        batchlifecycleproducer.branchDeleted(id);

        branchRepository.delete(branch);

        System.out.println("🔥 BRANCH DELETED FROM DB -> " + id);
    }

    /* ================= ADMIN: GET BRANCHES BY ORG ================= */
    // Feature key: get_branches — org-scoped
    public List<Branch> getAllBranches(String organizationId) {

        // NEW — enforce get_branches for this org
        flagsService.enforce(organizationId, null, BatchFeatureKeys.GET_BRANCHES);

        return branchRepository.findByOrganizationId(organizationId);
    }

    /* ================= INTERNAL — used by DepartmentService cascade, no direct feature key ================= */
    public List<Branch> getBranchesByDepartment(Long departmentId) {
        return branchRepository.findByDepartmentId(departmentId);
    }

    /* ================= ADMIN: UPDATE BRANCH ================= */
    // Feature key: update_branch — org-scoped (resolved from branch's organizationId)
    public Branch updateBranch(Long id, Branch updated) {

        Branch existing = branchRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Branch not found"));

        // NEW — enforce update_branch for this org
        flagsService.enforce(existing.getOrganizationId(), null, BatchFeatureKeys.UPDATE_BRANCH);

        existing.setName(updated.getName());
        existing.setCity(updated.getCity());
        // departmentId and organizationId are immutable after creation — do NOT update

        return branchRepository.save(existing);
    }

    public boolean existsById(Long id) {
        return branchRepository.existsById(id);
    }

    /* ===== SUPERADMIN — global (organizationId = null) branches — NO enforcement ===== */
    public List<Branch> getGlobalBranches() {
        return branchRepository.findByOrganizationIdIsNull();
    }

    /* ===== SUPERADMIN — get branches by orgId (viewing another org) — NO enforcement ===== */
    public List<Branch> getBranchesByOrg(String organizationId) {
        return branchRepository.findByOrganizationId(organizationId);
    }
}