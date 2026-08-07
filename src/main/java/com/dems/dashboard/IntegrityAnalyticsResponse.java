package com.dems.dashboard;

import com.dems.integrity.IntegrityVerificationResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Integrity Analytics Response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrityAnalyticsResponse {

    private long verifiedCount;
    private long tamperedCount;
    private long hashMissingCount;
    private long verificationFailedCount;
    private List<IntegrityVerificationResponse> latestVerifications;
}
