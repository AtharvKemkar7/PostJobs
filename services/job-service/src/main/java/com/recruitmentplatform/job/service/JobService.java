package com.recruitmentplatform.job.service;

import com.recruitmentplatform.job.dto.JobDtos;
import com.recruitmentplatform.job.entity.Job;
import com.recruitmentplatform.job.entity.JobSkill;
import com.recruitmentplatform.job.enums.JobStatus;
import com.recruitmentplatform.job.event.JobEventProducer;
import com.recruitmentplatform.job.repository.JobRepository;
import com.recruitmentplatform.job.repository.JobSkillRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class JobService {

    private final JobRepository jobRepository;
    private final JobSkillRepository jobSkillRepository;
    private final JobSearchService jobSearchService;
    private final JobEventProducer eventProducer;

    @Transactional
    public JobDtos.Response createJob(UUID recruiterUserId, JobDtos.CreateRequest req) {
        Job job = Job.builder()
                .companyId(req.getCompanyId())
                .recruiterUserId(recruiterUserId)
                .title(req.getTitle().trim())
                .description(req.getDescription())
                .requirements(req.getRequirements())
                .responsibilities(req.getResponsibilities())
                .locationCity(req.getLocationCity())
                .locationState(req.getLocationState())
                .locationCountry(req.getLocationCountry())
                .employmentType(req.getEmploymentType())
                .workMode(req.getWorkMode())
                .experienceMinYears(req.getExperienceMinYears() != null ? req.getExperienceMinYears() : 0)
                .experienceMaxYears(req.getExperienceMaxYears())
                .salaryMin(req.getSalaryMin())
                .salaryMax(req.getSalaryMax())
                .salaryCurrency(req.getSalaryCurrency() != null ? req.getSalaryCurrency() : "USD")
                .category(req.getCategory())
                .status(JobStatus.DRAFT)
                .build();

        job = jobRepository.save(job);

        if (req.getSkills() != null && !req.getSkills().isEmpty()) {
            final UUID jobId = job.getId();
            List<JobSkill> skills = req.getSkills().stream()
                    .map(s -> JobSkill.builder()
                            .jobId(jobId)
                            .skillName(s.getSkillName().trim())
                            .isRequired(s.isRequired())
                            .build())
                    .collect(Collectors.toList());
            jobSkillRepository.saveAll(skills);
        }

        return jobSearchService.toResponse(job);
    }

    public JobDtos.Response getJobById(UUID jobId) {
        Job job = jobRepository.findByIdAndDeletedFalse(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + jobId));
        return jobSearchService.toResponse(job);
    }

    public Page<JobDtos.Response> getRecruiterJobs(UUID recruiterUserId, Pageable pageable) {
        return jobRepository.findByRecruiterUserIdAndDeletedFalse(recruiterUserId, pageable)
                .map(jobSearchService::toResponse);
    }

    public Page<JobDtos.Response> getCompanyPublishedJobs(UUID companyId, Pageable pageable) {
        return jobRepository.findByCompanyIdAndStatusAndDeletedFalse(companyId, JobStatus.PUBLISHED, pageable)
                .map(jobSearchService::toResponse);
    }

    @Transactional
    public JobDtos.Response updateJob(UUID recruiterUserId, UUID jobId, JobDtos.UpdateRequest req) {
        Job job = getJobAndValidateOwnership(jobId, recruiterUserId);

        if (req.getTitle() != null) job.setTitle(req.getTitle().trim());
        if (req.getDescription() != null) job.setDescription(req.getDescription());
        if (req.getRequirements() != null) job.setRequirements(req.getRequirements());
        if (req.getResponsibilities() != null) job.setResponsibilities(req.getResponsibilities());
        if (req.getLocationCity() != null) job.setLocationCity(req.getLocationCity());
        if (req.getLocationState() != null) job.setLocationState(req.getLocationState());
        if (req.getLocationCountry() != null) job.setLocationCountry(req.getLocationCountry());
        if (req.getEmploymentType() != null) job.setEmploymentType(req.getEmploymentType());
        if (req.getWorkMode() != null) job.setWorkMode(req.getWorkMode());
        if (req.getExperienceMinYears() != null) job.setExperienceMinYears(req.getExperienceMinYears());
        if (req.getExperienceMaxYears() != null) job.setExperienceMaxYears(req.getExperienceMaxYears());
        if (req.getSalaryMin() != null) job.setSalaryMin(req.getSalaryMin());
        if (req.getSalaryMax() != null) job.setSalaryMax(req.getSalaryMax());
        if (req.getSalaryCurrency() != null) job.setSalaryCurrency(req.getSalaryCurrency());
        if (req.getCategory() != null) job.setCategory(req.getCategory());

        if (req.getSkills() != null) {
            jobSkillRepository.deleteByJobId(jobId);
            List<JobSkill> skills = req.getSkills().stream()
                    .map(s -> JobSkill.builder()
                            .jobId(jobId)
                            .skillName(s.getSkillName().trim())
                            .isRequired(s.isRequired())
                            .build())
                    .collect(Collectors.toList());
            jobSkillRepository.saveAll(skills);
        }

        return jobSearchService.toResponse(jobRepository.save(job));
    }

    @Transactional
    public JobDtos.Response publishJob(UUID recruiterUserId, UUID jobId) {
        Job job = getJobAndValidateOwnership(jobId, recruiterUserId);

        if (job.getStatus() != JobStatus.DRAFT) {
            throw new IllegalStateException("Only DRAFT jobs can be published");
        }

        job.setStatus(JobStatus.PUBLISHED);
        job.setPublishedAt(LocalDateTime.now());
        job = jobRepository.save(job);

        List<String> skillNames = jobSkillRepository.findByJobId(job.getId()).stream()
                .map(JobSkill::getSkillName)
                .collect(Collectors.toList());

        eventProducer.publishJobPublished(job, skillNames);

        return jobSearchService.toResponse(job);
    }

    @Transactional
    public JobDtos.Response closeJob(UUID recruiterUserId, UUID jobId) {
        Job job = getJobAndValidateOwnership(jobId, recruiterUserId);

        if (job.getStatus() != JobStatus.PUBLISHED) {
            throw new IllegalStateException("Only PUBLISHED jobs can be closed");
        }

        job.setStatus(JobStatus.CLOSED);
        job.setClosesAt(LocalDateTime.now());
        job = jobRepository.save(job);

        eventProducer.publishJobClosed(job);

        return jobSearchService.toResponse(job);
    }

    @Transactional
    public void deleteJob(UUID recruiterUserId, UUID jobId) {
        Job job = getJobAndValidateOwnership(jobId, recruiterUserId);
        job.setDeleted(true);
        job.setDeletedAt(LocalDateTime.now());
        jobRepository.save(job);
    }

    private Job getJobAndValidateOwnership(UUID jobId, UUID recruiterUserId) {
        Job job = jobRepository.findByIdAndDeletedFalse(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found"));

        if (!job.getRecruiterUserId().equals(recruiterUserId)) {
            throw new IllegalStateException("You are not the creator of this job posting");
        }
        return job;
    }
}
