package com.dems.custody;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * Chronological Forensic Timeline Response DTO showing complete movement history for evidence.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustodyTimelineResponse {

    private Long evidenceId;
    private String evidenceNumber;
    private String evidenceName;
    private String currentCustodianName;
    private Long currentCustodianId;
    private int totalTransfers;
    private List<CustodyResponse> timeline;
}
