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
 * DTO for updating evidence record metadata.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceUpdateRequest {

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
