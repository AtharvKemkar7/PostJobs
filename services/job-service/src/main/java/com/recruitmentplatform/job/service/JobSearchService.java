package com.recruitmentplatform.job.service;

import com.recruitmentplatform.job.dto.JobDtos;
import com.recruitmentplatform.job.entity.Job;
import com.recruitmentplatform.job.entity.JobSkill;
import com.recruitmentplatform.job.enums.JobStatus;
import com.recruitmentplatform.job.repository.JobRepository;
import com.recruitmentplatform.job.repository.JobSkillRepository;
import jakarta.persistence.criteria.Predicate;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobSearchService {

    private final JobRepository jobRepository;
    private final JobSkillRepository jobSkillRepository;

    public Page<JobDtos.Response> searchJobs(JobDtos.SearchFilter filter, Pageable pageable) {
        Specification<Job> spec = (root, query, cb) -> {
            List<Predicate> predicates = new ArrayList<>();

            // Always only published and non-deleted
            predicates.add(cb.equal(root.get("status"), JobStatus.PUBLISHED));
            predicates.add(cb.equal(root.get("deleted"), false));

            if (filter.getKeyword() != null && !filter.getKeyword().isBlank()) {
                String pattern = "%" + filter.getKeyword().toLowerCase().trim() + "%";
                Predicate titleMatch = cb.like(cb.lower(root.get("title")), pattern);
                Predicate descMatch = cb.like(cb.lower(root.get("description")), pattern);
                predicates.add(cb.or(titleMatch, descMatch));
            }

            if (filter.getLocationCountry() != null && !filter.getLocationCountry().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("locationCountry")), filter.getLocationCountry().toLowerCase().trim()));
            }

            if (filter.getLocationCity() != null && !filter.getLocationCity().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("locationCity")), filter.getLocationCity().toLowerCase().trim()));
            }

            if (filter.getEmploymentType() != null) {
                predicates.add(cb.equal(root.get("employmentType"), filter.getEmploymentType()));
            }

            if (filter.getWorkMode() != null) {
                predicates.add(cb.equal(root.get("workMode"), filter.getWorkMode()));
            }

            if (filter.getCategory() != null && !filter.getCategory().isBlank()) {
                predicates.add(cb.equal(cb.lower(root.get("category")), filter.getCategory().toLowerCase().trim()));
            }

            if (filter.getExperienceMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("experienceMinYears"), filter.getExperienceMin()));
            }

            if (filter.getExperienceMax() != null) {
                predicates.add(cb.lessThanOrEqualTo(root.get("experienceMaxYears"), filter.getExperienceMax()));
            }

            if (filter.getSalaryMin() != null) {
                predicates.add(cb.greaterThanOrEqualTo(root.get("salaryMax"), filter.getSalaryMin()));
            }

            return cb.and(predicates.toArray(new Predicate[0]));
        };

        return jobRepository.findAll(spec, pageable).map(this::toResponse);
    }

    public JobDtos.Response toResponse(Job job) {
        List<JobSkill> skills = jobSkillRepository.findByJobId(job.getId());
        List<JobDtos.SkillItem> skillItems = skills.stream()
                .map(s -> new JobDtos.SkillItem(s.getSkillName(), s.isRequired()))
                .collect(Collectors.toList());

        return JobDtos.Response.builder()
                .id(job.getId())
                .companyId(job.getCompanyId())
                .recruiterUserId(job.getRecruiterUserId())
                .title(job.getTitle())
                .description(job.getDescription())
                .requirements(job.getRequirements())
                .responsibilities(job.getResponsibilities())
                .locationCity(job.getLocationCity())
                .locationState(job.getLocationState())
                .locationCountry(job.getLocationCountry())
                .employmentType(job.getEmploymentType())
                .workMode(job.getWorkMode())
                .experienceMinYears(job.getExperienceMinYears())
                .experienceMaxYears(job.getExperienceMaxYears())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .salaryCurrency(job.getSalaryCurrency())
                .category(job.getCategory())
                .status(job.getStatus())
                .applicationCount(job.getApplicationCount())
                .skills(skillItems)
                .publishedAt(job.getPublishedAt())
                .closesAt(job.getClosesAt())
                .createdAt(job.getCreatedAt())
                .updatedAt(job.getUpdatedAt())
                .build();
    }
}
