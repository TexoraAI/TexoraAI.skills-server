package com.lms.course.service;

import com.lms.course.dto.CompanyRequest;
import com.lms.course.dto.CompanyResponse;
import com.lms.course.dto.CompanyStatsResponse;
import com.lms.course.dto.PageResponse;
import com.lms.course.exception.ResourceNotFoundException;
import com.lms.course.model.Company;
import com.lms.course.repository.CompanyRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Service
@Transactional
public class CompanyService {

    private final CompanyRepository companyRepository;

    public CompanyService(CompanyRepository companyRepository) {
        this.companyRepository = companyRepository;
    }

    public CompanyResponse create(CompanyRequest request) {
        Company company = new Company();
        applyRequestToEntity(request, company);
        Company saved = companyRepository.save(company);
        return toResponse(saved);
    }

    public CompanyResponse update(Long id, CompanyRequest request) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        applyRequestToEntity(request, company);
        Company saved = companyRepository.save(company);
        return toResponse(saved);
    }

    public void delete(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        companyRepository.delete(company);
    }

    @Transactional(readOnly = true)
    public CompanyResponse getById(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        return toResponse(company);
    }

    @Transactional(readOnly = true)
    public PageResponse<CompanyResponse> getAll(String search, String category, String status, int page, int size) {
        int zeroIndexedPage = Math.max(page - 1, 0);
        PageRequest pageRequest = PageRequest.of(zeroIndexedPage, size, Sort.by(Sort.Direction.DESC, "createdAt"));

        String normalizedSearch = (search == null || search.isBlank()) ? null : search.trim();
        Company.Category categoryEnum = (category == null || category.isBlank() || category.equalsIgnoreCase("all"))
                ? null : parseCategory(category);
        Company.Status statusEnum = (status == null || status.isBlank() || status.equalsIgnoreCase("all"))
                ? null : parseStatus(status);

        Page<Company> resultPage = companyRepository.search(normalizedSearch, categoryEnum, statusEnum, pageRequest);

        PageResponse<CompanyResponse> response = new PageResponse<>();
        List<CompanyResponse> content = new ArrayList<>();
        for (Company company : resultPage.getContent()) {
            content.add(toResponse(company));
        }
        response.setContent(content);
        response.setTotalElements((int) resultPage.getTotalElements());
        response.setTotalPages(resultPage.getTotalPages());
        response.setNumber(resultPage.getNumber() + 1);
        return response;
    }

    public CompanyResponse toggleStatus(Long id) {
        Company company = companyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Company not found with id: " + id));
        company.setStatus(company.getStatus() == Company.Status.ACTIVE ? Company.Status.INACTIVE : Company.Status.ACTIVE);
        Company saved = companyRepository.save(company);
        return toResponse(saved);
    }

    @Transactional(readOnly = true)
    public CompanyStatsResponse getStats() {
        long total = companyRepository.count();
        long techPartners = companyRepository.countByCategory(Company.Category.TECHNOLOGY_PARTNER);
        long businessPartners = companyRepository.countByCategory(Company.Category.BUSINESS_PARTNER);
        long texoraProducts = companyRepository.countByCategory(Company.Category.TEXORA_PRODUCT);
        long active = companyRepository.countByStatus(Company.Status.ACTIVE);
        return new CompanyStatsResponse(total, techPartners, businessPartners, texoraProducts, active);
    }

    @Transactional(readOnly = true)
    public Map<String, List<CompanyResponse>> getActiveForLandingPage() {
        List<Company> activeCompanies = companyRepository.findByStatusOrderByCategoryAscDisplayOrderAscNameAsc(Company.Status.ACTIVE);

        Map<String, List<CompanyResponse>> grouped = new LinkedHashMap<>();
        grouped.put("Technology Partner", new ArrayList<>());
        grouped.put("Business Partner", new ArrayList<>());
        grouped.put("Texora Product", new ArrayList<>());

        for (Company company : activeCompanies) {
            String key = categoryToDisplay(company.getCategory());
            grouped.computeIfAbsent(key, k -> new ArrayList<>()).add(toResponse(company));
        }
        return grouped;
    }

    // ---------- helpers ----------

    private void applyRequestToEntity(CompanyRequest request, Company company) {
        company.setName(request.getName());
        company.setShortName(
                (request.getShortName() == null || request.getShortName().isBlank())
                        ? generateShortName(request.getName())
                        : request.getShortName()
        );
        company.setDescription(request.getDescription());
        company.setWebsite(request.getWebsite());
        company.setCategory(parseCategory(request.getCategory()));
        company.setStatus(request.getStatus() == null || request.getStatus().isBlank()
                ? Company.Status.ACTIVE
                : parseStatus(request.getStatus()));
        company.setLogoUrl(request.getLogoUrl());
        company.setUploadedLogo(request.getUploadedLogo());
        company.setDisplayOrder(request.getDisplayOrder() == null ? 0 : request.getDisplayOrder());
    }

    private String generateShortName(String name) {
        if (name == null || name.isBlank()) {
            return null;
        }
        String trimmed = name.trim();
        String[] words = trimmed.split("\\s+");
        if (words.length == 1) {
            return trimmed.length() <= 4 ? trimmed.toUpperCase() : trimmed.substring(0, 4).toUpperCase();
        }
        StringBuilder shortName = new StringBuilder();
        for (String word : words) {
            if (!word.isEmpty()) {
                shortName.append(Character.toUpperCase(word.charAt(0)));
            }
        }
        return shortName.toString();
    }

    private Company.Category parseCategory(String raw) {
        String normalized = normalizeEnumString(raw);
        try {
            return Company.Category.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid category: " + raw);
        }
    }

    private Company.Status parseStatus(String raw) {
        String normalized = normalizeEnumString(raw);
        try {
            return Company.Status.valueOf(normalized);
        } catch (IllegalArgumentException ex) {
            throw new IllegalArgumentException("Invalid status: " + raw);
        }
    }

    private String normalizeEnumString(String raw) {
        if (raw == null) {
            throw new IllegalArgumentException("Value must not be null");
        }
        return raw.trim().toUpperCase().replaceAll("[\\s-]+", "_");
    }

    private String categoryToDisplay(Company.Category category) {
        switch (category) {
            case TECHNOLOGY_PARTNER:
                return "Technology Partner";
            case BUSINESS_PARTNER:
                return "Business Partner";
            case TEXORA_PRODUCT:
                return "Texora Product";
            default:
                return category.name();
        }
    }

    private String statusToDisplay(Company.Status status) {
        switch (status) {
            case ACTIVE:
                return "Active";
            case INACTIVE:
                return "Inactive";
            default:
                return status.name();
        }
    }

    private CompanyResponse toResponse(Company company) {
        CompanyResponse response = new CompanyResponse();
        response.setId(company.getId());
        response.setName(company.getName());
        response.setShortName(company.getShortName());
        response.setDescription(company.getDescription());
        response.setWebsite(company.getWebsite());
        response.setCategory(categoryToDisplay(company.getCategory()));
        response.setStatus(statusToDisplay(company.getStatus()));
        response.setLogoUrl(company.getLogoUrl());
        response.setUploadedLogo(company.getUploadedLogo());
        response.setDisplayOrder(company.getDisplayOrder());
        response.setCreatedAt(company.getCreatedAt());
        response.setUpdatedAt(company.getUpdatedAt());
        return response;
    }
}