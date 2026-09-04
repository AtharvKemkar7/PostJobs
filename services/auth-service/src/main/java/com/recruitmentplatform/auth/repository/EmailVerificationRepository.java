package com.recruitmentplatform.auth.repository;

import com.recruitmentplatform.auth.entity.EmailVerification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface EmailVerificationRepository extends JpaRepository<EmailVerification, UUID> {
    Optional<EmailVerification> findByTokenHash(String tokenHash);
    Optional<EmailVerification> findTopByUserIdOrderByCreatedAtDesc(UUID userId);
}
