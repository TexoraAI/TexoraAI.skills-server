//package com.lms.course.repository;
//
//import com.lms.course.model.Company;
//import org.springframework.data.domain.Page;
//import org.springframework.data.domain.Pageable;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.data.jpa.repository.Query;
//import org.springframework.data.repository.query.Param;
//
//import java.util.List;
//
//public interface CompanyRepository extends JpaRepository<Company, Long> {
//
//    long countByCategory(Company.Category category);
//
//    long countByStatus(Company.Status status);
//
//    @Query("SELECT c FROM Company c WHERE " +
//            "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', :search, '%')) " +
//            "OR LOWER(c.description) LIKE LOWER(CONCAT('%', :search, '%'))) " +
//            "AND (:category IS NULL OR c.category = :category) " +
//            "AND (:status IS NULL OR c.status = :status)")
//    Page<Company> search(@Param("search") String search,
//                          @Param("category") Company.Category category,
//                          @Param("status") Company.Status status,
//                          Pageable pageable);
//
//    List<Company> findByCategoryAndStatusOrderByDisplayOrderAscNameAsc(Company.Category category, Company.Status status);
//
//    List<Company> findByStatusOrderByCategoryAscDisplayOrderAscNameAsc(Company.Status status);
//}
package com.lms.course.repository;

import com.lms.course.model.Company;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface CompanyRepository extends JpaRepository<Company, Long> {

    long countByCategory(Company.Category category);

    long countByStatus(Company.Status status);

    @Query("SELECT c FROM Company c WHERE " +
            "(:search IS NULL OR LOWER(c.name) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%')) " +
            "OR LOWER(c.description) LIKE LOWER(CONCAT('%', CAST(:search AS string), '%'))) " +
            "AND (:category IS NULL OR c.category = :category) " +
            "AND (:status IS NULL OR c.status = :status)")
    Page<Company> search(@Param("search") String search,
                          @Param("category") Company.Category category,
                          @Param("status") Company.Status status,
                          Pageable pageable);

    List<Company> findByCategoryAndStatusOrderByDisplayOrderAscNameAsc(Company.Category category, Company.Status status);

    List<Company> findByStatusOrderByCategoryAscDisplayOrderAscNameAsc(Company.Status status);
}