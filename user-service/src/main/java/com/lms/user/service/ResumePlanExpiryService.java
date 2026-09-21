package com.lms.user.service;

import com.lms.user.model.User;
import com.lms.user.repo.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

// WHY: Downgrades expired individually-purchased resume-plan overrides back to
// no-override (null), mirroring auth-service's PlanExpiryService pattern.
@Service
public class ResumePlanExpiryService {

    private static final Logger log = LoggerFactory.getLogger(ResumePlanExpiryService.class);

    private final UserRepository userRepository;

    public ResumePlanExpiryService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void downgradeExpiredResumePlanOverrides() {
        LocalDate today = LocalDate.now();

        List<User> expiredUsers = userRepository
                .findByResumePlanOverrideExpiryDateBeforeAndResumePlanOverrideIsNotNull(today);

        for (User user : expiredUsers) {
            user.setResumePlanOverride(null);
            user.setResumePlanOverrideExpiryDate(null);
            userRepository.save(user);
            log.info("Downgraded expired resume-plan override -> userId={}", user.getId());
        }
    }
}