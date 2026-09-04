package com.recruitmentplatform.auth.security;

import com.recruitmentplatform.auth.enums.AccountRole;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

class JwtTokenProviderTest {

    private JwtTokenProvider jwtTokenProvider;
    private final String secret = "test-secret-key-that-is-at-least-32-bytes-long-for-hmac-sha-256";

    @BeforeEach
    void setUp() {
        jwtTokenProvider = new JwtTokenProvider(secret, 900000, 604800000);
    }

    @Test
    void testGenerateAndValidateToken() {
        UUID userId = UUID.randomUUID();
        String email = "candidate@example.com";
        AccountRole role = AccountRole.CANDIDATE;

        String token = jwtTokenProvider.generateAccessToken(userId, email, role);
        assertNotNull(token);
        assertTrue(jwtTokenProvider.validateToken(token));

        assertEquals(userId, jwtTokenProvider.getUserIdFromToken(token));
        assertEquals(email, jwtTokenProvider.getEmailFromToken(token));
        assertEquals(role.name(), jwtTokenProvider.getRoleFromToken(token));
    }

    @Test
    void testHashToken() {
        String rawToken = "sample-token-12345";
        String hash1 = JwtTokenProvider.hashToken(rawToken);
        String hash2 = JwtTokenProvider.hashToken(rawToken);

        assertNotNull(hash1);
        assertEquals(hash1, hash2);
        assertNotEquals(rawToken, hash1);
    }
}
