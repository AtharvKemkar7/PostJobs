package com.recruitmentplatform.auth.controller;

import com.recruitmentplatform.auth.dto.request.UpdateUserStatusRequest;
import com.recruitmentplatform.auth.dto.response.MessageResponse;
import com.recruitmentplatform.auth.dto.response.UserSummaryResponse;
import com.recruitmentplatform.auth.service.AdminUserService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/users")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminUserController {

    private final AdminUserService adminUserService;

    @GetMapping
    public ResponseEntity<Page<UserSummaryResponse>> listUsers(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminUserService.listUsers(pageable));
    }

    @PutMapping("/{userId}/status")
    public ResponseEntity<MessageResponse> updateUserStatus(
            @PathVariable UUID userId,
            @Valid @RequestBody UpdateUserStatusRequest request) {
        return ResponseEntity.ok(adminUserService.updateUserStatus(userId, request));
    }
}
