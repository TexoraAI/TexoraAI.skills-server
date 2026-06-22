//
//
//package com.lms.auth.repository;
//
//import com.lms.auth.model.Organization;
//import org.springframework.data.jpa.repository.JpaRepository;
//import org.springframework.stereotype.Repository;
//
//import java.util.List;
//import java.util.Optional; // ← ADDED
//import java.util.UUID;
//
//@Repository
//public interface OrganizationRepository extends JpaRepository<Organization, UUID> {
//    List<Organization> findByStatusOrderByNameAsc(String status);
//    boolean existsByIdAndStatus(UUID id, String status);
//    Optional<Organization> findByOwnerId(Long ownerId); // ← ADDED
//}

// OPTIMIZATION: Added findOrgUserCounts() @Query to replace N+1 count calls
// in getAllOrganizations(). Previously called countByOrganizationIdAndRole twice
// per org (2N queries for N orgs). New query fetches all org counts in one
// GROUP BY query and returns a Map<UUID, long[]> (index 0=students, 1=trainers).

package com.lms.auth.repository;

import com.lms.auth.model.Organization;
import com.lms.auth.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Repository
public interface OrganizationRepository extends JpaRepository<Organization, UUID> {

    List<Organization> findByStatusOrderByNameAsc(String status);
    boolean existsByIdAndStatus(UUID id, String status);
    Optional<Organization> findByOwnerId(Long ownerId);

    // OPTIMIZATION: Replaces 2N count queries in getAllOrganizations().
    // Returns list of Object[] rows: [organizationId, role, count].
    // Caller maps this into Map<UUID, long[]> with [0]=students, [1]=trainers.
    @Query("""
        SELECT u.organizationId, u.role, COUNT(u)
        FROM User u
        WHERE u.organizationId IS NOT NULL
          AND u.role IN ('STUDENT', 'TRAINER')
        GROUP BY u.organizationId, u.role
        """)
    List<Object[]> findOrgUserCountsRaw();

    // OPTIMIZATION: Default method converts raw rows to Map<UUID, long[]>
    // so OrganizationService only calls one method.
    default Map<UUID, long[]> findOrgUserCounts() {
        Map<UUID, long[]> result = new java.util.HashMap<>();
        for (Object[] row : findOrgUserCountsRaw()) {
            UUID orgId = (UUID) row[0];
            String roleStr = row[1].toString();
            long count = ((Number) row[2]).longValue();
            result.computeIfAbsent(orgId, k -> new long[]{0L, 0L});
            if ("STUDENT".equals(roleStr))  result.get(orgId)[0] = count;
            if ("TRAINER".equals(roleStr))  result.get(orgId)[1] = count;
        }
        return result;
    }
}