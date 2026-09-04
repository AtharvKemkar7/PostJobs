package com.recruitmentplatform.job.event;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.recruitmentplatform.job.repository.JobRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class JobApplicationEventListener {

    private final JobRepository jobRepository;
    private final ObjectMapper objectMapper;

    @KafkaListener(topics = "application-events", groupId = "job-service-group")
    @Transactional
    public void handleApplicationEvent(String message) {
        try {
            JsonNode root = objectMapper.readTree(message);
            String eventType = root.path("eventType").asText();
            JsonNode payload = root.path("payload");

            if ("ApplicationCreated".equals(eventType)) {
                UUID jobId = UUID.fromString(payload.path("jobId").asText());
                jobRepository.incrementApplicationCount(jobId);
                log.info("Incremented application count for jobId: {}", jobId);
            } else if ("ApplicationWithdrawn".equals(eventType)) {
                UUID jobId = UUID.fromString(payload.path("jobId").asText());
                jobRepository.decrementApplicationCount(jobId);
                log.info("Decremented application count for jobId: {}", jobId);
            }
        } catch (Exception e) {
            log.error("Error processing application event: {}", e.getMessage(), e);
        }
    }
}
