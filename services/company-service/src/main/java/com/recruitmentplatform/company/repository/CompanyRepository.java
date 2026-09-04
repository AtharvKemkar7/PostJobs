package com.recruitmentplatform.company.repository;

import com.recruitmentplatform.company.entity.Company;
import com.recruitmentplatform.company.enums.CompanyStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface CompanyRepository extends JpaRepository<Company, UUID> {
    Optional<Company> findByIdAndDeletedFalse(UUID id);
    Page<Company> findByDeletedFalse(Pageable pageable);
    Page<Company> findByStatusAndDeletedFalse(CompanyStatus status, Pageable pageable);
    boolean existsByNameIgnoreCaseAndDeletedFalse(String name);
}
