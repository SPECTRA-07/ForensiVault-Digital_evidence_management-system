package com.dems.audit;

import com.dems.enums.AuditAction;
import com.dems.enums.AuditEntityType;
import com.dems.enums.AuditStatus;
import com.dems.user.UserEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

/**
 * Reusable AuditService interface for capturing permanent, immutable audit log events across all DEMS modules.
 */
public interface AuditService {

    void recordAudit(
            AuditAction action,
            AuditEntityType entityType,
            String moduleName,
            String entityReference,
            Long entityId,
            UserEntity performedBy,
            String username,
            String userRole,
            String ipAddress,
            String userAgent,
            String correlationId,
            String previousValue,
            String newValue,
            Long executionTimeMs,
            AuditStatus status,
            String description,
            String failureReason
    );

    void recordEvent(
            AuditAction action,
            AuditEntityType entityType,
            String moduleName,
            String entityReference,
            Long entityId,
            AuditStatus status,
            String description
    );

    void recordEvent(
            AuditAction action,
            AuditEntityType entityType,
            String moduleName,
            String entityReference,
            Long entityId,
            AuditStatus status,
            String description,
            String failureReason
    );

    void recordEventWithDiff(
            AuditAction action,
            AuditEntityType entityType,
            String moduleName,
            String entityReference,
            Long entityId,
            String previousValue,
            String newValue,
            AuditStatus status,
            String description
    );

    Page<AuditSummaryResponse> getAllAuditLogs(Pageable pageable);

    AuditLogResponse getAuditLogById(Long id);

    Page<AuditSummaryResponse> getAuditLogsByUserId(Long userId, Pageable pageable);

    Page<AuditSummaryResponse> getAuditLogsByEntity(AuditEntityType entityType, Long entityId, Pageable pageable);

    Page<AuditSummaryResponse> searchAuditLogs(AuditSearchRequest request, Pageable pageable);

    AuditDashboardResponse getAuditDashboard(Pageable pageable);
}
