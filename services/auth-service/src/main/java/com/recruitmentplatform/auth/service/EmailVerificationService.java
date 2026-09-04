package com.recruitmentplatform.auth.service;

import com.recruitmentplatform.auth.dto.response.MessageResponse;
import com.recruitmentplatform.auth.entity.EmailVerification;
import com.recruitmentplatform.auth.entity.UserCredential;
import com.recruitmentplatform.auth.enums.AccountStatus;
import com.recruitmentplatform.auth.event.AuthEventProducer;
import com.recruitmentplatform.auth.event.EmailVerificationRequestedPayload;
import com.recruitmentplatform.auth.exception.BadRequestException;
import com.recruitmentplatform.auth.exception.ResourceNotFoundException;
import com.recruitmentplatform.auth.repository.EmailVerificationRepository;
import com.recruitmentplatform.auth.repository.UserCredentialRepository;
import com.recruitmentplatform.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class EmailVerificationService {

    private final EmailVerificationRepository emailVerificationRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final AuthEventProducer eventProducer;

    @Transactional
    public String createVerificationToken(UUID userId) {
        String rawToken = UUID.randomUUID().toString();
        String tokenHash = JwtTokenProvider.hashToken(rawToken);

        EmailVerification verification = EmailVerification.builder()
                .userId(userId)
                .tokenHash(tokenHash)
                .expiresAt(LocalDateTime.now().plusHours(24))
                .build();

        emailVerificationRepository.save(verification);
        return rawToken;
    }

    @Transactional
    public MessageResponse verifyEmail(String rawToken) {
        String tokenHash = JwtTokenProvider.hashToken(rawToken);

        EmailVerification verification = emailVerificationRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Invalid or expired verification token", "INVALID_TOKEN"));

        if (verification.isUsed() || verification.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Verification token has expired or already been used", "TOKEN_EXPIRED");
        }

        UserCredential credential = userCredentialRepository.findById(verification.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found"));

        verification.setUsed(true);
        emailVerificationRepository.save(verification);

        credential.setEmailVerified(true);
        if (credential.getStatus() == AccountStatus.PENDING_VERIFICATION) {
            credential.setStatus(AccountStatus.ACTIVE);
        }
        userCredentialRepository.save(credential);

        return new MessageResponse("Email verified successfully. You can now login.");
    }

    @Transactional
    public MessageResponse resendVerification(String email, String correlationId) {
        UserCredential credential = userCredentialRepository.findByEmailIgnoreCase(email)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + email));

        if (credential.isEmailVerified()) {
            throw new BadRequestException("Email is already verified", "ALREADY_VERIFIED");
        }

        String rawToken = createVerificationToken(credential.getId());

        eventProducer.publishEmailVerificationRequested(
                EmailVerificationRequestedPayload.builder()
                        .userId(credential.getId())
                        .email(credential.getEmail())
                        .verificationToken(rawToken)
                        .expiresAt(LocalDateTime.now().plusHours(24))
                        .build(),
                correlationId
        );

        return new MessageResponse("Verification email resent. Please check your inbox.");
    }
}
