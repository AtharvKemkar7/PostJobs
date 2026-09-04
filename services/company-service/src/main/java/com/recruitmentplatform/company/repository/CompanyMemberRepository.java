package com.recruitmentplatform.company.repository;

import com.recruitmentplatform.company.entity.CompanyMember;
import com.recruitmentplatform.company.enums.CompanyMemberRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyMemberRepository extends JpaRepository<CompanyMember, UUID> {
    List<CompanyMember> findByCompanyId(UUID companyId);
    Optional<CompanyMember> findByCompanyIdAndUserId(UUID companyId, UUID userId);
    Optional<CompanyMember> findByUserId(UUID userId);
    boolean existsByCompanyIdAndUserId(UUID companyId, UUID userId);
    long countByCompanyIdAndRole(UUID companyId, CompanyMemberRole role);
}
