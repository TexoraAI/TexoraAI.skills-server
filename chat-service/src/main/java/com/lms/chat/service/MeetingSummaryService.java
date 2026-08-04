package com.lms.chat.service;

import com.lms.chat.dto.MeetingSummaryResponse;
import com.lms.chat.entity.MeetingSummary;
import com.lms.chat.repository.MeetingSummaryRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Service
public class MeetingSummaryService {

    private final MeetingSummaryRepository repository;

    public MeetingSummaryService(MeetingSummaryRepository repository) {
        this.repository = repository;
    }

    public MeetingSummaryResponse getSummary(Long meetingId, String requesterEmail,
                                              String requesterRole, Long requesterOrgId) {
        MeetingSummary summary = repository.findByMeetingId(meetingId)
                .orElseThrow(() -> new RuntimeException("Summary not found"));

        boolean isOwner = requesterEmail != null && requesterEmail.equalsIgnoreCase(summary.getRequestedByEmail());
        boolean isSuperAdmin = "SUPER_ADMIN".equalsIgnoreCase(requesterRole);
        boolean isTenantAdmin = "TENANT_ADMIN".equalsIgnoreCase(requesterRole)
                && summary.getOrganizationId() != null
                && summary.getOrganizationId().equals(requesterOrgId);

        if (!isOwner && !isSuperAdmin && !isTenantAdmin) {
            throw new RuntimeException("You don't have access to this summary");
        }

        return MeetingSummaryResponse.from(summary);
    }

    public List<MeetingSummaryResponse> listMySummaries(String requesterEmail) {
        return repository.findByRequestedByEmailOrderByCreatedAtDesc(requesterEmail)
                .stream()
                .map(MeetingSummaryResponse::from)
                .collect(Collectors.toList());
    }
}