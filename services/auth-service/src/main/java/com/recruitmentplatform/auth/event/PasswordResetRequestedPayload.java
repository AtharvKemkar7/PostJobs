package com.recruitmentplatform.auth.event;

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
public class PasswordResetRequestedPayload {
    private UUID userId;
    private String email;
    private String resetToken;
    private LocalDateTime expiresAt;
}
