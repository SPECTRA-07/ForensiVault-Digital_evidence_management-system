package com.dems.dashboard;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * System Operational Health metrics response payload.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class SystemHealthResponse {

    private String status;
    private String database;
    private long totalCases;
    private long totalEvidenceFiles;
    private long totalStorageBytes;
    private String formattedStorageSize;
    private long activeUsersCount;
    private double integritySuccessRatePercentage;
    private OffsetDateTime checkedAt;
}
