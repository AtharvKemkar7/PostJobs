package com.recruitmentplatform.job.controller;

import com.recruitmentplatform.job.dto.JobDtos;
import com.recruitmentplatform.job.enums.JobEmploymentType;
import com.recruitmentplatform.job.enums.JobWorkMode;
import com.recruitmentplatform.job.service.JobSearchService;
import com.recruitmentplatform.job.service.JobService;
import com.recruitmentplatform.job.service.SavedJobService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/jobs")
@RequiredArgsConstructor
public class JobController {

    private final JobService jobService;
    private final JobSearchService jobSearchService;
    private final SavedJobService savedJobService;

    @GetMapping
    public ResponseEntity<Page<JobDtos.Response>> searchJobs(
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String locationCountry,
            @RequestParam(required = false) String locationCity,
            @RequestParam(required = false) JobEmploymentType employmentType,
            @RequestParam(required = false) JobWorkMode workMode,
            @RequestParam(required = false) String category,
            @RequestParam(required = false) Integer experienceMin,
            @RequestParam(required = false) Integer experienceMax,
            @RequestParam(required = false) BigDecimal salaryMin,
            @RequestParam(required = false) BigDecimal salaryMax,
            @PageableDefault(size = 20, sort = "publishedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        JobDtos.SearchFilter filter = JobDtos.SearchFilter.builder()
                .keyword(keyword)
                .locationCountry(locationCountry)
                .locationCity(locationCity)
                .employmentType(employmentType)
                .workMode(workMode)
                .category(category)
                .experienceMin(experienceMin)
                .experienceMax(experienceMax)
                .salaryMin(salaryMin)
                .salaryMax(salaryMax)
                .build();

        return ResponseEntity.ok(jobSearchService.searchJobs(filter, pageable));
    }

    @GetMapping("/{id}")
    public ResponseEntity<JobDtos.Response> getJob(@PathVariable UUID id) {
        return ResponseEntity.ok(jobService.getJobById(id));
    }

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobDtos.Response> createJob(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody JobDtos.CreateRequest request) {
        return new ResponseEntity<>(jobService.createJob(userId, request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobDtos.Response> updateJob(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody JobDtos.UpdateRequest request) {
        return ResponseEntity.ok(jobService.updateJob(userId, id, request));
    }

    @PostMapping("/{id}/publish")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobDtos.Response> publishJob(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(jobService.publishJob(userId, id));
    }

    @PostMapping("/{id}/close")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<JobDtos.Response> closeJob(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(jobService.closeJob(userId, id));
    }

    @DeleteMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Void> deleteJob(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        jobService.deleteJob(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Page<JobDtos.Response>> getMyJobs(
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(size = 20, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        return ResponseEntity.ok(jobService.getRecruiterJobs(userId, pageable));
    }

    @GetMapping("/company/{companyId}")
    public ResponseEntity<Page<JobDtos.Response>> getCompanyJobs(
            @PathVariable UUID companyId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(jobService.getCompanyPublishedJobs(companyId, pageable));
    }

    // --- Saved Jobs ---
    @PostMapping("/{id}/save")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Void> saveJob(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        savedJobService.saveJob(userId, id);
        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{id}/save")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Void> unsaveJob(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        savedJobService.unsaveJob(userId, id);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/saved")
    @PreAuthorize("hasRole('CANDIDATE')")
    public ResponseEntity<Page<JobDtos.Response>> getSavedJobs(
            @AuthenticationPrincipal UUID userId,
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(savedJobService.getSavedJobs(userId, pageable));
    }
}
