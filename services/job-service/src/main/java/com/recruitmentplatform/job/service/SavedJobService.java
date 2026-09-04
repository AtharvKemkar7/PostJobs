package com.recruitmentplatform.job.service;

import com.recruitmentplatform.job.dto.JobDtos;
import com.recruitmentplatform.job.entity.SavedJob;
import com.recruitmentplatform.job.repository.JobRepository;
import com.recruitmentplatform.job.repository.SavedJobRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class SavedJobService {

    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;
    private final JobSearchService jobSearchService;

    @Transactional
    public void saveJob(UUID candidateUserId, UUID jobId) {
        if (!jobRepository.existsById(jobId)) {
            throw new IllegalArgumentException("Job not found with id: " + jobId);
        }

        if (!savedJobRepository.existsByJobIdAndCandidateUserId(jobId, candidateUserId)) {
            SavedJob savedJob = SavedJob.builder()
                    .jobId(jobId)
                    .candidateUserId(candidateUserId)
                    .build();
            savedJobRepository.save(savedJob);
        }
    }

    @Transactional
    public void unsaveJob(UUID candidateUserId, UUID jobId) {
        savedJobRepository.deleteByJobIdAndCandidateUserId(jobId, candidateUserId);
    }

    public Page<JobDtos.Response> getSavedJobs(UUID candidateUserId, Pageable pageable) {
        return savedJobRepository.findByCandidateUserId(candidateUserId, pageable)
                .map(savedJob -> jobRepository.findByIdAndDeletedFalse(savedJob.getJobId())
                        .map(jobSearchService::toResponse)
                        .orElse(null));
    }
}
