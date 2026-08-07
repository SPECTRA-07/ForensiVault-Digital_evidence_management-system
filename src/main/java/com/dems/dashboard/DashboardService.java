package com.dems.dashboard;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Service interface aggregating enterprise-grade read-only dashboard metrics and analytics across DEMS repositories.
 */
public interface DashboardService {

    DashboardSummaryResponse getSummary(OffsetDateTime startDate, OffsetDateTime endDate);

    CaseAnalyticsResponse getCaseAnalytics(OffsetDateTime startDate, OffsetDateTime endDate);

    EvidenceAnalyticsResponse getEvidenceAnalytics(OffsetDateTime startDate, OffsetDateTime endDate);

    IntegrityAnalyticsResponse getIntegrityAnalytics(OffsetDateTime startDate, OffsetDateTime endDate);

    CustodyAnalyticsResponse getCustodyAnalytics(OffsetDateTime startDate, OffsetDateTime endDate);

    AuditAnalyticsResponse getAuditAnalytics(OffsetDateTime startDate, OffsetDateTime endDate);

    List<RecentActivityDto> getRecentActivities();

    SystemHealthResponse getSystemHealth();
}
