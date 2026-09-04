package com.recruitmentplatform.company.controller;

import com.recruitmentplatform.company.dto.CompanyDtos;
import com.recruitmentplatform.company.service.AdminCompanyService;
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
@RequestMapping("/api/v1/admin/companies")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminCompanyController {

    private final AdminCompanyService adminCompanyService;

    @GetMapping
    public ResponseEntity<Page<CompanyDtos.Response>> listCompanies(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminCompanyService.listCompanies(pageable));
    }

    @PutMapping("/{id}/status")
    public ResponseEntity<CompanyDtos.Response> updateCompanyStatus(
            @PathVariable UUID id,
            @Valid @RequestBody CompanyDtos.UpdateStatusRequest request) {
        return ResponseEntity.ok(adminCompanyService.updateCompanyStatus(id, request));
    }
}
