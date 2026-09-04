package com.recruitmentplatform.company.service;

import com.recruitmentplatform.company.dto.CompanyDtos;
import com.recruitmentplatform.company.entity.Company;
import com.recruitmentplatform.company.entity.CompanyMember;
import com.recruitmentplatform.company.enums.CompanyMemberRole;
import com.recruitmentplatform.company.enums.CompanyStatus;
import com.recruitmentplatform.company.repository.CompanyMemberRepository;
import com.recruitmentplatform.company.repository.CompanyRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;

@Service
@RequiredArgsConstructor
public class CompanyService {

    private final CompanyRepository companyRepository;
    private final CompanyMemberRepository companyMemberRepository;

    @Transactional
    public CompanyDtos.Response createCompany(UUID userId, CompanyDtos.CreateRequest request) {
        if (companyRepository.existsByNameIgnoreCaseAndDeletedFalse(request.getName())) {
            throw new IllegalArgumentException("Company with name '" + request.getName() + "' already exists");
        }

        Company company = Company.builder()
                .name(request.getName().trim())
                .description(request.getDescription())
                .industry(request.getIndustry())
                .website(request.getWebsite())
                .headquartersCity(request.getHeadquartersCity())
                .headquartersState(request.getHeadquartersState())
                .headquartersCountry(request.getHeadquartersCountry())
                .employeeCount(request.getEmployeeCount())
                .foundedYear(request.getFoundedYear())
                .status(CompanyStatus.ACTIVE)
                .createdByUserId(userId)
                .build();

        company = companyRepository.save(company);

        // Creator automatically becomes OWNER
        CompanyMember owner = CompanyMember.builder()
                .companyId(company.getId())
                .userId(userId)
                .role(CompanyMemberRole.OWNER)
                .build();
        companyMemberRepository.save(owner);

        return toResponse(company);
    }

    public CompanyDtos.Response getCompany(UUID id) {
        Company company = companyRepository.findByIdAndDeletedFalse(id)
                .orElseThrow(() -> new IllegalArgumentException("Company not found with id: " + id));
        return toResponse(company);
    }

    public CompanyDtos.Response getRecruiterCompany(UUID userId) {
        CompanyMember member = companyMemberRepository.findByUserId(userId)
                .orElseThrow(() -> new IllegalArgumentException("Recruiter is not associated with any company"));
        return getCompany(member.getCompanyId());
    }

    @Transactional
    public CompanyDtos.Response updateCompany(UUID userId, UUID companyId, CompanyDtos.UpdateRequest request) {
        CompanyMember member = companyMemberRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new IllegalStateException("You are not a member of this company"));

        if (member.getRole() != CompanyMemberRole.OWNER && member.getRole() != CompanyMemberRole.ADMIN) {
            throw new IllegalStateException("Only OWNER or ADMIN can modify company details");
        }

        Company company = companyRepository.findByIdAndDeletedFalse(companyId)
                .orElseThrow(() -> new IllegalArgumentException("Company not found"));

        if (request.getName() != null) company.setName(request.getName().trim());
        if (request.getDescription() != null) company.setDescription(request.getDescription());
        if (request.getIndustry() != null) company.setIndustry(request.getIndustry());
        if (request.getWebsite() != null) company.setWebsite(request.getWebsite());
        if (request.getHeadquartersCity() != null) company.setHeadquartersCity(request.getHeadquartersCity());
        if (request.getHeadquartersState() != null) company.setHeadquartersState(request.getHeadquartersState());
        if (request.getHeadquartersCountry() != null) company.setHeadquartersCountry(request.getHeadquartersCountry());
        if (request.getEmployeeCount() != null) company.setEmployeeCount(request.getEmployeeCount());
        if (request.getFoundedYear() != null) company.setFoundedYear(request.getFoundedYear());

        return toResponse(companyRepository.save(company));
    }

    private CompanyDtos.Response toResponse(Company c) {
        return CompanyDtos.Response.builder()
                .id(c.getId())
                .name(c.getName())
                .description(c.getDescription())
                .industry(c.getIndustry())
                .website(c.getWebsite())
                .logoUrl(c.getLogoObjectKey())
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
