package com.recruitmentplatform.auth.service;

import com.recruitmentplatform.auth.dto.request.UpdateUserStatusRequest;
import com.recruitmentplatform.auth.dto.response.MessageResponse;
import com.recruitmentplatform.auth.dto.response.UserSummaryResponse;
import com.recruitmentplatform.auth.entity.UserCredential;
import com.recruitmentplatform.auth.exception.ResourceNotFoundException;
import com.recruitmentplatform.auth.repository.RefreshTokenRepository;
import com.recruitmentplatform.auth.repository.UserCredentialRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminUserService {

    private final UserCredentialRepository userCredentialRepository;
    private final RefreshTokenRepository refreshTokenRepository;

    public Page<UserSummaryResponse> listUsers(Pageable pageable) {
        return userCredentialRepository.findAll(pageable)
                .map(this::toSummary);
    }

    @Transactional
    public MessageResponse updateUserStatus(UUID userId, UpdateUserStatusRequest request) {
        UserCredential credential = userCredentialRepository.findById(userId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + userId));

        credential.setStatus(request.getStatus());
        userCredentialRepository.save(credential);

        // If suspending or deactivating, revoke tokens
        if (request.getStatus() != com.recruitmentplatform.auth.enums.AccountStatus.ACTIVE) {
            refreshTokenRepository.revokeAllByUserId(userId);
        }

        return new MessageResponse("User status updated to " + request.getStatus().name());
    }

    private UserSummaryResponse toSummary(UserCredential c) {
        return UserSummaryResponse.builder()
                .userId(c.getId())
                .email(c.getEmail())
                .role(c.getRole())
                .status(c.getStatus())
                .emailVerified(c.isEmailVerified())
                .createdAt(c.getCreatedAt())
                .build();
    }
}
