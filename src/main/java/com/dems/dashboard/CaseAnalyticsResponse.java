package com.dems.dashboard;

import com.dems.enums.CaseStatus;
import com.dems.enums.CrimeSeverity;
import com.dems.enums.CrimeType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Case Analytics Response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseAnalyticsResponse {

    private Map<CaseStatus, Long> casesByStatus;
    private Map<CrimeType, Long> casesByCrimeType;
    private Map<CrimeSeverity, Long> casesBySeverity;
    private List<MonthlyCountDto> casesCreatedPerMonth;
}
