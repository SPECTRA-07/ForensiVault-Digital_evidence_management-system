package com.dems.dashboard;

import com.dems.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * REST Controller for DEMS Read-Only Executive Dashboard & Operational Analytics.
 * Restricted to ADMIN and FORENSIC_EXPERT roles.
 */
@Tag(name = "Dashboard & Analytics", description = "Read-only executive analytics, module KPI summaries, and system operational health metrics")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/dashboard")
@PreAuthorize("hasAnyRole('ADMIN', 'FORENSIC_EXPERT')")
public class DashboardController {

    private final DashboardService dashboardService;

    public DashboardController(DashboardService dashboardService) {
        this.dashboardService = dashboardService;
    }

    @Operation(summary = "Get high-level executive summary metrics across all DEMS modules")
    @GetMapping("/summary")
    public ResponseEntity<ApiResponse<DashboardSummaryResponse>> getSummary(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        DashboardSummaryResponse summary = dashboardService.getSummary(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(summary, "Executive summary metrics generated successfully"));
    }

    @Operation(summary = "Get Case analytics including distributions by status, crime type, severity, and monthly creation trends")
    @GetMapping("/cases")
    public ResponseEntity<ApiResponse<CaseAnalyticsResponse>> getCaseAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        CaseAnalyticsResponse analytics = dashboardService.getCaseAnalytics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(analytics, "Case analytics generated successfully"));
    }

    @Operation(summary = "Get Evidence analytics including file types, largest files, recent uploads, and monthly trends")
    @GetMapping("/evidence")
    public ResponseEntity<ApiResponse<EvidenceAnalyticsResponse>> getEvidenceAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        EvidenceAnalyticsResponse analytics = dashboardService.getEvidenceAnalytics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(analytics, "Evidence analytics generated successfully"));
    }

    @Operation(summary = "Get Evidence Integrity analytics including verified vs tampered counts and recent verification logs")
    @GetMapping("/integrity")
    public ResponseEntity<ApiResponse<IntegrityAnalyticsResponse>> getIntegrityAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        IntegrityAnalyticsResponse analytics = dashboardService.getIntegrityAnalytics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(analytics, "Integrity analytics generated successfully"));
    }

    @Operation(summary = "Get Chain of Custody analytics including transfer status ratios, average transfer time, and recent handshakes")
    @GetMapping("/custody")
    public ResponseEntity<ApiResponse<CustodyAnalyticsResponse>> getCustodyAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        CustodyAnalyticsResponse analytics = dashboardService.getCustodyAnalytics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(analytics, "Custody analytics generated successfully"));
    }

    @Operation(summary = "Get System Audit analytics including action breakdowns, module counts, top active users, and recent audit logs")
    @GetMapping("/audit")
    public ResponseEntity<ApiResponse<AuditAnalyticsResponse>> getAuditAnalytics(
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate) {
        AuditAnalyticsResponse analytics = dashboardService.getAuditAnalytics(startDate, endDate);
        return ResponseEntity.ok(ApiResponse.success(analytics, "Audit analytics generated successfully"));
    }

    @Operation(summary = "Get real-time consolidated recent activity stream across all system modules")
    @GetMapping("/recent-activities")
    public ResponseEntity<ApiResponse<List<RecentActivityDto>>> getRecentActivities() {
        List<RecentActivityDto> activities = dashboardService.getRecentActivities();
        return ResponseEntity.ok(ApiResponse.success(activities, "Recent activities feed generated successfully"));
    }

    @Operation(summary = "Get operational system health, storage footprint, database status, and active user metrics")
    @GetMapping("/system-health")
    public ResponseEntity<ApiResponse<SystemHealthResponse>> getSystemHealth() {
        SystemHealthResponse health = dashboardService.getSystemHealth();
        return ResponseEntity.ok(ApiResponse.success(health, "System health operational status generated successfully"));
    }
}
