package com.dems.custody;

import com.dems.enums.IntegrityStatus;
import com.dems.enums.TransferPurpose;
import com.dems.enums.TransferStatus;
import com.dems.user.UserResponse;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Detailed Response DTO representing full Custody Record entity attributes.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CustodyResponse {

    private Long id;
    private String custodyNumber;
    private Integer custodySequence;
    private Long evidenceId;
    private String evidenceNumber;
    private String evidenceName;
    private UserResponse transferredBy;
    private UserResponse transferredTo;
    private TransferStatus transferStatus;
    private TransferPurpose transferPurpose;
    private String transferLocation;
    private IntegrityStatus integrityStatusAtTransfer;
    private OffsetDateTime transferredAt;
    private OffsetDateTime acceptedAt;
    private String transferRemarks;
    private String acceptanceRemarks;
    private Boolean active;
    private OffsetDateTime createdAt;
}
