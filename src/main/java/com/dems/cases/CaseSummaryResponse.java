package com.dems.cases;

import com.dems.enums.CaseStatus;
import com.dems.enums.CrimeSeverity;
import com.dems.enums.CrimeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.OffsetDateTime;

/**
 * Lightweight Summary Response DTO optimized for paginated search and list views.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseSummaryResponse {

    private Long id;
    private String caseNumber;
    private String crimeNumber;
    private String caseName;
    private CrimeType crimeType;
    private CrimeSeverity severity;
    private CaseStatus status;
    private LocalDate incidentDate;
    private String crimeSceneLocation;
    private String assignedOfficerName;
    private Long assignedOfficerId;
    private Boolean active;
    private OffsetDateTime createdAt;
}
