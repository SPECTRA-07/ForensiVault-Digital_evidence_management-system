package com.dems.cases;

import com.dems.enums.CaseStatus;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for updating case status state machine.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseStatusUpdateRequest {

    @NotNull(message = "New case status is required")
    private CaseStatus status;

    private String remarks;
}
