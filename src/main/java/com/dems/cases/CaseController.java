package com.dems.cases;

import com.dems.common.ApiResponse;
import com.dems.enums.CaseStatus;
import com.dems.enums.CrimeSeverity;
import com.dems.enums.CrimeType;
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
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.time.LocalDate;

/**
 * REST Controller for DEMS Criminal Investigation Case Management.
 */
@Tag(name = "Case Management", description = "Endpoints for creating, updating, searching, and assigning investigation cases")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/cases")
public class CaseController {

    private final CaseService caseService;

    public CaseController(CaseService caseService) {
        this.caseService = caseService;
    }

    @Operation(summary = "Create a new criminal case (Admin only)")
    @PostMapping
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CaseResponse>> createCase(@Valid @RequestBody CaseCreateRequest request) {
        CaseResponse response = caseService.createCase(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Case record created successfully"));
    }

    @Operation(summary = "Get all cases with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<CaseSummaryResponse>>> getAllCases(
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CaseSummaryResponse> cases = caseService.getAllCases(pageable);
        return ResponseEntity.ok(ApiResponse.success(cases, "Cases retrieved successfully"));
    }

    @Operation(summary = "Get cases assigned to the currently authenticated officer")
    @GetMapping("/my-cases")
    public ResponseEntity<ApiResponse<Page<CaseSummaryResponse>>> getMyAssignedCases(
            @AuthenticationPrincipal UserDetails currentUser,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<CaseSummaryResponse> cases = caseService.getMyAssignedCases(currentUser.getUsername(), pageable);
        return ResponseEntity.ok(ApiResponse.success(cases, "Assigned cases retrieved successfully"));
    }

    @Operation(summary = "Get detailed case information by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseResponse>> getCaseById(@PathVariable Long id) {
        CaseResponse caseResponse = caseService.getCaseById(id);
        return ResponseEntity.ok(ApiResponse.success(caseResponse, "Case details retrieved successfully"));
    }

    @Operation(summary = "Update case metadata details")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<CaseResponse>> updateCase(
            @PathVariable Long id,
            @Valid @RequestBody CaseUpdateRequest request) {
        CaseResponse response = caseService.updateCase(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Case updated successfully"));
    }

    @Operation(summary = "Update case status state machine")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<CaseResponse>> updateCaseStatus(
            @PathVariable Long id,
            @Valid @RequestBody CaseStatusUpdateRequest request) {
        CaseResponse response = caseService.updateCaseStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Case status updated successfully"));
    }

    @Operation(summary = "Assign or reassign Police Officer to case (Admin only)")
    @PatchMapping("/{id}/assign-officer")
    @PreAuthorize("hasRole('ADMIN')")
    public ResponseEntity<ApiResponse<CaseResponse>> assignOfficer(
            @PathVariable Long id,
            @Valid @RequestBody CaseAssignOfficerRequest request) {
        CaseResponse response = caseService.assignOfficer(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Police Officer assigned successfully"));
    }

    @Operation(summary = "Dynamic search and filter cases with pagination")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<CaseSummaryResponse>>> searchCases(
            @RequestParam(required = false) String caseNumber,
            @RequestParam(required = false) String crimeNumber,
            @RequestParam(required = false) String caseName,
            @RequestParam(required = false) CaseStatus status,
            @RequestParam(required = false) CrimeType crimeType,
            @RequestParam(required = false) CrimeSeverity severity,
            @RequestParam(required = false) Long assignedOfficerId,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate incidentDate,
            @RequestParam(required = false) Boolean active,
            @RequestParam(required = false) String searchTerm,
            @PageableDefault(sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<CaseSummaryResponse> results = caseService.searchCases(
                caseNumber, crimeNumber, caseName, status, crimeType, severity, assignedOfficerId, incidentDate, active, searchTerm, pageable
        );
        return ResponseEntity.ok(ApiResponse.success(results, "Case search completed successfully"));
    }
}
