package com.recruitmentplatform.job.dto;

import com.recruitmentplatform.job.enums.JobEmploymentType;
import com.recruitmentplatform.job.enums.JobStatus;
import com.recruitmentplatform.job.enums.JobWorkMode;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

public class JobDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SkillItem {
        @NotBlank
        private String skillName;
        @Builder.Default
        private boolean isRequired = true;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotNull(message = "Company ID is required")
        private UUID companyId;

        @NotBlank(message = "Job title is required")
        @Size(max = 255)
        private String title;

        @NotBlank(message = "Job description is required")
        private String description;

        private String requirements;
        private String responsibilities;

        @Size(max = 100)
        private String locationCity;
        @Size(max = 100)
        private String locationState;
        @Size(max = 100)
        private String locationCountry;

        @NotNull(message = "Employment type is required")
        private JobEmploymentType employmentType;

        @NotNull(message = "Work mode is required")
        private JobWorkMode workMode;

        private Integer experienceMinYears;
        private Integer experienceMaxYears;

        private BigDecimal salaryMin;
        private BigDecimal salaryMax;
        private String salaryCurrency;

        @Size(max = 100)
        private String category;

        private List<SkillItem> skills;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(max = 255)
        private String title;
        private String description;
        private String requirements;
        private String responsibilities;
        private String locationCity;
        private String locationState;
        private String locationCountry;
        private JobEmploymentType employmentType;
        private JobWorkMode workMode;
        private Integer experienceMinYears;
        private Integer experienceMaxYears;
        private BigDecimal salaryMin;
        private BigDecimal salaryMax;
        private String salaryCurrency;
        private String category;
        private List<SkillItem> skills;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class SearchFilter {
        private String keyword;
        private String locationCountry;
        private String locationCity;
        private JobEmploymentType employmentType;
        private JobWorkMode workMode;
        private String category;
        private Integer experienceMin;
        private Integer experienceMax;
        private BigDecimal salaryMin;
        private BigDecimal salaryMax;
        private List<String> skills;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID companyId;
        private UUID recruiterUserId;
        private String title;
        private String description;
        private String requirements;
        private String responsibilities;
        private String locationCity;
        private String locationState;
        private String locationCountry;
        private JobEmploymentType employmentType;
        private JobWorkMode workMode;
        private Integer experienceMinYears;
        private Integer experienceMaxYears;
        private BigDecimal salaryMin;
        private BigDecimal salaryMax;
        private String salaryCurrency;
        private String category;
        private JobStatus status;
        private Integer applicationCount;
        private List<SkillItem> skills;
        private LocalDateTime publishedAt;
        private LocalDateTime closesAt;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
