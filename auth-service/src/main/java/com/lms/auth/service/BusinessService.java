//package com.lms.auth.service;
//
//import com.lms.auth.dto.BusinessApplyRequest;
//import com.lms.auth.model.BusinessProfile;
//import com.lms.auth.model.Role;
//import com.lms.auth.model.User;
//import com.lms.auth.repository.BusinessProfileRepository;
//import com.lms.auth.repository.UserRepository;
//import org.springframework.http.HttpStatus;
//import org.springframework.security.crypto.password.PasswordEncoder;
//import org.springframework.stereotype.Service;
//import org.springframework.web.server.ResponseStatusException;
//
//@Service
//public class BusinessService {
//
//    private final UserRepository userRepository;
//    private final BusinessProfileRepository businessProfileRepository;
//    private final PasswordEncoder passwordEncoder;
//
//    public BusinessService(UserRepository userRepository,
//                           BusinessProfileRepository businessProfileRepository,
//                           PasswordEncoder passwordEncoder) {
//        this.userRepository = userRepository;
//        this.businessProfileRepository = businessProfileRepository;
//        this.passwordEncoder = passwordEncoder;
//    }
//
//    public void applyBusiness(BusinessApplyRequest request) {
//
//        if (userRepository.existsByEmail(request.getEmail())) {
//            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
//        }
//
//        // 1) Create user
//        User user = new User();
//        user.setName(request.getBusinessName()); // show businessName as user name
//        user.setEmail(request.getEmail());
//        user.setPassword(passwordEncoder.encode(request.getPassword()));
//        user.setRole(Role.BUSINESS);
//
//        // pending approval
//        user.setApproved(false);
//
//        User savedUser = userRepository.save(user);
//
//        // 2) Create business profile
//        BusinessProfile profile = new BusinessProfile();
//        profile.setUser(savedUser);
//
//        // Step-1
//        profile.setBusinessName(request.getBusinessName());
//        profile.setOwnerName(request.getOwnerName());
//        profile.setMobileNumber(request.getMobileNumber());
//        profile.setBusinessType(request.getBusinessType());
//        profile.setIndustryDomain(request.getIndustryDomain());
//        profile.setLocation(request.getLocation());
//        profile.setWebsite(request.getWebsite());
//
//        // Step-2
//        profile.setCompanySize(request.getCompanySize());
//        profile.setYearsOfExperience(request.getYearsOfExperience());
//        profile.setLookingFor(request.getLookingFor());
//        profile.setAboutBusiness(request.getAboutBusiness());
//        profile.setExpectedOutcome(request.getExpectedOutcome());
//
//        businessProfileRepository.save(profile);
//    }
//}





package com.lms.auth.service;

import com.lms.auth.dto.BusinessApplyRequest;
import com.lms.auth.event.AuthEvent;                    // ✅ ADDED
import com.lms.auth.model.BusinessProfile;
import com.lms.auth.model.Role;
import com.lms.auth.model.User;
import com.lms.auth.producer.AuthEventProducer;         // ✅ ADDED
import com.lms.auth.repository.BusinessProfileRepository;
import com.lms.auth.repository.UserRepository;
import org.springframework.http.HttpStatus;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

@Service
public class BusinessService {

    private final UserRepository userRepository;
    private final BusinessProfileRepository businessProfileRepository;
    private final PasswordEncoder passwordEncoder;

    // ✅ ADDED (Kafka Producer)
    private final AuthEventProducer authEventProducer;

    // ❌ constructor logic NOT changed — ONLY parameter added
    public BusinessService(UserRepository userRepository,
                           BusinessProfileRepository businessProfileRepository,
                           PasswordEncoder passwordEncoder,
                           AuthEventProducer authEventProducer) {

        this.userRepository = userRepository;
        this.businessProfileRepository = businessProfileRepository;
        this.passwordEncoder = passwordEncoder;
        this.authEventProducer = authEventProducer;
    }

    public void applyBusiness(BusinessApplyRequest request) {

        if (userRepository.existsByEmail(request.getEmail())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT, "Email already registered");
        }

        User user = new User();
        user.setName(request.getBusinessName());
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword()));
        user.setRole(Role.BUSINESS);
        user.setApproved(false);

        User savedUser = userRepository.save(user);

        BusinessProfile profile = new BusinessProfile();
        profile.setUser(savedUser);
        profile.setBusinessName(request.getBusinessName());
        profile.setOwnerName(request.getOwnerName());
        profile.setMobileNumber(request.getMobileNumber());
        profile.setBusinessType(request.getBusinessType());
        profile.setIndustryDomain(request.getIndustryDomain());
        profile.setLocation(request.getLocation());
        profile.setWebsite(request.getWebsite());
        profile.setCompanySize(request.getCompanySize());
        profile.setYearsOfExperience(request.getYearsOfExperience());
        profile.setLookingFor(request.getLookingFor());
        profile.setAboutBusiness(request.getAboutBusiness());
        profile.setExpectedOutcome(request.getExpectedOutcome());

        businessProfileRepository.save(profile);
    }

    // ✅ Admin: approve business — ONLY KAFKA ADDED
    public void approveBusiness(Long userId) {

        User user = userRepository.findById(userId)
                .orElseThrow(() ->
                        new ResponseStatusException(
                                HttpStatus.NOT_FOUND, "Business not found"
                        )
                );

        if (user.getRole() != Role.BUSINESS) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "Not a business user"
            );
        }

        user.setApproved(true);
        userRepository.save(user);

        // 🔥 ONLY MISSING PART (Kafka)
        AuthEvent event = new AuthEvent();
        event.setEventType("USER_CREATED");
        event.setUserId(user.getId());
        event.setEmail(user.getEmail());
        event.setRole("BUSINESS");
        event.setDisplayName(user.getName());

        authEventProducer.sendEvent(event);
    }
}
