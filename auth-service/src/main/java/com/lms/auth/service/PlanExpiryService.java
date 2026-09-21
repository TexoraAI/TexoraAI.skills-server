package com.lms.auth.service;

import com.lms.auth.constants.DefaultOrgLimits;
import com.lms.auth.event.AuthEvent;
import com.lms.auth.model.Organization;
import com.lms.auth.model.User;
import com.lms.auth.producer.AuthEventProducer;
import com.lms.auth.repository.OrganizationRepository;
import com.lms.auth.repository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

@Service
public class PlanExpiryService {

    private static final Logger log = LoggerFactory.getLogger(PlanExpiryService.class);

    private final OrganizationRepository organizationRepository;
    private final UserRepository userRepository;
    private final AuthEventProducer authEventProducer;

    public PlanExpiryService(OrganizationRepository organizationRepository,
                              UserRepository userRepository,
                              AuthEventProducer authEventProducer) {
        this.organizationRepository = organizationRepository;
        this.userRepository = userRepository;
        this.authEventProducer = authEventProducer;
    }

    @Scheduled(cron = "0 0 2 * * *")
    @Transactional
    public void downgradeExpiredPlans() {
        LocalDate today = LocalDate.now();

        List<Organization> expiredOrgs = organizationRepository
                .findByPlanExpiryDateBeforeAndPlanNot(today, "trial");
        for (Organization org : expiredOrgs) {
            org.setPlan("trial");
            org.setMaxStudents(DefaultOrgLimits.MAX_STUDENTS);
            org.setMaxTrainers(DefaultOrgLimits.MAX_TRAINERS);
            org.setMaxDepartments(DefaultOrgLimits.MAX_DEPARTMENTS);
            org.setMaxBranchesPerDept(DefaultOrgLimits.MAX_BRANCHES_PER_DEPT);
            org.setMaxBatchesPerBranch(DefaultOrgLimits.MAX_BATCHES_PER_BRANCH);
            org.setPlanExpiryDate(null);
            organizationRepository.save(org);

            AuthEvent orgEvent = new AuthEvent(
                "ORG_UPDATED",
                null,
                org.getEmail(),
                null,
                org.getName(),
                org.getId().toString(),
                org.getMaxDepartments(),
                org.getMaxBranchesPerDept(),
                org.getMaxBatchesPerBranch()
            );
            orgEvent.setPlan("trial");
            orgEvent.setExpiresAt(null);
            authEventProducer.sendEvent(orgEvent);

            log.info("Downgraded expired org plan -> orgId={}", org.getId());
        }

        List<User> expiredUsers = userRepository
                .findByPlanExpiryDateBeforeAndPlanNot(today, "free");
        for (User user : expiredUsers) {
            user.setPlan("free");
            user.setPlanExpiryDate(null);
            userRepository.save(user);

            AuthEvent userEvent = new AuthEvent(
                "USER_PLAN_UPDATED",
                user.getId(),
                user.getEmail(),
                user.getRole().name(),
                user.getName(),
                null
            );
            userEvent.setPlan("free");
            userEvent.setExpiresAt(null);
            authEventProducer.sendEvent(userEvent);

            log.info("Downgraded expired individual plan -> userId={}", user.getId());
        }
    }
}