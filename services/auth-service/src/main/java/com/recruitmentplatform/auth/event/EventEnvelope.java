package com.recruitmentplatform.auth.event;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EventEnvelope<T> {
    @Builder.Default
    private String eventId = UUID.randomUUID().toString();
    private String eventType;
    @Builder.Default
    private String eventVersion = "1.0";
    @Builder.Default
    private Instant timestamp = Instant.now();
    private String correlationId;
    @Builder.Default
    private String source = "auth-service";
    private T payload;
}
