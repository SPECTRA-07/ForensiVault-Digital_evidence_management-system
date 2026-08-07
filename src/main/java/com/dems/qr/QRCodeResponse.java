package com.dems.qr;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.OffsetDateTime;

/**
 * Response DTO returning QR code metadata and download URL.
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class QRCodeResponse {

    private Long evidenceId;
    private String evidenceNumber;
    private String qrFileName;
    private String qrDownloadUrl;
    private OffsetDateTime generatedAt;
}
