package com.dems.evidence;

import com.dems.enums.EvidenceStatus;
import com.dems.enums.EvidenceType;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * Lightweight Summary Response DTO optimized for paginated search and case evidence lists.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceSummaryResponse {

    private Long id;
    private String evidenceNumber;
    private String evidenceName;
    private String displayName;
    private EvidenceType evidenceType;
    private EvidenceStatus status;
    private String originalFileName;
    private Long fileSize;
    private String mimeType;
    private Long caseId;
    private String caseNumber;
    private String uploadedByName;
    private Long uploadedById;
    private String currentCustodianName;
    private Long currentCustodianId;
    private LocalDateTime uploadedAt;
    private Long downloadCount;
    private Boolean active;
}
