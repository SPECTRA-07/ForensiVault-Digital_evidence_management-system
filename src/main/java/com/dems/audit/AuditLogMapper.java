package com.dems.audit;

import com.dems.user.UserMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper component converting AuditLogEntity to response DTOs.
 */
@Component
public class AuditLogMapper {

    private final UserMapper userMapper;

    public AuditLogMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public AuditLogResponse toResponse(AuditLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return AuditLogResponse.builder()
                .id(entity.getId())
                .auditNumber(entity.getAuditNumber())
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .moduleName(entity.getModuleName())
                .entityReference(entity.getEntityReference())
                .entityId(entity.getEntityId())
                .performedBy(userMapper.toResponse(entity.getPerformedBy()))
                .username(entity.getUsername())
                .userRole(entity.getUserRole())
                .ipAddress(entity.getIpAddress())
                .userAgent(entity.getUserAgent())
                .correlationId(entity.getCorrelationId())
                .previousValue(entity.getPreviousValue())
                .newValue(entity.getNewValue())
                .executionTimeMs(entity.getExecutionTimeMs())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .failureReason(entity.getFailureReason())
                .actionTimestamp(entity.getActionTimestamp())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }

    public AuditSummaryResponse toSummaryResponse(AuditLogEntity entity) {
        if (entity == null) {
            return null;
        }
        return AuditSummaryResponse.builder()
                .id(entity.getId())
                .auditNumber(entity.getAuditNumber())
                .action(entity.getAction())
                .entityType(entity.getEntityType())
                .moduleName(entity.getModuleName())
                .entityReference(entity.getEntityReference())
                .username(entity.getUsername())
                .ipAddress(entity.getIpAddress())
                .status(entity.getStatus())
                .description(entity.getDescription())
                .actionTimestamp(entity.getActionTimestamp())
                .build();
    }
}
