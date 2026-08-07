package com.dems.audit;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Value object capturing current HTTP request and SecurityContext metadata for audit recording.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditContext {

    private String username;
    private String userRole;
    private String ipAddress;
    private String userAgent;
    private String correlationId;
}
