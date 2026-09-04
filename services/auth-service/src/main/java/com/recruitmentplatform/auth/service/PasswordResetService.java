package com.recruitmentplatform.auth.service;

import com.recruitmentplatform.auth.dto.request.ForgotPasswordRequest;
import com.recruitmentplatform.auth.dto.request.ResetPasswordRequest;
import com.recruitmentplatform.auth.dto.response.MessageResponse;
import com.recruitmentplatform.auth.entity.PasswordReset;
import com.recruitmentplatform.auth.entity.UserCredential;
import com.recruitmentplatform.auth.event.AuthEventProducer;
import com.recruitmentplatform.auth.event.PasswordResetRequestedPayload;
import com.recruitmentplatform.auth.exception.BadRequestException;
import com.recruitmentplatform.auth.repository.PasswordResetRepository;
import com.recruitmentplatform.auth.repository.RefreshTokenRepository;
import com.recruitmentplatform.auth.repository.UserCredentialRepository;
import com.recruitmentplatform.auth.security.JwtTokenProvider;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

    private final PasswordResetRepository passwordResetRepository;
    private final UserCredentialRepository userCredentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final PasswordEncoder passwordEncoder;
    private final AuthEventProducer eventProducer;

    @Transactional
    public MessageResponse forgotPassword(ForgotPasswordRequest request, String correlationId) {
        userCredentialRepository.findByEmailIgnoreCase(request.getEmail().toLowerCase().trim()).ifPresent(credential -> {
            String rawToken = UUID.randomUUID().toString();
            String tokenHash = JwtTokenProvider.hashToken(rawToken);

            PasswordReset reset = PasswordReset.builder()
                    .userId(credential.getId())
                    .tokenHash(tokenHash)
                    .expiresAt(LocalDateTime.now().plusHours(1))
                    .build();

            passwordResetRepository.save(reset);

            eventProducer.publishPasswordResetRequested(
                    PasswordResetRequestedPayload.builder()
                            .userId(credential.getId())
                            .email(credential.getEmail())
                            .resetToken(rawToken)
                            .expiresAt(LocalDateTime.now().plusHours(1))
                            .build(),
                    correlationId
            );
        });

        // Always return 200 to prevent email enumeration
        return new MessageResponse("If your email is registered, you will receive password reset instructions.");
    }

    @Transactional
    public MessageResponse resetPassword(ResetPasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match", "PASSWORD_MISMATCH");
        }

        String tokenHash = JwtTokenProvider.hashToken(request.getToken());

        PasswordReset reset = passwordResetRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new BadRequestException("Invalid or expired password reset token", "INVALID_TOKEN"));

        if (reset.isUsed() || reset.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new BadRequestException("Password reset token has expired or already been used", "TOKEN_EXPIRED");
        }

        UserCredential credential = userCredentialRepository.findById(reset.getUserId())
                .orElseThrow(() -> new BadRequestException("User not found", "USER_NOT_FOUND"));

        reset.setUsed(true);
        passwordResetRepository.save(reset);

        credential.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userCredentialRepository.save(credential);

        // Revoke all existing refresh tokens
        refreshTokenRepository.revokeAllByUserId(credential.getId());

        return new MessageResponse("Password has been reset successfully. You can now login with your new password.");
    }
}
