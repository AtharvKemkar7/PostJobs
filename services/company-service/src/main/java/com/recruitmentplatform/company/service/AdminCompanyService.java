package com.recruitmentplatform.company.service;

import com.recruitmentplatform.company.dto.CompanyDtos;
import com.recruitmentplatform.company.entity.Company;
import com.recruitmentplatform.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AdminCompanyService {

    private final CompanyRepository companyRepository;

    public Page<CompanyDtos.Response> listCompanies(Pageable pageable) {
        return companyRepository.findByDeletedFalse(pageable)
                .map(this::toResponse);
    }

    @Transactional
    public CompanyDtos.Response updateCompanyStatus(UUID id, CompanyDtos.UpdateStatusRequest request) {
        Company company = companyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + id));

        company.setStatus(request.getStatus());
        return toResponse(companyRepository.save(company));
    }

    private CompanyDtos.Response toResponse(Company c) {
        return CompanyDtos.Response.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .industry(c.getIndustry())
                .website(c.getWebsite())
                .headquartersCity(c.getHeadquartersCity())
                .headquartersState(c.getHeadquartersState())
                .headquartersCountry(c.getHeadquartersCountry())
                .employeeCount(c.getEmployeeCount())
                .foundedYear(c.getFoundedYear())
                .status(c.getStatus())
                .createdByUserId(c.getCreatedByUserId())
                .createdAt(c.getCreatedAt())
                .updatedAt(c.getUpdatedAt())
                .build();
    }
}
