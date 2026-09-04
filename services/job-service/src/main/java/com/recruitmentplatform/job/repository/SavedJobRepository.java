package com.recruitmentplatform.job.repository;

import com.recruitmentplatform.job.entity.SavedJob;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SavedJobRepository extends JpaRepository<SavedJob, UUID> {
    Page<SavedJob> findByCandidateUserId(UUID candidateUserId, Pageable pageable);
    Optional<SavedJob> findByJobIdAndCandidateUserId(UUID jobId, UUID candidateUserId);
    boolean existsByJobIdAndCandidateUserId(UUID jobId, UUID candidateUserId);
    void deleteByJobIdAndCandidateUserId(UUID jobId, UUID candidateUserId);
}
