package com.dems.dashboard;

import com.dems.audit.AuditSummaryResponse;
import com.dems.enums.AuditAction;
import com.dems.enums.AuditStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Audit Analytics Response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AuditAnalyticsResponse {

    private Map<AuditAction, Long> eventsByAction;
    private Map<String, Long> eventsByModule;
    private Map<AuditStatus, Long> eventsByStatus;
    private List<TopActiveUserDto> topActiveUsers;
    private List<AuditSummaryResponse> recentActivities;
}
