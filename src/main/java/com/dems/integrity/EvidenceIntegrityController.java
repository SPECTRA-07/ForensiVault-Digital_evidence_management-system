package com.dems.integrity;

import com.dems.common.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * REST Controller for DEMS Evidence Integrity & Tamper Detection.
 */
@Tag(name = "Evidence Integrity", description = "Endpoints for cryptographic SHA-256 verification, tamper detection, and forensic audit reports")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/evidence")
public class EvidenceIntegrityController {

    private final EvidenceIntegrityService integrityService;

    public EvidenceIntegrityController(EvidenceIntegrityService integrityService) {
        this.integrityService = integrityService;
    }

    @Operation(summary = "Execute live SHA-256 integrity verification and tamper check on evidence file")
    @PostMapping("/{id}/verify")
    public ResponseEntity<ApiResponse<IntegrityVerificationResponse>> verifyEvidenceIntegrity(
            @PathVariable Long id,
            @AuthenticationPrincipal UserDetails currentUser) {
        String verifierEmail = currentUser != null ? currentUser.getUsername() : null;
        IntegrityVerificationResponse response = integrityService.verifyEvidenceIntegrity(id, verifierEmail);
        return ResponseEntity.ok(ApiResponse.success(response, "Integrity verification check completed"));
    }

    @Operation(summary = "Get stored integrity metadata for an evidence record")
    @GetMapping("/{id}/integrity")
    public ResponseEntity<ApiResponse<IntegrityVerificationResponse>> getStoredIntegrityInfo(@PathVariable Long id) {
        IntegrityVerificationResponse response = integrityService.getStoredIntegrityInfo(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Stored integrity info retrieved successfully"));
    }

    @Operation(summary = "Get comprehensive integrity summary report and verification dashboard statistics")
    @GetMapping("/integrity/report")
    public ResponseEntity<ApiResponse<IntegritySummaryReportResponse>> getIntegritySummaryReport(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        IntegritySummaryReportResponse report = integrityService.getIntegritySummaryReport(pageable);
        return ResponseEntity.ok(ApiResponse.success(report, "Integrity summary report generated successfully"));
    }
}
