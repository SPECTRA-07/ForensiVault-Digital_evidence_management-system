package com.dems.audit;

import com.dems.enums.AuditAction;
import com.dems.enums.AuditEntityType;
import com.dems.enums.AuditStatus;
import com.dems.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Detailed Response DTO representing full AuditLog attributes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditLogResponse {

    private Long id;
    private String auditNumber;
    private AuditAction action;
    private AuditEntityType entityType;
    private String moduleName;
    private String entityReference;
    private Long entityId;
    private UserResponse performedBy;
    private String username;
    private String userRole;
    private String ipAddress;
    private String userAgent;
    private String correlationId;
    private String previousValue;
    private String newValue;
    private Long executionTimeMs;
    private AuditStatus status;
    private String description;
    private String failureReason;
    private OffsetDateTime actionTimestamp;
    private Boolean active;
    private OffsetDateTime createdAt;
}
