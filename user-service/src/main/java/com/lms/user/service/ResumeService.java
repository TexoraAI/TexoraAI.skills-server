package com.lms.user.service;

import com.lms.user.dto.*;
import com.lms.user.exception.ResumeNotFoundException;
import com.lms.user.model.*;
import com.lms.user.repo.ResumeRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional
public class ResumeService {

    private final ResumeRepository resumeRepository;

    public ResumeService(ResumeRepository resumeRepository) {
        this.resumeRepository = resumeRepository;
    }

    // ===================== CREATE =====================
    public ResumeResponseDTO createResume(Long userId, ResumeRequestDTO request) {
        Resume resume = mapRequestToEntity(new Resume(), request);
        resume.setUserId(userId);
        resume.setResumeScore(calculateScore(resume));
        resume.setIsAtsFriendly(isAtsFriendly(resume));
        Resume saved = resumeRepository.save(resume);
        return mapToResponse(saved);
    }

    // ===================== UPDATE =====================
    public ResumeResponseDTO updateResume(Long userId, Long resumeId, ResumeRequestDTO request) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found with id: " + resumeId));

        resume.getWorkExperiences().clear();
        resume.getEducations().clear();
        resume.getSkills().clear();
        resume.getProjects().clear();
        resume.getCertifications().clear();

        mapRequestToEntity(resume, request);
        resume.setResumeScore(calculateScore(resume));
        resume.setIsAtsFriendly(isAtsFriendly(resume));

        Resume saved = resumeRepository.save(resume);
        return mapToResponse(saved);
    }

    // ===================== GET ALL =====================
    @Transactional(readOnly = true)
    public List<ResumeResponseDTO> getAllResumes(Long userId) {
        return resumeRepository.findByUserIdOrderByUpdatedAtDesc(userId)
                .stream()
                .map(this::mapToResponse)
                .collect(Collectors.toList());
    }

    // ===================== GET BY ID =====================
    @Transactional(readOnly = true)
    public ResumeResponseDTO getResumeById(Long userId, Long resumeId) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found with id: " + resumeId));
        return mapToResponse(resume);
    }

    // ===================== DELETE =====================
    public void deleteResume(Long userId, Long resumeId) {
        Resume resume = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found with id: " + resumeId));
        resumeRepository.delete(resume);
    }

    // ===================== DUPLICATE =====================
    public ResumeResponseDTO duplicateResume(Long userId, Long resumeId) {
        Resume original = resumeRepository.findByIdAndUserId(resumeId, userId)
                .orElseThrow(() -> new ResumeNotFoundException("Resume not found with id: " + resumeId));

        Resume copy = new Resume();
        copy.setUserId(userId);
        copy.setTitle(original.getTitle() + " (Copy)");
        copy.setTemplateName(original.getTemplateName());
        copy.setFirstName(original.getFirstName());
        copy.setLastName(original.getLastName());
        copy.setJobTitle(original.getJobTitle());
        copy.setEmail(original.getEmail());
        copy.setPhone(original.getPhone());
        copy.setCity(original.getCity());
        copy.setCountry(original.getCountry());
        copy.setLinkedinUrl(original.getLinkedinUrl());
        copy.setGithubUrl(original.getGithubUrl());
        copy.setPortfolioUrl(original.getPortfolioUrl());
        copy.setProfileSummary(original.getProfileSummary());

        for (WorkExperience we : original.getWorkExperiences()) {
            WorkExperience newWe = new WorkExperience();
            newWe.setResume(copy);
            newWe.setCompanyName(we.getCompanyName());
            newWe.setPosition(we.getPosition());
            newWe.setStartDate(we.getStartDate());
            newWe.setEndDate(we.getEndDate());
            newWe.setIsCurrent(we.getIsCurrent());
            newWe.setLocation(we.getLocation());
            newWe.setDescription(we.getDescription());
            newWe.setDisplayOrder(we.getDisplayOrder());
            copy.getWorkExperiences().add(newWe);
        }

        for (Education edu : original.getEducations()) {
            Education newEdu = new Education();
            newEdu.setResume(copy);
            newEdu.setInstitution(edu.getInstitution());
            newEdu.setDegree(edu.getDegree());
            newEdu.setFieldOfStudy(edu.getFieldOfStudy());
            newEdu.setStartDate(edu.getStartDate());
            newEdu.setEndDate(edu.getEndDate());
            newEdu.setGrade(edu.getGrade());
            newEdu.setDescription(edu.getDescription());
            newEdu.setDisplayOrder(edu.getDisplayOrder());
            copy.getEducations().add(newEdu);
        }

        for (ResumeSkill skill : original.getSkills()) {
            ResumeSkill newSkill = new ResumeSkill();
            newSkill.setResume(copy);
            newSkill.setSkillName(skill.getSkillName());
            newSkill.setProficiencyLevel(skill.getProficiencyLevel());
            newSkill.setDisplayOrder(skill.getDisplayOrder());
            copy.getSkills().add(newSkill);
        }

        for (Project proj : original.getProjects()) {
            Project newProj = new Project();
            newProj.setResume(copy);
            newProj.setProjectName(proj.getProjectName());
            newProj.setTechStack(proj.getTechStack());
            newProj.setProjectUrl(proj.getProjectUrl());
            newProj.setStartDate(proj.getStartDate());
            newProj.setEndDate(proj.getEndDate());
            newProj.setDescription(proj.getDescription());
            newProj.setDisplayOrder(proj.getDisplayOrder());
            copy.getProjects().add(newProj);
        }

        for (Certification cert : original.getCertifications()) {
            Certification newCert = new Certification();
            newCert.setResume(copy);
            newCert.setCertName(cert.getCertName());
            newCert.setIssuingOrganization(cert.getIssuingOrganization());
            newCert.setIssueDate(cert.getIssueDate());
            newCert.setExpiryDate(cert.getExpiryDate());
            newCert.setCredentialId(cert.getCredentialId());
            newCert.setCredentialUrl(cert.getCredentialUrl());
            newCert.setDisplayOrder(cert.getDisplayOrder());
            copy.getCertifications().add(newCert);
        }

        copy.setResumeScore(original.getResumeScore());
        copy.setIsAtsFriendly(original.getIsAtsFriendly());

        Resume saved = resumeRepository.save(copy);
        return mapToResponse(saved);
    }

    // ===================== SCORE CALCULATION =====================
    private int calculateScore(Resume resume) {
        int score = 0;

        if (resume.getFirstName() != null && !resume.getFirstName().isEmpty()) score += 5;
        if (resume.getEmail() != null && !resume.getEmail().isEmpty()) score += 5;
        if (resume.getPhone() != null && !resume.getPhone().isEmpty()) score += 5;
        if (resume.getCity() != null && !resume.getCity().isEmpty()) score += 5;

        if (resume.getProfileSummary() != null && resume.getProfileSummary().length() > 50) score += 15;

        if (!resume.getWorkExperiences().isEmpty()) score += Math.min(25, resume.getWorkExperiences().size() * 12);
        if (!resume.getEducations().isEmpty()) score += Math.min(15, resume.getEducations().size() * 10);
        if (!resume.getSkills().isEmpty()) score += Math.min(15, resume.getSkills().size() * 3);
        if (!resume.getProjects().isEmpty()) score += 5;
        if (!resume.getCertifications().isEmpty()) score += 5;

        if (resume.getLinkedinUrl() != null && !resume.getLinkedinUrl().isEmpty()) score += 5;
        if (resume.getGithubUrl() != null && !resume.getGithubUrl().isEmpty()) score += 5;

        return Math.min(100, score);
    }

    private boolean isAtsFriendly(Resume resume) {
        return resume.getEmail() != null
                && resume.getProfileSummary() != null
                && !resume.getSkills().isEmpty()
                && !resume.getWorkExperiences().isEmpty();
    }

    // ===================== MAPPER HELPERS =====================
    private Resume mapRequestToEntity(Resume resume, ResumeRequestDTO request) {
        resume.setTitle(request.getTitle());
        resume.setTemplateName(request.getTemplateName());
        resume.setFirstName(request.getFirstName());
        resume.setLastName(request.getLastName());
        resume.setJobTitle(request.getJobTitle());
        resume.setEmail(request.getEmail());
        resume.setPhone(request.getPhone());
        resume.setCity(request.getCity());
        resume.setCountry(request.getCountry());
        resume.setLinkedinUrl(request.getLinkedinUrl());
        resume.setGithubUrl(request.getGithubUrl());
        resume.setPortfolioUrl(request.getPortfolioUrl());
        resume.setProfileSummary(request.getProfileSummary());

        if (request.getWorkExperiences() != null) {
            request.getWorkExperiences().forEach(dto -> {
                WorkExperience we = new WorkExperience();
                we.setResume(resume);
                we.setCompanyName(dto.getCompanyName());
                we.setPosition(dto.getPosition());
                we.setStartDate(dto.getStartDate());
                we.setEndDate(dto.getEndDate());
                we.setIsCurrent(dto.getIsCurrent());
                we.setLocation(dto.getLocation());
                we.setDescription(dto.getDescription());
                we.setDisplayOrder(dto.getDisplayOrder());
                resume.getWorkExperiences().add(we);
            });
        }

        if (request.getEducations() != null) {
            request.getEducations().forEach(dto -> {
                Education edu = new Education();
                edu.setResume(resume);
                edu.setInstitution(dto.getInstitution());
                edu.setDegree(dto.getDegree());
                edu.setFieldOfStudy(dto.getFieldOfStudy());
                edu.setStartDate(dto.getStartDate());
                edu.setEndDate(dto.getEndDate());
                edu.setGrade(dto.getGrade());
                edu.setDescription(dto.getDescription());
                edu.setDisplayOrder(dto.getDisplayOrder());
                resume.getEducations().add(edu);
            });
        }

        if (request.getSkills() != null) {
            request.getSkills().forEach(dto -> {
                ResumeSkill skill = new ResumeSkill();
                skill.setResume(resume);
                skill.setSkillName(dto.getSkillName());
                if (dto.getProficiencyLevel() != null) {
                    skill.setProficiencyLevel(ResumeSkill.ProficiencyLevel.valueOf(dto.getProficiencyLevel()));
                }
                skill.setDisplayOrder(dto.getDisplayOrder());
                resume.getSkills().add(skill);
            });
        }

        if (request.getProjects() != null) {
            request.getProjects().forEach(dto -> {
                Project project = new Project();
                project.setResume(resume);
                project.setProjectName(dto.getProjectName());
                project.setTechStack(dto.getTechStack());
                project.setProjectUrl(dto.getProjectUrl());
                project.setStartDate(dto.getStartDate());
                project.setEndDate(dto.getEndDate());
                project.setDescription(dto.getDescription());
                project.setDisplayOrder(dto.getDisplayOrder());
                resume.getProjects().add(project);
            });
        }

        if (request.getCertifications() != null) {
            request.getCertifications().forEach(dto -> {
                Certification cert = new Certification();
                cert.setResume(resume);
                cert.setCertName(dto.getCertName());
                cert.setIssuingOrganization(dto.getIssuingOrganization());
                cert.setIssueDate(dto.getIssueDate());
                cert.setExpiryDate(dto.getExpiryDate());
                cert.setCredentialId(dto.getCredentialId());
                cert.setCredentialUrl(dto.getCredentialUrl());
                cert.setDisplayOrder(dto.getDisplayOrder());
                resume.getCertifications().add(cert);
            });
        }

        return resume;
    }

    // ===================== KEY FIX: mapToResponse uses separate DTO classes now =====================
    private ResumeResponseDTO mapToResponse(Resume resume) {
        ResumeResponseDTO dto = new ResumeResponseDTO();
        dto.setId(resume.getId());
        dto.setUserId(resume.getUserId());
        dto.setTitle(resume.getTitle());
        dto.setTemplateName(resume.getTemplateName());
        dto.setResumeScore(resume.getResumeScore());
        dto.setIsAtsFriendly(resume.getIsAtsFriendly());
        dto.setCreatedAt(resume.getCreatedAt());
        dto.setUpdatedAt(resume.getUpdatedAt());
        dto.setFirstName(resume.getFirstName());
        dto.setLastName(resume.getLastName());
        dto.setJobTitle(resume.getJobTitle());
        dto.setEmail(resume.getEmail());
        dto.setPhone(resume.getPhone());
        dto.setCity(resume.getCity());
        dto.setCountry(resume.getCountry());
        dto.setLinkedinUrl(resume.getLinkedinUrl());
        dto.setGithubUrl(resume.getGithubUrl());
        dto.setPortfolioUrl(resume.getPortfolioUrl());
        dto.setProfileSummary(resume.getProfileSummary());

        // ✅ Using top-level WorkExperienceResponseDTO (not inner class)
        dto.setWorkExperiences(resume.getWorkExperiences().stream().map(we -> {
            WorkExperienceResponseDTO r = new WorkExperienceResponseDTO();
            r.setId(we.getId());
            r.setCompanyName(we.getCompanyName());
            r.setPosition(we.getPosition());
            r.setStartDate(we.getStartDate());
            r.setEndDate(we.getEndDate());
            r.setIsCurrent(we.getIsCurrent());
            r.setLocation(we.getLocation());
            r.setDescription(we.getDescription());
            r.setDisplayOrder(we.getDisplayOrder());
            return r;
        }).collect(Collectors.toList()));

        // ✅ Using top-level EducationResponseDTO
        dto.setEducations(resume.getEducations().stream().map(edu -> {
            EducationResponseDTO r = new EducationResponseDTO();
            r.setId(edu.getId());
            r.setInstitution(edu.getInstitution());
            r.setDegree(edu.getDegree());
            r.setFieldOfStudy(edu.getFieldOfStudy());
            r.setStartDate(edu.getStartDate());
            r.setEndDate(edu.getEndDate());
            r.setGrade(edu.getGrade());
            r.setDescription(edu.getDescription());
            r.setDisplayOrder(edu.getDisplayOrder());
            return r;
        }).collect(Collectors.toList()));

        // ✅ Using top-level ResumeSkillResponseDTO
        dto.setSkills(resume.getSkills().stream().map(skill -> {
            ResumeSkillResponseDTO r = new ResumeSkillResponseDTO();
            r.setId(skill.getId());
            r.setSkillName(skill.getSkillName());
            r.setProficiencyLevel(skill.getProficiencyLevel() != null ? skill.getProficiencyLevel().name() : null);
            r.setDisplayOrder(skill.getDisplayOrder());
            return r;
        }).collect(Collectors.toList()));

        // ✅ Using top-level ProjectResponseDTO
        dto.setProjects(resume.getProjects().stream().map(proj -> {
            ProjectResponseDTO r = new ProjectResponseDTO();
            r.setId(proj.getId());
            r.setProjectName(proj.getProjectName());
            r.setTechStack(proj.getTechStack());
            r.setProjectUrl(proj.getProjectUrl());
            r.setStartDate(proj.getStartDate());
            r.setEndDate(proj.getEndDate());
            r.setDescription(proj.getDescription());
            r.setDisplayOrder(proj.getDisplayOrder());
            return r;
        }).collect(Collectors.toList()));

        // ✅ Using top-level CertificationResponseDTO
        dto.setCertifications(resume.getCertifications().stream().map(cert -> {
            CertificationResponseDTO r = new CertificationResponseDTO();
            r.setId(cert.getId());
            r.setCertName(cert.getCertName());
            r.setIssuingOrganization(cert.getIssuingOrganization());
            r.setIssueDate(cert.getIssueDate());
            r.setExpiryDate(cert.getExpiryDate());
            r.setCredentialId(cert.getCredentialId());
            r.setCredentialUrl(cert.getCredentialUrl());
            r.setDisplayOrder(cert.getDisplayOrder());
            return r;
        }).collect(Collectors.toList()));

        return dto;
    }
}