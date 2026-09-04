package com.recruitmentplatform.auth.controller;

import com.recruitmentplatform.auth.dto.request.*;
import com.recruitmentplatform.auth.dto.response.AuthResponse;
import com.recruitmentplatform.auth.dto.response.MessageResponse;
import com.recruitmentplatform.auth.dto.response.TokenRefreshResponse;
import com.recruitmentplatform.auth.security.UserPrincipal;
import com.recruitmentplatform.auth.service.AuthService;
import com.recruitmentplatform.auth.service.EmailVerificationService;
import com.recruitmentplatform.auth.service.PasswordResetService;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;
    private final EmailVerificationService emailVerificationService;
    private final PasswordResetService passwordResetService;

    @PostMapping("/register")
    public ResponseEntity<MessageResponse> register(
            @Valid @RequestBody RegisterRequest request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        MessageResponse response = authService.register(request, correlationId);
        return new ResponseEntity<>(response, HttpStatus.CREATED);
    }

    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(
            @Valid @RequestBody LoginRequest request,
            HttpServletRequest httpRequest) {
        String deviceInfo = httpRequest.getHeader("User-Agent");
        AuthResponse response = authService.login(request, deviceInfo);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/refresh")
    public ResponseEntity<TokenRefreshResponse> refreshToken(
            @Valid @RequestBody RefreshTokenRequest request,
            HttpServletRequest httpRequest) {
        String deviceInfo = httpRequest.getHeader("User-Agent");
        TokenRefreshResponse response = authService.refreshToken(request, deviceInfo);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/logout")
    public ResponseEntity<MessageResponse> logout(@Valid @RequestBody RefreshTokenRequest request) {
        MessageResponse response = authService.logout(request);
        return ResponseEntity.ok(response);
    }

    @GetMapping("/verify-email")
    public ResponseEntity<MessageResponse> verifyEmail(@RequestParam("token") String token) {
        MessageResponse response = emailVerificationService.verifyEmail(token);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/resend-verification")
    public ResponseEntity<MessageResponse> resendVerification(
            @Valid @RequestBody ForgotPasswordRequest request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        MessageResponse response = emailVerificationService.resendVerification(request.getEmail(), correlationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/forgot-password")
    public ResponseEntity<MessageResponse> forgotPassword(
            @Valid @RequestBody ForgotPasswordRequest request,
            @RequestHeader(value = "X-Correlation-ID", required = false) String correlationId) {
        MessageResponse response = passwordResetService.forgotPassword(request, correlationId);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/reset-password")
    public ResponseEntity<MessageResponse> resetPassword(@Valid @RequestBody ResetPasswordRequest request) {
        MessageResponse response = passwordResetService.resetPassword(request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/change-password")
    public ResponseEntity<MessageResponse> changePassword(
            @AuthenticationPrincipal UserPrincipal principal,
            @Valid @RequestBody ChangePasswordRequest request) {
        MessageResponse response = authService.changePassword(principal, request);
        return ResponseEntity.ok(response);
    }
}
