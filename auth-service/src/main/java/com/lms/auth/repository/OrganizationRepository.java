
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
    List<Organization> findByPlanExpiryDateBeforeAndPlanNot(java.time.LocalDate date, String plan);

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