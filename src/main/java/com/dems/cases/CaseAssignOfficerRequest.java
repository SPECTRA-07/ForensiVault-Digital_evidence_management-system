package com.dems.cases;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for assigning or reassigning a Police Officer to a case (Admin only).
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseAssignOfficerRequest {

    @NotNull(message = "Officer ID is required")
    private Long officerId;
}
