package com.dems.audit;

import com.dems.enums.AuditAction;
import com.dems.enums.AuditEntityType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

import java.util.Map;

/**
 * Operational Summary Dashboard Response DTO returning high-level audit metrics and recent audit entries.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditDashboardResponse {

    private long totalAuditCount;
    private long successCount;
    private long failedCount;
    private long warningCount;
    private Map<AuditAction, Long> countByAction;
    private Map<AuditEntityType, Long> countByEntityType;
    private Page<AuditSummaryResponse> recentLogs;
}
