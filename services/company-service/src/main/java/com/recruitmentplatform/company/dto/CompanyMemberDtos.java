package com.recruitmentplatform.company.dto;

import com.recruitmentplatform.company.enums.CompanyMemberRole;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

public class CompanyMemberDtos {

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class AddRequest {
        @NotNull(message = "User ID is required")
        private UUID userId;

        @NotNull(message = "Member role is required")
        private CompanyMemberRole role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class UpdateRoleRequest {
        @NotNull(message = "Member role is required")
        private CompanyMemberRole role;
    }

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Response {
        private UUID id;
        private UUID companyId;
        private UUID userId;
        private CompanyMemberRole role;
        private LocalDateTime joinedAt;
    }
}
