package com.dems.evidence;

import com.dems.enums.EvidenceStatus;
import com.dems.enums.EvidenceType;
import com.dems.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Detailed Response DTO representing full Evidence metadata attributes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceResponse {

    private Long id;
    private String evidenceNumber;
    private String evidenceName;
    private String displayName;
    private String description;
    private String remarks;
    private EvidenceType evidenceType;
    private EvidenceStatus status;
    private String originalFileName;
    private String storedFileName;
    private String fileExtension;
    private Long fileSize;
    private String mimeType;
    private String storagePath;
    private String collectedFrom;
    private String collectionMethod;
    private LocalDateTime collectedAt;
    private String collectedBy;
    private Long downloadCount;
    private String fileHash;
    private String hashAlgorithm;
    private String qrFileName;
    private String qrDownloadUrl;
    private OffsetDateTime qrGeneratedAt;
    private LocalDateTime uploadedAt;
    private UserResponse uploadedBy;
    private UserResponse currentCustodian;
    private OffsetDateTime lastTransferredAt;
    private Long caseId;
    private String caseNumber;
    private String caseName;
    private Boolean active;
    private OffsetDateTime createdAt;
    private OffsetDateTime updatedAt;
}
