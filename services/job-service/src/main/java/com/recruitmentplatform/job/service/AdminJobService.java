package com.recruitmentplatform.job.service;

import com.recruitmentplatform.job.dto.JobDtos;
import com.recruitmentplatform.job.entity.Job;
import com.recruitmentplatform.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminJobService {

    private final JobRepository jobRepository;
    private final JobSearchService jobSearchService;

    public Page<JobDtos.Response> listAllJobs(Pageable pageable) {
        return jobRepository.findAll(pageable)
                .map(jobSearchService::toResponse);
    }

    @Transactional
    public void deleteJob(UUID id) {
        Job job = jobRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Job not found with id: " + id));
        job.setDeleted(true);
        job.setDeletedAt(LocalDateTime.now());
        jobRepository.save(job);
    }
}
