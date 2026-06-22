//package com.lms.batch.repository;
//
//import com.lms.batch.entity.Branch;
//import org.springframework.data.jpa.repository.JpaRepository;
//
//public interface BranchRepository extends JpaRepository<Branch, Long> {
//}
package com.lms.batch.repository;

import com.lms.batch.entity.Branch;

import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface BranchRepository extends JpaRepository<Branch, Long> {

    // NEW — used by DepartmentService.deleteDepartment() and BranchService.getBranchesByDepartment()
    List<Branch> findByDepartmentId(Long departmentId);

    // NEW — used if listing branches by org is needed
    List<Branch> findByOrganizationId(String organizationId);
    
    List<Branch> findByOrganizationIdIsNull();
    
    long countByDepartmentId(Long departmentId);
    
    long countByOrganizationId(String organizationId);
}