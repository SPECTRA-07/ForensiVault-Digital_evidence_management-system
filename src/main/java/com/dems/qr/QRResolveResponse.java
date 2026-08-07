package com.dems.qr;

import com.dems.enums.EvidenceStatus;
import com.dems.enums.IntegrityStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Safe Response DTO returned when resolving physical evidence barcodes/QR codes.
 * Contains basic evidence metadata without exposing sensitive forensic details, hashes, or storage paths.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRResolveResponse {

    private Long evidenceId;
    private String evidenceNumber;
    private String caseNumber;
    private String evidenceName;
    private EvidenceStatus evidenceStatus;
    private String currentCustodian;
    private IntegrityStatus integrityStatus;
    private OffsetDateTime qrGeneratedAt;
}
