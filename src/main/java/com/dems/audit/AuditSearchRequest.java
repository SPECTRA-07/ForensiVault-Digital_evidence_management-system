package com.dems.audit;

import com.dems.enums.AuditAction;
import com.dems.enums.AuditEntityType;
import com.dems.enums.AuditStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;

import java.time.OffsetDateTime;

/**
 * Request DTO carrying query parameters for dynamic audit log searching.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditSearchRequest {

    private AuditAction action;
    private AuditEntityType entityType;
    private String moduleName;
    private String username;
    private AuditStatus status;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime startDate;

    @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME)
    private OffsetDateTime endDate;

    private String entityReference;
    private String ipAddress;
    private String correlationId;
    private Boolean active;
}
