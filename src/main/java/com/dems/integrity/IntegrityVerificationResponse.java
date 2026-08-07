package com.dems.integrity;

import com.dems.enums.IntegrityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO returning cryptographic verification status and forensic comparison details.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class IntegrityVerificationResponse {

    private String verificationNumber;
    private Long evidenceId;
    private String evidenceNumber;
    private String evidenceName;
    private IntegrityStatus integrityStatus;
    private String storedHash;
    private String currentHash;
    private String hashAlgorithm;
    private OffsetDateTime verifiedAt;
    private String verifiedBy;
    private String message;
}
