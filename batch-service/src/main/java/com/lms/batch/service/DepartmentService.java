//package com.lms.batch.service;
//
//import com.lms.batch.entity.Branch;
//import com.lms.batch.entity.Department;
//import com.lms.batch.kafka.BatchLifecycleProducer;
//import com.lms.batch.repository.BranchRepository;
//import com.lms.batch.repository.DepartmentRepository;
//import com.lms.batch.repository.OrgLimitsRepository;
//
//import org.springframework.http.HttpStatus;
//import org.springframework.stereotype.Service;
//import org.springframework.transaction.annotation.Transactional;
//import org.springframework.web.server.ResponseStatusException;
//
//import java.util.List;
//import com.lms.batch.entity.OrgLimits;
//@Service
//public class DepartmentService {
//
//    private final DepartmentRepository departmentRepository;
//    private final BranchRepository branchRepository;
//    private final BranchService branchService;
//    private final BatchLifecycleProducer lifecycleProducer;
//    private final OrgLimitsRepository  orgLimitsRepository;
//    public DepartmentService(
//            DepartmentRepository departmentRepository,
//            BranchRepository branchRepository,
//            BranchService branchService,
//            BatchLifecycleProducer lifecycleProducer,
//            OrgLimitsRepository  orgLimitsRepository
//    ) {
//        this.departmentRepository = departmentRepository;
//        this.branchRepository     = branchRepository;
//        this.branchService        = branchService;
//        this.lifecycleProducer    = lifecycleProducer;
//        this.orgLimitsRepository=orgLimitsRepository;
//        
//    }
//
//    /* ================= CREATE ================= */
//
////    public Department createDepartment(Department department) {
////        return departmentRepository.save(department);
////    }
////    public Department createDepartment(Department department) {
////        String orgId = department.getOrganizationId();
////        
////        OrgLimits limits = orgLimitsRepository
////            .findById(orgId).orElse(null);
////        
////        if (limits != null && limits.getMaxDepartments() != null) {
////            long count = departmentRepository
////                .countByOrganizationId(orgId);
////            if (count >= limits.getMaxDepartments()) {
////                throw new ResponseStatusException(HttpStatus.FORBIDDEN,
////                    "Department limit reached. Max: " 
////                    + limits.getMaxDepartments());
////            }
////        }
////        return departmentRepository.save(department);
////    }
//    public Department createDepartment(Department department) {
//        String orgId = department.getOrganizationId();
//
//        if (orgId != null) {
//            OrgLimits limits = orgLimitsRepository.findById(orgId).orElse(null);
//            if (limits != null && limits.getMaxDepartments() != null) {
//                long count = departmentRepository.countByOrganizationId(orgId);
//                if (count >= limits.getMaxDepartments()) {
//                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
//                        "Department limit reached. Max: " + limits.getMaxDepartments());
//                }
//            }
//        }
//
//        return departmentRepository.save(department);
//    }
//
//    /* ================= READ ================= */
//
//    public List<Department> getAllDepartments() {
//        return departmentRepository.findAll();
//    }
//
//    public List<Department> getDepartmentsByOrganization(String organizationId) {
//        return departmentRepository.findByOrganizationId(organizationId);
//    }
//
//    public Department getDepartmentById(Long id) {
//        return departmentRepository.findById(id)
//                .orElseThrow(() -> new RuntimeException("Department not found: " + id));
//    }
//
//    /* ================= UPDATE ================= */
//
//    public Department updateDepartment(Long id, Department updated) {
//        Department existing = getDepartmentById(id);
//        existing.setName(updated.getName());
//        existing.setHead(updated.getHead());
//        return departmentRepository.save(existing);
//    }
//
//    /* ================= DELETE (cascade) ================= */
//
//    /**
//     * Delete cascade:
//     * Department deleted
//     *   → all Branches under it deleted  (via branchService.deleteBranch)
//     *     → each branch delete fires BRANCH_DELETED Kafka event
//     *       → BatchService.deleteAllBatchesUnderBranch() handles batch cleanup
//     *         → each batch delete fires BATCH_DELETED Kafka event
//     *           → course-service / other services clean up content
//     * Finally fires DEPARTMENT_DELETED lifecycle event.
//     */
//    @Transactional
//    public void deleteDepartment(Long departmentId) {
//
//        Department department = getDepartmentById(departmentId);
//
//        // 1. Find all branches under this department
//        List<Branch> branches = branchRepository.findByDepartmentId(departmentId);
//
//        System.out.println("🏢 DELETING DEPARTMENT -> " + departmentId
//                + " | branches=" + branches.size());
//
//        // 2. Delete each branch — this triggers batch cascade (already working ✅)
//        for (Branch branch : branches) {
//            branchService.deleteBranch(branch.getId());
//        }
//
//        // 3. Delete the department itself
//        departmentRepository.delete(department);
//
//        // 4. Fire DEPARTMENT_DELETED lifecycle event
//        lifecycleProducer.departmentDeleted(departmentId);
//
//        System.out.println("✅ DEPARTMENT FULLY DELETED -> " + departmentId);
//    }
//    /* ===== SUPERADMIN — global (organizationId = null) departments ===== */
//    public List<Department> getGlobalDepartments() {
//        return departmentRepository.findByOrganizationIdIsNull();
//    }
//}



package com.lms.batch.service;

import com.lms.batch.constants.BatchFeatureKeys;
import com.lms.batch.entity.Branch;
import com.lms.batch.entity.Department;
import com.lms.batch.entity.OrgLimits;
import com.lms.batch.kafka.BatchLifecycleProducer;
import com.lms.batch.repository.BranchRepository;
import com.lms.batch.repository.DepartmentRepository;
import com.lms.batch.repository.OrgLimitsRepository;

import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class DepartmentService {

    private final DepartmentRepository      departmentRepository;
    private final BranchRepository          branchRepository;
    private final BranchService             branchService;
    private final BatchLifecycleProducer    lifecycleProducer;
    private final OrgLimitsRepository       orgLimitsRepository;
    private final BatchFeatureFlagsService  flagsService; // NEW

    public DepartmentService(
            DepartmentRepository departmentRepository,
            BranchRepository branchRepository,
            BranchService branchService,
            BatchLifecycleProducer lifecycleProducer,
            OrgLimitsRepository orgLimitsRepository,
            BatchFeatureFlagsService flagsService // NEW
    ) {
        this.departmentRepository = departmentRepository;
        this.branchRepository     = branchRepository;
        this.branchService        = branchService;
        this.lifecycleProducer    = lifecycleProducer;
        this.orgLimitsRepository  = orgLimitsRepository;
        this.flagsService         = flagsService; // NEW
    }

    /* ================= ADMIN: CREATE DEPARTMENT ================= */
    // Feature key: create_department — org-scoped (organizationId on the entity)
    public Department createDepartment(Department department) {
        String orgId = department.getOrganizationId();

        // NEW — enforce create_department for this org
        flagsService.enforce(orgId, null, BatchFeatureKeys.CREATE_DEPARTMENT);

        if (orgId != null) {
            OrgLimits limits = orgLimitsRepository.findById(orgId).orElse(null);
            if (limits != null && limits.getMaxDepartments() != null) {
                long count = departmentRepository.countByOrganizationId(orgId);
                if (count >= limits.getMaxDepartments()) {
                    throw new ResponseStatusException(HttpStatus.FORBIDDEN,
                        "Department limit reached. Max: " + limits.getMaxDepartments());
                }
            }
        }

        return departmentRepository.save(department);
    }

    /* ================= ADMIN: GET ALL DEPARTMENTS (org-scoped, JWT-derived org) ================= */
    // Feature key: get_departments — org-scoped
    public List<Department> getDepartmentsByOrganization(String organizationId) {

        // NEW — enforce get_departments for this org
        flagsService.enforce(organizationId, null, BatchFeatureKeys.GET_DEPARTMENTS);

        return departmentRepository.findByOrganizationId(organizationId);
    }

    /* ================= ADMIN: GET DEPARTMENT BY ID ================= */
    // Feature key: get_department_by_id — org-scoped (resolved from the department entity)
    public Department getDepartmentById(Long id) {

        Department dept = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found: " + id));

        // NEW — enforce get_department_by_id for this org (resolved from the entity itself)
        flagsService.enforce(dept.getOrganizationId(), null,
                BatchFeatureKeys.GET_DEPARTMENT_BY_ID);

        return dept;
    }

    /* ================= ADMIN: UPDATE DEPARTMENT ================= */
    // Feature key: update_department — org-scoped (resolved from existing entity)
    public Department updateDepartment(Long id, Department updated) {

        Department existing = departmentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("Department not found: " + id));

        // NEW — enforce update_department for this org
        flagsService.enforce(existing.getOrganizationId(), null,
                BatchFeatureKeys.UPDATE_DEPARTMENT);

        existing.setName(updated.getName());
        existing.setHead(updated.getHead());
        return departmentRepository.save(existing);
    }

    /* ================= ADMIN: DELETE DEPARTMENT (cascade) ================= */
    // Feature key: delete_department — org-scoped (resolved from existing entity)
    @Transactional
    public void deleteDepartment(Long departmentId) {

        Department department = departmentRepository.findById(departmentId)
                .orElseThrow(() -> new RuntimeException("Department not found: " + departmentId));

        // NEW — enforce delete_department for this org
        flagsService.enforce(department.getOrganizationId(), null,
                BatchFeatureKeys.DELETE_DEPARTMENT);

        // 1. Find all branches under this department
        List<Branch> branches = branchRepository.findByDepartmentId(departmentId);

        System.out.println("🏢 DELETING DEPARTMENT -> " + departmentId
                + " | branches=" + branches.size());

        // 2. Delete each branch — triggers batch cascade (already working ✅)
        for (Branch branch : branches) {
            branchService.deleteBranch(branch.getId());
        }

        // 3. Delete the department itself
        departmentRepository.delete(department);

        // 4. Fire DEPARTMENT_DELETED lifecycle event
        lifecycleProducer.departmentDeleted(departmentId);

        System.out.println("✅ DEPARTMENT FULLY DELETED -> " + departmentId);
    }

    /* ===== READ ALL — no feature key, used internally/superadmin only ===== */
    public List<Department> getAllDepartments() {
        return departmentRepository.findAll();
    }

    /**
     * SuperAdmin only — view another org's departments without enforcement.
     * Used by OrganizationDetailsPage and DepartmentController GET /by-org/{orgId}.
     */
    public List<Department> getDepartmentsByOrganizationNoEnforce(String organizationId) {
        return departmentRepository.findByOrganizationId(organizationId);
    }

    /* ===== SUPERADMIN — global (organizationId = null) departments — NO enforcement ===== */
    public List<Department> getGlobalDepartments() {
        return departmentRepository.findByOrganizationIdIsNull();
    }
}