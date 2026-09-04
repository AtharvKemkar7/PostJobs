package com.recruitmentplatform.auth.service;

import com.recruitmentplatform.auth.dto.request.*;
import com.recruitmentplatform.auth.dto.response.AuthResponse;
import com.recruitmentplatform.auth.dto.response.MessageResponse;
import com.recruitmentplatform.auth.dto.response.TokenRefreshResponse;
import com.recruitmentplatform.auth.entity.RefreshToken;
import com.recruitmentplatform.auth.entity.UserCredential;
import com.recruitmentplatform.auth.enums.AccountRole;
import com.recruitmentplatform.auth.enums.AccountStatus;
import com.recruitmentplatform.auth.event.AuthEventProducer;
import com.recruitmentplatform.auth.event.EmailVerificationRequestedPayload;
import com.recruitmentplatform.auth.event.UserRegisteredPayload;
import com.recruitmentplatform.auth.exception.BadRequestException;
import com.recruitmentplatform.auth.exception.ConflictException;
import com.recruitmentplatform.auth.exception.ForbiddenException;
import com.recruitmentplatform.auth.exception.UnauthorizedException;
import com.recruitmentplatform.auth.repository.RefreshTokenRepository;
import com.recruitmentplatform.auth.repository.UserCredentialRepository;
import com.recruitmentplatform.auth.security.JwtTokenProvider;
import com.recruitmentplatform.auth.security.UserPrincipal;
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
public class AuthService {

    private final UserCredentialRepository userCredentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;
    private final EmailVerificationService emailVerificationService;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final AuthEventProducer eventProducer;

    @Transactional
    public MessageResponse register(RegisterRequest request, String correlationId) {
        if (!request.getPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match", "PASSWORD_MISMATCH");
        }

        if (userCredentialRepository.existsByEmailIgnoreCase(request.getEmail())) {
            throw new ConflictException("Email is already registered", "EMAIL_ALREADY_EXISTS");
        }

        if (request.getRole() == AccountRole.ADMIN) {
            throw new BadRequestException("Admin accounts cannot be registered publicly", "INVALID_ROLE");
        }

        UserCredential credential = UserCredential.builder()
                .email(request.getEmail().toLowerCase().trim())
                .passwordHash(passwordEncoder.encode(request.getPassword()))
                .role(request.getRole())
                .status(AccountStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .build();

        credential = userCredentialRepository.save(credential);
        log.info("Registered user with ID: {}", credential.getId());

        // Create email verification token
        String rawVerificationToken = emailVerificationService.createVerificationToken(credential.getId());

        // Publish events to Kafka
        eventProducer.publishUserRegistered(
                UserRegisteredPayload.builder()
                        .userId(credential.getId())
                        .email(credential.getEmail())
                        .role(credential.getRole())
                        .firstName(request.getFirstName())
                        .lastName(request.getLastName())
                        .build(),
                correlationId
        );

        eventProducer.publishEmailVerificationRequested(
                EmailVerificationRequestedPayload.builder()
                        .userId(credential.getId())
                        .email(credential.getEmail())
                        .verificationToken(rawVerificationToken)
                        .expiresAt(LocalDateTime.now().plusHours(24))
                        .build(),
                correlationId
        );

        return new MessageResponse("Registration successful. Please verify your email address.");
    }

    @Transactional
    public AuthResponse login(LoginRequest request, String deviceInfo) {
        UserCredential credential = userCredentialRepository.findByEmailIgnoreCase(request.getEmail().toLowerCase().trim())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password", "INVALID_CREDENTIALS"));

        if (!passwordEncoder.matches(request.getPassword(), credential.getPasswordHash())) {
            throw new UnauthorizedException("Invalid email or password", "INVALID_CREDENTIALS");
        }

        if (credential.getStatus() == AccountStatus.SUSPENDED) {
            throw new ForbiddenException("Account has been suspended. Please contact support.", "ACCOUNT_SUSPENDED");
        }

        if (credential.getStatus() == AccountStatus.DEACTIVATED) {
            throw new ForbiddenException("Account has been deactivated.", "ACCOUNT_DEACTIVATED");
        }

        if (!credential.isEmailVerified()) {
            throw new ForbiddenException("Email address has not been verified.", "EMAIL_NOT_VERIFIED");
        }

        String accessToken = jwtTokenProvider.generateAccessToken(
                credential.getId(),
                credential.getEmail(),
                credential.getRole()
        );

        String rawRefreshToken = UUID.randomUUID().toString();
        String refreshTokenHash = JwtTokenProvider.hashToken(rawRefreshToken);

        RefreshToken refreshToken = RefreshToken.builder()
                .userId(credential.getId())
                .tokenHash(refreshTokenHash)
                .deviceInfo(deviceInfo)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepository.save(refreshToken);

        return AuthResponse.builder()
                .accessToken(accessToken)
                .refreshToken(rawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiryMillis() / 1000)
                .userId(credential.getId())
                .email(credential.getEmail())
                .role(credential.getRole())
                .build();
    }

    @Transactional
    public TokenRefreshResponse refreshToken(RefreshTokenRequest request, String deviceInfo) {
        String rawToken = request.getRefreshToken();
        String tokenHash = JwtTokenProvider.hashToken(rawToken);

        RefreshToken existingToken = refreshTokenRepository.findByTokenHash(tokenHash)
                .orElseThrow(() -> new UnauthorizedException("Invalid refresh token", "INVALID_REFRESH_TOKEN"));

        if (existingToken.isRevoked() || existingToken.getExpiresAt().isBefore(LocalDateTime.now())) {
            throw new UnauthorizedException("Refresh token is expired or revoked", "REFRESH_TOKEN_EXPIRED");
        }

        UserCredential credential = userCredentialRepository.findById(existingToken.getUserId())
                .orElseThrow(() -> new UnauthorizedException("User not found", "USER_NOT_FOUND"));

        if (credential.getStatus() != AccountStatus.ACTIVE) {
            throw new ForbiddenException("Account is not active", "ACCOUNT_INACTIVE");
        }

        // Token rotation: revoke current token
        existingToken.setRevoked(true);
        refreshTokenRepository.save(existingToken);

        // Generate new access & refresh tokens
        String newAccessToken = jwtTokenProvider.generateAccessToken(
                credential.getId(),
                credential.getEmail(),
                credential.getRole()
        );

        String newRawRefreshToken = UUID.randomUUID().toString();
        String newRefreshTokenHash = JwtTokenProvider.hashToken(newRawRefreshToken);

        RefreshToken newRefreshToken = RefreshToken.builder()
                .userId(credential.getId())
                .tokenHash(newRefreshTokenHash)
                .deviceInfo(deviceInfo)
                .expiresAt(LocalDateTime.now().plusDays(7))
                .build();

        refreshTokenRepository.save(newRefreshToken);

        return TokenRefreshResponse.builder()
                .accessToken(newAccessToken)
                .refreshToken(newRawRefreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenExpiryMillis() / 1000)
                .build();
    }

    @Transactional
    public MessageResponse logout(RefreshTokenRequest request) {
        String rawToken = request.getRefreshToken();
        String tokenHash = JwtTokenProvider.hashToken(rawToken);

        refreshTokenRepository.findByTokenHash(tokenHash).ifPresent(token -> {
            token.setRevoked(true);
            refreshTokenRepository.save(token);
        });

        return new MessageResponse("Logged out successfully");
    }

    @Transactional
    public MessageResponse changePassword(UserPrincipal principal, ChangePasswordRequest request) {
        if (!request.getNewPassword().equals(request.getConfirmPassword())) {
            throw new BadRequestException("Passwords do not match", "PASSWORD_MISMATCH");
        }

        UserCredential credential = userCredentialRepository.findById(principal.getId())
                .orElseThrow(() -> new UnauthorizedException("User not found", "USER_NOT_FOUND"));

        if (!passwordEncoder.matches(request.getCurrentPassword(), credential.getPasswordHash())) {
            throw new BadRequestException("Current password is incorrect", "INCORRECT_PASSWORD");
        }

        credential.setPasswordHash(passwordEncoder.encode(request.getNewPassword()));
        userCredentialRepository.save(credential);

        // Revoke all refresh tokens for security
        refreshTokenRepository.revokeAllByUserId(credential.getId());

        return new MessageResponse("Password changed successfully. Please login again.");
    }
}
