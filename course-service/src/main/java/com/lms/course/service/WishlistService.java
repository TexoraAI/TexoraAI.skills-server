package com.lms.course.service;

import com.lms.course.dto.WishlistToggleResponseDTO;
import com.lms.course.exception.ResourceNotFoundException;
import com.lms.course.model.FeaturedProgram;
import com.lms.course.model.ProgramWishlist;
import com.lms.course.repository.FeaturedProgramRepository;
import com.lms.course.repository.ProgramWishlistRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class WishlistService {

    private final ProgramWishlistRepository wishlistRepository;
    private final FeaturedProgramRepository featuredProgramRepository;

    public WishlistService(ProgramWishlistRepository wishlistRepository,
                            FeaturedProgramRepository featuredProgramRepository) {
        this.wishlistRepository = wishlistRepository;
        this.featuredProgramRepository = featuredProgramRepository;
    }

    @Transactional
    public WishlistToggleResponseDTO toggle(Long programId, String userEmail, String userName) {
        FeaturedProgram program = featuredProgramRepository.findById(programId)
                .orElseThrow(() -> new ResourceNotFoundException("Program not found with id: " + programId));

        boolean alreadyWishlisted = wishlistRepository.existsByProgramIdAndUserEmail(programId, userEmail);

        if (alreadyWishlisted) {
            wishlistRepository.deleteByProgramIdAndUserEmail(programId, userEmail);
        } else {
            ProgramWishlist entry = new ProgramWishlist();
            entry.setProgram(program);
            entry.setUserEmail(userEmail);
            entry.setUserName(userName);
            wishlistRepository.save(entry);
        }

        long total = wishlistRepository.countByProgramId(programId);
        return new WishlistToggleResponseDTO(programId, !alreadyWishlisted, total);
    }

    @Transactional(readOnly = true)
    public List<Long> getMyWishlistedProgramIds(String userEmail) {
        return wishlistRepository.findAllByUserEmail(userEmail).stream()
                .map(w -> w.getProgram().getId())
                .collect(Collectors.toList());
    }
}