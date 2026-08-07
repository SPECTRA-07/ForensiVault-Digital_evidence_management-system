package com.dems.cases;

import com.dems.enums.CaseStatus;
import com.dems.enums.CrimeSeverity;
import com.dems.enums.CrimeType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;

/**
 * Service interface managing Case lifecycle, status transitions, officer assignments, and dynamic searching.
 */
public interface CaseService {

    CaseResponse createCase(CaseCreateRequest request);

    CaseResponse getCaseById(Long id);

    Page<CaseSummaryResponse> getAllCases(Pageable pageable);

    Page<CaseSummaryResponse> getMyAssignedCases(String userEmail, Pageable pageable);

    CaseResponse updateCase(Long id, CaseUpdateRequest request);

    CaseResponse updateCaseStatus(Long id, CaseStatusUpdateRequest request);

    CaseResponse assignOfficer(Long id, CaseAssignOfficerRequest request);

    Page<CaseSummaryResponse> searchCases(
            String caseNumber,
            String crimeNumber,
            String caseName,
            CaseStatus status,
            CrimeType crimeType,
            CrimeSeverity severity,
            Long assignedOfficerId,
            LocalDate incidentDate,
            Boolean active,
            String searchTerm,
            Pageable pageable
    );
}
