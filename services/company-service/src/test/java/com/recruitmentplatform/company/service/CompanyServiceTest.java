package com.recruitmentplatform.company.service;

import com.recruitmentplatform.company.dto.CompanyDtos;
import com.recruitmentplatform.company.entity.Company;
import com.recruitmentplatform.company.enums.CompanyStatus;
import com.recruitmentplatform.company.repository.CompanyMemberRepository;
import com.recruitmentplatform.company.repository.CompanyRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CompanyServiceTest {

    @Mock
    private CompanyRepository companyRepository;

    @Mock
    private CompanyMemberRepository companyMemberRepository;

    @InjectMocks
    private CompanyService companyService;

    private UUID userId;
    private CompanyDtos.CreateRequest createRequest;

    @BeforeEach
    void setUp() {
        userId = UUID.randomUUID();
        createRequest = CompanyDtos.CreateRequest.builder()
                .name("Acme Corp")
                .industry("Technology")
                .website("https://acme.example.com")
                .headquartersCity("San Francisco")
                .headquartersCountry("USA")
                .build();
    }

    @Test
    void testCreateCompanySuccess() {
        when(companyRepository.existsByNameIgnoreCaseAndDeletedFalse("Acme Corp")).thenReturn(false);
        when(companyRepository.save(any())).thenAnswer(inv -> {
            Company c = inv.getArgument(0);
            c.setId(UUID.randomUUID());
            return c;
        });

        CompanyDtos.Response response = companyService.createCompany(userId, createRequest);

        assertNotNull(response);
        assertEquals("Acme Corp", response.getName());
        assertEquals(CompanyStatus.ACTIVE, response.getStatus());
        verify(companyMemberRepository, times(1)).save(any());
    }

    @Test
    void testCreateCompanyDuplicateName() {
        when(companyRepository.existsByNameIgnoreCaseAndDeletedFalse("Acme Corp")).thenReturn(true);

        assertThrows(IllegalArgumentException.class, () -> companyService.createCompany(userId, createRequest));
        verify(companyRepository, never()).save(any());
    }
}
