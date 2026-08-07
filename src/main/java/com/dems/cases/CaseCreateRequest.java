package com.dems.cases;

import com.dems.enums.CrimeSeverity;
import com.dems.enums.CrimeType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PastOrPresent;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;

/**
 * DTO for creating a new criminal case record (Admin only).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseCreateRequest {

    @NotBlank(message = "Case number is required")
    private String caseNumber;

    @NotBlank(message = "Crime number is required")
    private String crimeNumber;

    @NotBlank(message = "Case name is required")
    private String caseName;

    private String caseSummary;

    @NotNull(message = "Crime type is required")
    private CrimeType crimeType;

    @NotNull(message = "Crime severity is required")
    private CrimeSeverity severity;

    @NotNull(message = "Incident date is required")
    @PastOrPresent(message = "Incident date cannot be in the future")
    private LocalDate incidentDate;

    @NotBlank(message = "Crime scene location is required")
    private String crimeSceneLocation;

    @PastOrPresent(message = "Investigation start date cannot be in the future")
    private LocalDate investigationStartDate;

    private Long assignedOfficerId;
}
