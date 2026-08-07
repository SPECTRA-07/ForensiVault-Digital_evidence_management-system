package com.dems.dashboard;

import com.dems.enums.EvidenceStatus;
import com.dems.enums.EvidenceType;
import com.dems.evidence.EvidenceSummaryResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Evidence Analytics Response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceAnalyticsResponse {

    private Map<EvidenceType, Long> evidenceByType;
    private Map<EvidenceStatus, Long> evidenceByStatus;
    private List<EvidenceSummaryResponse> largestFiles;
    private List<EvidenceSummaryResponse> latestUploads;
    private List<MonthlyCountDto> evidenceUploadedPerMonth;
}
