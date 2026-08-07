package com.dems.custody;

import com.dems.enums.TransferPurpose;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * DTO for initiating evidence custody transfer.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustodyTransferRequest {

    @NotNull(message = "Evidence ID is required")
    private Long evidenceId;

    @NotNull(message = "Recipient officer ID is required")
    private Long transferredToId;

    @NotNull(message = "Transfer purpose is required")
    private TransferPurpose transferPurpose;

    @NotBlank(message = "Transfer location is required")
    private String transferLocation;

    private String transferRemarks;
}
