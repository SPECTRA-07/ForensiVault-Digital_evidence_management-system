package com.dems.custody;

import com.dems.common.ApiResponse;
import com.dems.enums.TransferPurpose;
import com.dems.enums.TransferStatus;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.OffsetDateTime;

/**
 * REST Controller for DEMS Legally Auditable Chain of Custody Management.
 */
@Tag(name = "Chain of Custody", description = "Endpoints for evidence transfer handshakes, custodian tracking, and forensic timeline audits")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/custody")
public class CustodyController {

    private final CustodyService custodyService;

    public CustodyController(CustodyService custodyService) {
        this.custodyService = custodyService;
    }

    @Operation(summary = "Initiate an evidence custody transfer handshake to another authorized user")
    @PostMapping("/transfer")
    public ResponseEntity<ApiResponse<CustodyResponse>> initiateTransfer(
            @Valid @RequestBody CustodyTransferRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        CustodyResponse response = custodyService.initiateTransfer(request, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Custody transfer initiated successfully"));
    }

    @Operation(summary = "Accept or reject a pending evidence custody transfer handshake")
    @PostMapping("/{id}/accept")
    public ResponseEntity<ApiResponse<CustodyResponse>> acceptOrRejectTransfer(
            @PathVariable Long id,
            @Valid @RequestBody CustodyAcceptRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {
        CustodyResponse response = custodyService.acceptOrRejectTransfer(id, request, currentUser.getUsername());
        return ResponseEntity.ok(ApiResponse.success(response, "Custody transfer response recorded successfully"));
    }

    @Operation(summary = "Get detailed custody record information by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CustodyResponse>> getCustodyById(@PathVariable Long id) {
        CustodyResponse response = custodyService.getCustodyById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Custody record details retrieved successfully"));
    }

    @Operation(summary = "Get paginated custody transfer records for a specific evidence item")
    @GetMapping("/evidence/{evidenceId}")
    public ResponseEntity<ApiResponse<Page<CustodyResponse>>> getCustodyByEvidenceId(
            @PathVariable Long evidenceId,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CustodyResponse> records = custodyService.getCustodyByEvidenceId(evidenceId, pageable);
        return ResponseEntity.ok(ApiResponse.success(records, "Evidence custody records retrieved successfully"));
    }

    @Operation(summary = "Get complete chronological forensic timeline history for an evidence item")
    @GetMapping("/history/{evidenceId}")
    public ResponseEntity<ApiResponse<CustodyTimelineResponse>> getCustodyTimeline(@PathVariable Long evidenceId) {
        CustodyTimelineResponse timeline = custodyService.getCustodyTimeline(evidenceId);
        return ResponseEntity.ok(ApiResponse.success(timeline, "Forensic custody timeline generated successfully"));
    }

    @Operation(summary = "Dynamic search and filter custody records with pagination")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<CustodyResponse>>> searchCustodyRecords(
            @RequestParam(required = false) Long evidenceId,
            @RequestParam(required = false) Long transferredById,
            @RequestParam(required = false) Long transferredToId,
            @RequestParam(required = false) TransferStatus transferStatus,
            @RequestParam(required = false) TransferPurpose transferPurpose,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime startDate,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) OffsetDateTime endDate,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CustodyResponse> results = custodyService.searchCustodyRecords(
                evidenceId, transferredById, transferredToId, transferStatus, transferPurpose, startDate, endDate, active, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(results, "Custody records search completed successfully"));
    }
}
