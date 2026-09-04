package com.recruitmentplatform.job.event;

import com.recruitmentplatform.job.dto.JobDtos;
import com.recruitmentplatform.job.entity.Job;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobEventProducer {

    private static final String TOPIC = "job-events";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishJobPublished(Job job, List<String> skills) {
        JobEventPayloads.JobPublished payload = JobEventPayloads.JobPublished.builder()
                .jobId(job.getId())
                .companyId(job.getCompanyId())
                .title(job.getTitle())
                .skills(skills)
                .employmentType(job.getEmploymentType())
                .workMode(job.getWorkMode())
                .locationCountry(job.getLocationCountry())
                .locationCity(job.getLocationCity())
                .experienceMinYears(job.getExperienceMinYears())
                .experienceMaxYears(job.getExperienceMaxYears())
                .salaryMin(job.getSalaryMin())
                .salaryMax(job.getSalaryMax())
                .salaryCurrency(job.getSalaryCurrency())
                .category(job.getCategory())
                .publishedAt(job.getPublishedAt())
                .build();

        log.info("Publishing JobPublished event for jobId: {}", job.getId());
        kafkaTemplate.send(TOPIC, job.getId().toString(), payload);
    }

    public void publishJobClosed(Job job) {
        JobEventPayloads.JobClosed payload = JobEventPayloads.JobClosed.builder()
                .jobId(job.getId())
                .companyId(job.getCompanyId())
                .closedAt(LocalDateTime.now())
                .build();

        log.info("Publishing JobClosed event for jobId: {}", job.getId());
        kafkaTemplate.send(TOPIC, job.getId().toString(), payload);
    }
}
