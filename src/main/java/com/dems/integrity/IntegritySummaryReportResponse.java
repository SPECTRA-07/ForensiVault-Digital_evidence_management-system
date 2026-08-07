package com.dems.integrity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

/**
 * Summary report DTO featuring aggregated counts across all evidence integrity states and paginated reports.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegritySummaryReportResponse {

    private long totalEvidenceCount;
    private long verifiedCount;
    private long tamperedCount;
    private long fileMissingCount;
    private long hashMissingCount;
    private long verificationFailedCount;
    private Page<IntegrityVerificationResponse> records;
}
