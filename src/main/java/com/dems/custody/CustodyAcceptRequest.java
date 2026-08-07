package com.dems.custody;

import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for accepting or rejecting a pending custody transfer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustodyAcceptRequest {

    @NotNull(message = "Accepted flag is required")
    private Boolean accepted;

    private String acceptanceRemarks;
}
