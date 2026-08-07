package com.dems.cases;

import com.dems.enums.CaseStatus;
import com.dems.enums.CrimeSeverity;
import com.dems.enums.CrimeType;
import com.dems.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Detailed Response DTO representing full Case entity attributes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseResponse {

    private Long id;
    private String caseNumber;
    private String crimeNumber;
    private String caseName;
    private String caseSummary;
    private CrimeType crimeType;
    private CrimeSeverity severity;
    private CaseStatus status;
    private LocalDate incidentDate;
    private String crimeSceneLocation;
    private LocalDate investigationStartDate;
    private LocalDate investigationEndDate;
    private UserResponse assignedOfficer;
    private Boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
