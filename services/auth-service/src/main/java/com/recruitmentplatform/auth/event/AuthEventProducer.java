package com.recruitmentplatform.auth.event;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
@Slf4j
public class AuthEventProducer {

    private static final String USER_EVENTS_TOPIC = "user-events";
    private final KafkaTemplate<String, Object> kafkaTemplate;

    public void publishUserRegistered(UserRegisteredPayload payload, String correlationId) {
        EventEnvelope<UserRegisteredPayload> envelope = EventEnvelope.<UserRegisteredPayload>builder()
                .eventType("UserRegistered")
                .correlationId(correlationId)
                .payload(payload)
                .build();

        log.info("Publishing UserRegistered event for userId: {}", payload.getUserId());
        kafkaTemplate.send(USER_EVENTS_TOPIC, payload.getUserId().toString(), envelope);
    }

    public void publishEmailVerificationRequested(EmailVerificationRequestedPayload payload, String correlationId) {
        EventEnvelope<EmailVerificationRequestedPayload> envelope = EventEnvelope.<EmailVerificationRequestedPayload>builder()
                .eventType("EmailVerificationRequested")
                .correlationId(correlationId)
                .payload(payload)
                .build();

        log.info("Publishing EmailVerificationRequested event for userId: {}", payload.getUserId());
        kafkaTemplate.send(USER_EVENTS_TOPIC, payload.getUserId().toString(), envelope);
    }

    public void publishPasswordResetRequested(PasswordResetRequestedPayload payload, String correlationId) {
        EventEnvelope<PasswordResetRequestedPayload> envelope = EventEnvelope.<PasswordResetRequestedPayload>builder()
                .eventType("PasswordResetRequested")
                .correlationId(correlationId)
                .payload(payload)
                .build();

        log.info("Publishing PasswordResetRequested event for userId: {}", payload.getUserId());
        kafkaTemplate.send(USER_EVENTS_TOPIC, payload.getUserId().toString(), envelope);
    }
}
