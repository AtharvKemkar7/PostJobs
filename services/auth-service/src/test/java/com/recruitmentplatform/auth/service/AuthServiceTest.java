package com.recruitmentplatform.auth.service;

import com.recruitmentplatform.auth.dto.request.LoginRequest;
import com.recruitmentplatform.auth.dto.request.RegisterRequest;
import com.recruitmentplatform.auth.dto.response.AuthResponse;
import com.recruitmentplatform.auth.dto.response.MessageResponse;
import com.recruitmentplatform.auth.entity.UserCredential;
import com.recruitmentplatform.auth.enums.AccountRole;
import com.recruitmentplatform.auth.enums.AccountStatus;
import com.recruitmentplatform.auth.event.AuthEventProducer;
import com.recruitmentplatform.auth.exception.BadRequestException;
import com.recruitmentplatform.auth.exception.ConflictException;
import com.recruitmentplatform.auth.exception.ForbiddenException;
import com.recruitmentplatform.auth.exception.UnauthorizedException;
import com.recruitmentplatform.auth.repository.RefreshTokenRepository;
import com.recruitmentplatform.auth.repository.UserCredentialRepository;
import com.recruitmentplatform.auth.security.JwtTokenProvider;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AuthServiceTest {

    @Mock
    private UserCredentialRepository userCredentialRepository;

    @Mock
    private RefreshTokenRepository refreshTokenRepository;

    @Mock
    private EmailVerificationService emailVerificationService;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtTokenProvider jwtTokenProvider;

    @Mock
    private AuthEventProducer eventProducer;

    @InjectMocks
    private AuthService authService;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        registerRequest = RegisterRequest.builder()
                .email("test@example.com")
                .password("Password@123")
                .confirmPassword("Password@123")
                .role(AccountRole.CANDIDATE)
                .firstName("John")
                .lastName("Doe")
                .build();
    }

    @Test
    void testRegisterSuccess() {
        when(userCredentialRepository.existsByEmailIgnoreCase(any())).thenReturn(false);
        when(passwordEncoder.encode(any())).thenReturn("encodedPassword");
        when(userCredentialRepository.save(any())).thenAnswer(invocation -> {
            UserCredential u = invocation.getArgument(0);
            u.setId(UUID.randomUUID());
            return u;
        });
        when(emailVerificationService.createVerificationToken(any())).thenReturn("raw-token");

        MessageResponse response = authService.register(registerRequest, "test-correlation-id");

        assertNotNull(response);
        verify(userCredentialRepository, times(1)).save(any(UserCredential.class));
        verify(eventProducer, times(1)).publishUserRegistered(any(), any());
    }

    @Test
    void testRegisterPasswordMismatch() {
        registerRequest.setConfirmPassword("DifferentPassword@123");

        assertThrows(BadRequestException.class, () -> authService.register(registerRequest, "corr-id"));
        verify(userCredentialRepository, never()).save(any());
    }

    @Test
    void testRegisterEmailAlreadyExists() {
        when(userCredentialRepository.existsByEmailIgnoreCase(any())).thenReturn(true);

        assertThrows(ConflictException.class, () -> authService.register(registerRequest, "corr-id"));
        verify(userCredentialRepository, never()).save(any());
    }

    @Test
    void testLoginSuccess() {
        UUID userId = UUID.randomUUID();
        UserCredential credential = UserCredential.builder()
                .id(userId)
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .role(AccountRole.CANDIDATE)
                .status(AccountStatus.ACTIVE)
                .emailVerified(true)
                .build();

        when(userCredentialRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("Password@123", "encodedPassword")).thenReturn(true);
        when(jwtTokenProvider.generateAccessToken(any(), any(), any())).thenReturn("access-token-jwt");
        when(jwtTokenProvider.getAccessTokenExpiryMillis()).thenReturn(900000L);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("Password@123")
                .build();

        AuthResponse response = authService.login(loginRequest, "Mozilla/5.0");

        assertNotNull(response);
        assertEquals("access-token-jwt", response.getAccessToken());
        assertEquals(userId, response.getUserId());
        verify(refreshTokenRepository, times(1)).save(any());
    }

    @Test
    void testLoginUnverifiedEmail() {
        UserCredential credential = UserCredential.builder()
                .id(UUID.randomUUID())
                .email("test@example.com")
                .passwordHash("encodedPassword")
                .role(AccountRole.CANDIDATE)
                .status(AccountStatus.PENDING_VERIFICATION)
                .emailVerified(false)
                .build();

        when(userCredentialRepository.findByEmailIgnoreCase("test@example.com")).thenReturn(Optional.of(credential));
        when(passwordEncoder.matches("Password@123", "encodedPassword")).thenReturn(true);

        LoginRequest loginRequest = LoginRequest.builder()
                .email("test@example.com")
                .password("Password@123")
                .build();

        assertThrows(ForbiddenException.class, () -> authService.login(loginRequest, "device"));
    }

    @Test
    void testLoginInvalidCredentials() {
        when(userCredentialRepository.findByEmailIgnoreCase("unknown@example.com")).thenReturn(Optional.empty());

        LoginRequest loginRequest = LoginRequest.builder()
                .email("unknown@example.com")
                .password("Password@123")
                .build();

        assertThrows(UnauthorizedException.class, () -> authService.login(loginRequest, "device"));
    }
}
