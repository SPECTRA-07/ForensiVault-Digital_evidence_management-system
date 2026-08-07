package com.dems.audit;

import com.dems.common.ApiResponse;
import com.dems.enums.AuditEntityType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for DEMS Centralized Immutable Audit Logging and Activity Tracking.
 */
@Tag(name = "Audit Logging", description = "Endpoints for retrieving system activity logs, forensic audit records, and operational dashboards")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/audit")
public class AuditController {

    private final AuditService auditService;

    public AuditController(AuditService auditService) {
        this.auditService = auditService;
    }

    @Operation(summary = "Get all system audit log records with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<AuditSummaryResponse>>> getAllAuditLogs(
            @PageableDefault(sort = "actionTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditSummaryResponse> logs = auditService.getAllAuditLogs(pageable);
        return ResponseEntity.ok(ApiResponse.success(logs, "Audit log records retrieved successfully"));
    }

    @Operation(summary = "Get detailed audit log record by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<AuditLogResponse>> getAuditLogById(@PathVariable Long id) {
        AuditLogResponse response = auditService.getAuditLogById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Audit log record details retrieved successfully"));
    }

    @Operation(summary = "Dynamic search and filter system audit log entries with pagination")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<AuditSummaryResponse>>> searchAuditLogs(
            @Valid @ModelAttribute AuditSearchRequest request,
            @PageableDefault(sort = "actionTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditSummaryResponse> results = auditService.searchAuditLogs(request, pageable);
        return ResponseEntity.ok(ApiResponse.success(results, "Audit search completed successfully"));
    }

    @Operation(summary = "Get operational audit dashboard summary metrics and recent activity logs")
    @GetMapping("/dashboard")
    public ResponseEntity<ApiResponse<AuditDashboardResponse>> getAuditDashboard(
            @PageableDefault(sort = "actionTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        AuditDashboardResponse dashboard = auditService.getAuditDashboard(pageable);
        return ResponseEntity.ok(ApiResponse.success(dashboard, "Audit dashboard generated successfully"));
    }

    @Operation(summary = "Get audit log entries performed by a specific user")
    @GetMapping("/user/{userId}")
    public ResponseEntity<ApiResponse<Page<AuditSummaryResponse>>> getAuditLogsByUserId(
            @PathVariable Long userId,
            @PageableDefault(sort = "actionTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditSummaryResponse> logs = auditService.getAuditLogsByUserId(userId, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs, "User audit logs retrieved successfully"));
    }

    @Operation(summary = "Get audit log entries for a specific domain entity")
    @GetMapping("/entity/{entityType}/{entityId}")
    public ResponseEntity<ApiResponse<Page<AuditSummaryResponse>>> getAuditLogsByEntity(
            @PathVariable AuditEntityType entityType,
            @PathVariable Long entityId,
            @PageableDefault(sort = "actionTimestamp", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<AuditSummaryResponse> logs = auditService.getAuditLogsByEntity(entityType, entityId, pageable);
        return ResponseEntity.ok(ApiResponse.success(logs, "Entity audit logs retrieved successfully"));
    }
}
