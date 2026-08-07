package com.dems.qr;

import com.dems.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for DEMS Physical Evidence Barcode / QR Code Tracking.
 */
@Tag(name = "QR Code & Physical Evidence Tracking", description = "Endpoints for generating, retrieving, streaming PNG images, regenerating, and resolving physical evidence barcodes")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/qr")
public class QRCodeController {

    private final QRCodeService qrCodeService;

    public QRCodeController(QRCodeService qrCodeService) {
        this.qrCodeService = qrCodeService;
    }

    @Operation(summary = "Get QR code metadata and download URL for an evidence record")
    @GetMapping("/evidence/{evidenceId}")
    public ResponseEntity<ApiResponse<QRCodeResponse>> getQRCodeInfo(@PathVariable Long evidenceId) {
        QRCodeResponse response = qrCodeService.getQRCodeInfo(evidenceId);
        return ResponseEntity.ok(ApiResponse.success(response, "QR code metadata retrieved successfully"));
    }

    @Operation(summary = "Stream 250x250 PNG QR code barcode image for an evidence record")
    @GetMapping(value = "/evidence/{evidenceId}/image", produces = MediaType.IMAGE_PNG_VALUE)
    public ResponseEntity<Resource> getQRCodeImage(@PathVariable Long evidenceId) {
        Resource imageResource = qrCodeService.getQRCodeImageResource(evidenceId);
        return ResponseEntity.ok()
                .contentType(MediaType.IMAGE_PNG)
                .header(HttpHeaders.CONTENT_DISPOSITION, "inline; filename=\"QR-evidence-" + evidenceId + ".png\"")
                .body(imageResource);
    }

    @Operation(summary = "Regenerate physical QR Code barcode image for an evidence record (ADMIN only)")
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/evidence/{evidenceId}/regenerate")
    public ResponseEntity<ApiResponse<QRCodeResponse>> regenerateQRCode(@PathVariable Long evidenceId) {
        QRCodeResponse response = qrCodeService.regenerateQRCode(evidenceId);
        return ResponseEntity.ok(ApiResponse.success(response, "QR code regenerated successfully"));
    }

    @Operation(summary = "Resolve evidence QR code barcode scan to safe, non-sensitive evidence metadata")
    @GetMapping("/resolve/{evidenceNumber}")
    public ResponseEntity<ApiResponse<QRResolveResponse>> resolveQRCode(@PathVariable String evidenceNumber) {
        QRResolveResponse response = qrCodeService.resolveQRCode(evidenceNumber);
        return ResponseEntity.ok(ApiResponse.success(response, "QR code resolved successfully"));
    }
}
