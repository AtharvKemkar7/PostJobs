package com.recruitmentplatform.company.entity;

import com.recruitmentplatform.company.enums.CompanyStatus;
import com.recruitmentplatform.company.enums.EmployeeCountRange;
import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.time.LocalDateTime;
import java.util.UUID;

@Entity
@Table(name = "companies")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Company {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(nullable = false, length = 255)
    private String name;

    @Column(columnDefinition = "TEXT")
    private String description;

    @Column(length = 100)
    private String industry;

    @Column(length = 500)
    private String website;

    @Column(name = "logo_object_key", length = 500)
    private String logoObjectKey;

    @Column(name = "headquarters_city", length = 100)
    private String headquartersCity;

    @Column(name = "headquarters_state", length = 100)
    private String headquartersState;

    @Column(name = "headquarters_country", length = 100)
    private String headquartersCountry;

    @Enumerated(EnumType.STRING)
    @Column(name = "employee_count", length = 20)
    private EmployeeCountRange employeeCount;

    @Column(name = "founded_year")
    private Integer foundedYear;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    @Builder.Default
    private CompanyStatus status = CompanyStatus.ACTIVE;

    @Column(name = "created_by_user_id", nullable = false)
    private UUID createdByUserId;

    @CreationTimestamp
    @Column(name = "created_at", nullable = false, updatable = false)
    private LocalDateTime createdAt;

    @UpdateTimestamp
    @Column(name = "updated_at", nullable = false)
    private LocalDateTime updatedAt;

    @Column(nullable = false)
    @Builder.Default
    private boolean deleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

    @Version
    @Column(nullable = false)
    @Builder.Default
    private Integer version = 0;
}
