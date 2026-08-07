package com.dems.evidence;

import com.dems.enums.EvidenceType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

/**
 * DTO carrying evidence metadata parameters accompanying multipart file uploads.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceUploadRequest {

    @NotNull(message = "Case ID is required")
    private Long caseId;

    @NotBlank(message = "Evidence name is required")
    private String evidenceName;

    private String displayName;

    private String description;

    private String remarks;

    @NotNull(message = "Evidence type is required")
    private EvidenceType evidenceType;

    private String collectedFrom;

    private String collectionMethod;

    private LocalDateTime collectedAt;

    private String collectedBy;
}
