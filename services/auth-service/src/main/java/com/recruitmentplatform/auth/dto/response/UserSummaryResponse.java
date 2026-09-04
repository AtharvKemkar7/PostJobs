package com.recruitmentplatform.auth.dto.response;

import com.recruitmentplatform.auth.enums.AccountRole;
import com.recruitmentplatform.auth.enums.AccountStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserSummaryResponse {
    private UUID userId;
    private String email;
    private AccountRole role;
    private AccountStatus status;
    private boolean emailVerified;
    private LocalDateTime createdAt;
}
