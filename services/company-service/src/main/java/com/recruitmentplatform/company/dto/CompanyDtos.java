package com.recruitmentplatform.company.dto;

import com.recruitmentplatform.company.enums.CompanyStatus;
import com.recruitmentplatform.company.enums.EmployeeCountRange;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

public class CompanyDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class CreateRequest {
        @NotBlank(message = "Company name is required")
        @Size(max = 255)
        private String name;

        private String description;

        @Size(max = 100)
        private String industry;

        @Size(max = 500)
        private String website;

        @Size(max = 100)
        private String headquartersCity;

        @Size(max = 100)
        private String headquartersState;

        @Size(max = 100)
        private String headquartersCountry;

        private EmployeeCountRange employeeCount;
        private Integer foundedYear;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRequest {
        @Size(max = 255)
        private String name;

        private String description;

        @Size(max = 100)
        private String industry;

        @Size(max = 500)
        private String website;

        @Size(max = 100)
        private String headquartersCity;

        @Size(max = 100)
        private String headquartersState;

        @Size(max = 100)
        private String headquartersCountry;

        private EmployeeCountRange employeeCount;
        private Integer foundedYear;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateStatusRequest {
        private CompanyStatus status;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private String name;
        private String description;
        private String industry;
        private String website;
        private String logoUrl;
        private String headquartersCity;
        private String headquartersState;
        private String headquartersCountry;
        private EmployeeCountRange employeeCount;
        private Integer foundedYear;
        private CompanyStatus status;
        private UUID createdByUserId;
        private LocalDateTime createdAt;
        private LocalDateTime updatedAt;
    }
}
