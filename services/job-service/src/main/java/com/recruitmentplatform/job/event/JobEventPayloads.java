package com.recruitmentplatform.job.event;

import com.recruitmentplatform.job.enums.JobEmploymentType;
import com.recruitmentplatform.job.enums.JobWorkMode;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class JobEventPayloads {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobPublished {
        private UUID jobId;
        private UUID companyId;
        private String title;
        private List<String> skills;
        private JobEmploymentType employmentType;
        private JobWorkMode workMode;
        private String locationCountry;
        private String locationCity;
        private Integer experienceMinYears;
        private Integer experienceMaxYears;
        private BigDecimal salaryMin;
        private BigDecimal salaryMax;
        private String salaryCurrency;
        private String category;
        private LocalDateTime publishedAt;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class JobClosed {
        private UUID jobId;
        private UUID companyId;
        private LocalDateTime closedAt;
    }
}
