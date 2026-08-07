package com.dems.evidence;

import com.dems.enums.EvidenceStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating evidence status state.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class EvidenceStatusUpdateRequest {

    @NotNull(message = "New evidence status is required")
    private EvidenceStatus status;

    private String remarks;
}
