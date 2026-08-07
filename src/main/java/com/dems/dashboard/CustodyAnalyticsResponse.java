package com.dems.dashboard;

import com.dems.custody.CustodyResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Custody Analytics Response DTO.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustodyAnalyticsResponse {

    private long pendingTransfers;
    private long acceptedTransfers;
    private long rejectedTransfers;
    private double averageTransferTimeMinutes;
    private List<CustodyResponse> latestCustodyTransfers;
}
