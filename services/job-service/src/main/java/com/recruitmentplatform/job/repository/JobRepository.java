package com.recruitmentplatform.job.repository;

import com.recruitmentplatform.job.entity.Job;
import com.recruitmentplatform.job.enums.JobStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface JobRepository extends JpaRepository<Job, UUID>, JpaSpecificationExecutor<Job> {
    Optional<Job> findByIdAndDeletedFalse(UUID id);
    Page<Job> findByRecruiterUserIdAndDeletedFalse(UUID recruiterUserId, Pageable pageable);
    Page<Job> findByCompanyIdAndStatusAndDeletedFalse(UUID companyId, JobStatus status, Pageable pageable);

    @Modifying
    @Query("UPDATE Job j SET j.applicationCount = j.applicationCount + 1 WHERE j.id = :jobId")
    void incrementApplicationCount(UUID jobId);

    @Modifying
    @Query("UPDATE Job j SET j.applicationCount = CASE WHEN j.applicationCount > 0 THEN j.applicationCount - 1 ELSE 0 END WHERE j.id = :jobId")
    void decrementApplicationCount(UUID jobId);
}
