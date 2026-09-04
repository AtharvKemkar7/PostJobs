package com.recruitmentplatform.job.service;

import com.recruitmentplatform.job.dto.JobDtos;
import com.recruitmentplatform.job.entity.Job;
import com.recruitmentplatform.job.enums.JobEmploymentType;
import com.recruitmentplatform.job.enums.JobStatus;
import com.recruitmentplatform.job.enums.JobWorkMode;
import com.recruitmentplatform.job.event.JobEventProducer;
import com.recruitmentplatform.job.repository.JobRepository;
import com.recruitmentplatform.job.repository.JobSkillRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class JobServiceTest {

    @Mock
    private JobRepository jobRepository;

    @Mock
    private JobSkillRepository jobSkillRepository;

    @Mock
    private JobSearchService jobSearchService;

    @Mock
    private JobEventProducer eventProducer;

    @InjectMocks
    private JobService jobService;

    private UUID recruiterUserId;
    private UUID companyId;

    @BeforeEach
    void setUp() {
        recruiterUserId = UUID.randomUUID();
        companyId = UUID.randomUUID();
    }

    @Test
    void testCreateJobSuccess() {
        JobDtos.CreateRequest req = JobDtos.CreateRequest.builder()
                .companyId(companyId)
                .title("Senior Java Engineer")
                .description("Build microservices")
                .employmentType(JobEmploymentType.FULL_TIME)
                .workMode(JobWorkMode.REMOTE)
                .salaryMin(new BigDecimal("120000"))
                .salaryMax(new BigDecimal("150000"))
                .skills(List.of(new JobDtos.SkillItem("Java", true), new JobDtos.SkillItem("Kafka", false)))
                .build();

        when(jobRepository.save(any())).thenAnswer(inv -> {
            Job j = inv.getArgument(0);
            j.setId(UUID.randomUUID());
            return j;
        });
        when(jobSearchService.toResponse(any())).thenReturn(JobDtos.Response.builder().title("Senior Java Engineer").build());

        JobDtos.Response response = jobService.createJob(recruiterUserId, req);

        assertNotNull(response);
        assertEquals("Senior Java Engineer", response.getTitle());
        verify(jobSkillRepository, times(1)).saveAll(any());
    }

    @Test
    void testPublishJobSuccess() {
        UUID jobId = UUID.randomUUID();
        Job job = Job.builder()
                .id(jobId)
                .companyId(companyId)
                .recruiterUserId(recruiterUserId)
                .title("Senior Java Engineer")
                .status(JobStatus.DRAFT)
                .build();

        when(jobRepository.findByIdAndDeletedFalse(jobId)).thenReturn(Optional.of(job));
        when(jobRepository.save(any())).thenReturn(job);
        when(jobSearchService.toResponse(any())).thenReturn(JobDtos.Response.builder().id(jobId).status(JobStatus.PUBLISHED).build());

        JobDtos.Response response = jobService.publishJob(recruiterUserId, jobId);

        assertNotNull(response);
        assertEquals(JobStatus.PUBLISHED, response.getStatus());
        verify(eventProducer, times(1)).publishJobPublished(any(), any());
    }
}
