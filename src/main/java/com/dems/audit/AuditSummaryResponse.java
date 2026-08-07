package com.dems.audit;

import com.dems.enums.AuditAction;
import com.dems.enums.AuditEntityType;
import com.dems.enums.AuditStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Lightweight Summary Response DTO optimized for paginated audit lists.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditSummaryResponse {

    private Long id;
    private String auditNumber;
    private AuditAction action;
    private AuditEntityType entityType;
    private String moduleName;
    private String entityReference;
    private String username;
    private String ipAddress;
    private AuditStatus status;
    private String description;
    private OffsetDateTime actionTimestamp;
}
