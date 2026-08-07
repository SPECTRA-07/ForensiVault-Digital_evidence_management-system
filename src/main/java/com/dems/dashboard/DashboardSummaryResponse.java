package com.dems.dashboard;

import com.dems.enums.EvidenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Executive Summary Response DTO aggregating key metrics across all DEMS modules.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardSummaryResponse {

    private long totalCases;
    private long openCases;
    private long underInvestigationCases;
    private long closedCases;
    private long archivedCases;

    private long totalEvidence;
    private Map<EvidenceType, Long> evidenceByType;
    private long verifiedEvidence;
    private long tamperedEvidence;

    private long pendingCustodyTransfers;

    private long todaysAuditEvents;
    private long successfulAuditEvents;
    private long failedAuditEvents;
}
