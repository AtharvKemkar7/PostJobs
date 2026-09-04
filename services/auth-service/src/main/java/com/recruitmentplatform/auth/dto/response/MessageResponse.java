package com.recruitmentplatform.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class MessageResponse {
    private String message;
    @Builder.Default
    private Instant timestamp = Instant.now();

    public MessageResponse(String message) {
        this.message = message;
        this.timestamp = Instant.now();
    }
}
