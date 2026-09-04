package com.recruitmentplatform.job.controller;

import com.recruitmentplatform.job.dto.JobDtos;
import com.recruitmentplatform.job.service.AdminJobService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequestMapping("/api/v1/admin/jobs")
@RequiredArgsConstructor
@PreAuthorize("hasRole('ADMIN')")
public class AdminJobController {

    private final AdminJobService adminJobService;

    @GetMapping
    public ResponseEntity<Page<JobDtos.Response>> listAllJobs(
            @PageableDefault(size = 20) Pageable pageable) {
        return ResponseEntity.ok(adminJobService.listAllJobs(pageable));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteJob(@PathVariable UUID id) {
        adminJobService.deleteJob(id);
        return ResponseEntity.noContent().build();
    }
}
