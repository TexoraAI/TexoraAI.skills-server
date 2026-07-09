package com.lms.course.dto;

public class WishlistToggleResponseDTO {
    private Long programId;
    private boolean wishlisted;
    private long totalWishlistCount;

    public WishlistToggleResponseDTO() {}

    public WishlistToggleResponseDTO(Long programId, boolean wishlisted, long totalWishlistCount) {
        this.programId = programId;
        this.wishlisted = wishlisted;
        this.totalWishlistCount = totalWishlistCount;
    }

    public Long getProgramId() { return programId; }
    public void setProgramId(Long programId) { this.programId = programId; }

    public boolean isWishlisted() { return wishlisted; }
    public void setWishlisted(boolean wishlisted) { this.wishlisted = wishlisted; }

    public long getTotalWishlistCount() { return totalWishlistCount; }
    public void setTotalWishlistCount(long totalWishlistCount) { this.totalWishlistCount = totalWishlistCount; }
}