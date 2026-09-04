package com.recruitmentplatform.company.service;

import com.recruitmentplatform.company.dto.CompanyMemberDtos;
import com.recruitmentplatform.company.entity.CompanyMember;
import com.recruitmentplatform.company.enums.CompanyMemberRole;
import com.recruitmentplatform.company.repository.CompanyMemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class CompanyMemberService {

    private final CompanyMemberRepository companyMemberRepository;

    public List<CompanyMemberDtos.Response> listMembers(UUID userId, UUID companyId) {
        validateMembership(userId, companyId);
        return companyMemberRepository.findByCompanyId(companyId).stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    @Transactional
    public CompanyMemberDtos.Response addMember(UUID currentUserId, UUID companyId, CompanyMemberDtos.AddRequest request) {
        validateOwnerOrAdmin(currentUserId, companyId);

        if (companyMemberRepository.existsByCompanyIdAndUserId(companyId, request.getUserId())) {
            throw new IllegalArgumentException("User is already a member of this company");
        }

        CompanyMember member = CompanyMember.builder()
                .companyId(companyId)
                .userId(request.getUserId())
                .role(request.getRole())
                .build();

        return toResponse(companyMemberRepository.save(member));
    }

    @Transactional
    public CompanyMemberDtos.Response updateMemberRole(UUID currentUserId, UUID companyId, UUID memberId, CompanyMemberDtos.UpdateRoleRequest request) {
        validateOwner(currentUserId, companyId);

        CompanyMember member = companyMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (!member.getCompanyId().equals(companyId)) {
            throw new IllegalArgumentException("Member does not belong to this company");
        }

        member.setRole(request.getRole());
        return toResponse(companyMemberRepository.save(member));
    }

    @Transactional
    public void removeMember(UUID currentUserId, UUID companyId, UUID memberId) {
        validateOwner(currentUserId, companyId);

        CompanyMember member = companyMemberRepository.findById(memberId)
                .orElseThrow(() -> new IllegalArgumentException("Member not found"));

        if (member.getRole() == CompanyMemberRole.OWNER) {
            long ownerCount = companyMemberRepository.countByCompanyIdAndRole(companyId, CompanyMemberRole.OWNER);
            if (ownerCount <= 1) {
                throw new IllegalStateException("Cannot remove the only OWNER of the company");
            }
        }

        companyMemberRepository.delete(member);
    }

    private void validateMembership(UUID userId, UUID companyId) {
        if (!companyMemberRepository.existsByCompanyIdAndUserId(companyId, userId)) {
            throw new IllegalStateException("You are not a member of this company");
        }
    }

    private void validateOwnerOrAdmin(UUID userId, UUID companyId) {
        CompanyMember member = companyMemberRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new IllegalStateException("You are not a member of this company"));
        if (member.getRole() != CompanyMemberRole.OWNER && member.getRole() != CompanyMemberRole.ADMIN) {
            throw new IllegalStateException("Only OWNER or ADMIN can perform this action");
        }
    }

    private void validateOwner(UUID userId, UUID companyId) {
        CompanyMember member = companyMemberRepository.findByCompanyIdAndUserId(companyId, userId)
                .orElseThrow(() -> new IllegalStateException("You are not a member of this company"));
        if (member.getRole() != CompanyMemberRole.OWNER) {
            throw new IllegalStateException("Only company OWNER can perform this action");
        }
    }

    private CompanyMemberDtos.Response toResponse(CompanyMember m) {
        return CompanyMemberDtos.Response.builder()
                .id(m.getId())
                .companyId(m.getCompanyId())
                .userId(m.getUserId())
                .role(m.getRole())
                .joinedAt(m.getJoinedAt())
                .build();
    }
}
