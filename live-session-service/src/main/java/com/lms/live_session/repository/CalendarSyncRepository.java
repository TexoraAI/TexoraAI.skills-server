package com.lms.live_session.repository;

import com.lms.live_session.entity.CalendarSync;
import com.lms.live_session.entity.SyncStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CalendarSyncRepository extends JpaRepository<CalendarSync, Long> {

    Optional<CalendarSync> findByUserId(String userId);

    Optional<CalendarSync> findByGoogleEmail(String googleEmail);

    // NOTE: typed as the SyncStatus enum, not String. syncStatus is persisted with
    // @Enumerated(EnumType.STRING), so a derived-query parameter typed String won't
    // bind correctly against it - Spring Data matches on the property's declared type.
    List<CalendarSync> findBySyncStatus(SyncStatus syncStatus);

    // Derived delete methods don't need @Modifying (that's only required for
    // @Query-based bulk JPQL/native updates); Spring Data handles this natively.
    void deleteByUserId(String userId);
}