package com.lms.live_session.repository;

import com.lms.live_session.entity.BookingPageSettings;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface BookingPageSettingsRepository extends JpaRepository<BookingPageSettings, Long> {

    Optional<BookingPageSettings> findByTrainerEmail(String trainerEmail);

    Optional<BookingPageSettings> findByPublicSlug(String publicSlug);

    boolean existsByPublicSlug(String publicSlug);

    boolean existsByPublicSlugAndTrainerEmailNot(String publicSlug, String trainerEmail);

    // NEW – used by getPublishedTrainers() public listing endpoint
    List<BookingPageSettings> findByIsPublishedTrue();
}