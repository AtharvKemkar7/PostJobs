package com.recruitmentplatform.company.controller;

import com.recruitmentplatform.company.dto.CompanyDtos;
import com.recruitmentplatform.company.dto.CompanyMemberDtos;
import com.recruitmentplatform.company.service.CompanyMemberService;
import com.recruitmentplatform.company.service.CompanyService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/api/v1/companies")
@RequiredArgsConstructor
public class CompanyController {

    private final CompanyService companyService;
    private final CompanyMemberService companyMemberService;

    @PostMapping
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<CompanyDtos.Response> createCompany(
            @AuthenticationPrincipal UUID userId,
            @Valid @RequestBody CompanyDtos.CreateRequest request) {
        return new ResponseEntity<>(companyService.createCompany(userId, request), HttpStatus.CREATED);
    }

    @GetMapping("/{id}")
    public ResponseEntity<CompanyDtos.Response> getCompany(@PathVariable UUID id) {
        return ResponseEntity.ok(companyService.getCompany(id));
    }

    @GetMapping("/my")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<CompanyDtos.Response> getMyCompany(@AuthenticationPrincipal UUID userId) {
        return ResponseEntity.ok(companyService.getRecruiterCompany(userId));
    }

    @PutMapping("/{id}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<CompanyDtos.Response> updateCompany(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody CompanyDtos.UpdateRequest request) {
        return ResponseEntity.ok(companyService.updateCompany(userId, id, request));
    }

    // --- Members ---
    @GetMapping("/{id}/members")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<List<CompanyMemberDtos.Response>> listMembers(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id) {
        return ResponseEntity.ok(companyMemberService.listMembers(userId, id));
    }

    @PostMapping("/{id}/members")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<CompanyMemberDtos.Response> addMember(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @Valid @RequestBody CompanyMemberDtos.AddRequest request) {
        return new ResponseEntity<>(companyMemberService.addMember(userId, id, request), HttpStatus.CREATED);
    }

    @PutMapping("/{id}/members/{memberId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<CompanyMemberDtos.Response> updateMemberRole(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @PathVariable UUID memberId,
            @Valid @RequestBody CompanyMemberDtos.UpdateRoleRequest request) {
        return ResponseEntity.ok(companyMemberService.updateMemberRole(userId, id, memberId, request));
    }

    @DeleteMapping("/{id}/members/{memberId}")
    @PreAuthorize("hasRole('RECRUITER')")
    public ResponseEntity<Void> removeMember(
            @AuthenticationPrincipal UUID userId,
            @PathVariable UUID id,
            @PathVariable UUID memberId) {
        companyMemberService.removeMember(userId, id, memberId);
        return ResponseEntity.noContent().build();
    }
}
