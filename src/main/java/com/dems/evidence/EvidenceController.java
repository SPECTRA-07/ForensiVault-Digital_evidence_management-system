package com.dems.evidence;

import com.dems.common.ApiResponse;
import com.dems.enums.EvidenceStatus;
import com.dems.enums.EvidenceType;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * REST Controller for DEMS Digital Evidence Management.
 */
@Tag(name = "Evidence Management", description = "Endpoints for uploading, downloading, searching, and managing digital evidence metadata")
@SecurityRequirement(name = "bearerAuth")
@RestController
@RequestMapping("/evidence")
public class EvidenceController {

    private final EvidenceService evidenceService;

    public EvidenceController(EvidenceService evidenceService) {
        this.evidenceService = evidenceService;
    }

    @Operation(summary = "Upload digital evidence file and metadata (Multipart request)", description = "Uploads an evidence file to local case storage and persists metadata record in PostgreSQL")
    @PostMapping(value = "/upload", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<ApiResponse<EvidenceResponse>> uploadEvidence(
            @Parameter(description = "Evidence digital file", required = true) @RequestPart("file") MultipartFile file,
            @Valid @ModelAttribute EvidenceUploadRequest request,
            @AuthenticationPrincipal UserDetails currentUser) {

        EvidenceResponse response = evidenceService.uploadEvidence(file, request, currentUser.getUsername());
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(ApiResponse.success(response, "Evidence file uploaded and registered successfully"));
    }

    @Operation(summary = "Get all evidence records with pagination")
    @GetMapping
    public ResponseEntity<ApiResponse<Page<EvidenceSummaryResponse>>> getAllEvidence(
            @PageableDefault(sort = "uploadedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<EvidenceSummaryResponse> evidence = evidenceService.getAllEvidence(pageable);
        return ResponseEntity.ok(ApiResponse.success(evidence, "Evidence records retrieved successfully"));
    }

    @Operation(summary = "Get detailed evidence record metadata by ID")
    @GetMapping("/{id}")
    public ResponseEntity<ApiResponse<EvidenceResponse>> getEvidenceById(@PathVariable Long id) {
        EvidenceResponse response = evidenceService.getEvidenceById(id);
        return ResponseEntity.ok(ApiResponse.success(response, "Evidence metadata retrieved successfully"));
    }

    @Operation(summary = "Download digital evidence file payload stream")
    @GetMapping("/download/{id}")
    public ResponseEntity<Resource> downloadEvidence(@PathVariable Long id) {
        EvidenceResponse metadata = evidenceService.getEvidenceById(id);
        Resource fileResource = evidenceService.downloadEvidence(id);

        return ResponseEntity.ok()
                .contentType(MediaType.parseMediaType(metadata.getMimeType()))
                .header(HttpHeaders.CONTENT_DISPOSITION,
                        "attachment; filename=\"" + metadata.getOriginalFileName() + "\"")
                .contentLength(metadata.getFileSize())
                .body(fileResource);
    }

    @Operation(summary = "Update evidence record metadata")
    @PutMapping("/{id}")
    public ResponseEntity<ApiResponse<EvidenceResponse>> updateEvidence(
            @PathVariable Long id,
            @Valid @RequestBody EvidenceUpdateRequest request) {
        EvidenceResponse response = evidenceService.updateEvidence(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Evidence metadata updated successfully"));
    }

    @Operation(summary = "Update evidence status state")
    @PatchMapping("/{id}/status")
    public ResponseEntity<ApiResponse<EvidenceResponse>> updateEvidenceStatus(
            @PathVariable Long id,
            @Valid @RequestBody EvidenceStatusUpdateRequest request) {
        EvidenceResponse response = evidenceService.updateEvidenceStatus(id, request);
        return ResponseEntity.ok(ApiResponse.success(response, "Evidence status updated successfully"));
    }

    @Operation(summary = "Get evidence records associated with a specific Case ID")
    @GetMapping("/case/{caseId}")
    public ResponseEntity<ApiResponse<Page<EvidenceSummaryResponse>>> getEvidenceByCaseId(
            @PathVariable Long caseId,
            @PageableDefault(sort = "uploadedAt", direction = Sort.Direction.DESC) Pageable pageable) {
        Page<EvidenceSummaryResponse> evidence = evidenceService.getEvidenceByCaseId(caseId, pageable);
        return ResponseEntity.ok(ApiResponse.success(evidence, "Case evidence records retrieved successfully"));
    }

    @Operation(summary = "Dynamic search and filter evidence with pagination")
    @GetMapping("/search")
    public ResponseEntity<ApiResponse<Page<EvidenceSummaryResponse>>> searchEvidence(
            @RequestParam(required = false) EvidenceStatus status,
            @RequestParam(required = false) EvidenceType evidenceType,
            @RequestParam(required = false) Long caseId,
            @RequestParam(required = false) Long uploadedById,
            @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate uploadDate,
            @RequestParam(required = false) String evidenceNumber,
            @RequestParam(required = false) String evidenceName,
            @RequestParam(required = false) String searchTerm,
            @RequestParam(required = false) Boolean active,
            @PageableDefault(sort = "uploadedAt", direction = Sort.Direction.DESC) Pageable pageable) {

        Page<EvidenceSummaryResponse> results = evidenceService.searchEvidence(
                status, evidenceType, caseId, uploadedById, uploadDate, evidenceNumber, evidenceName, searchTerm,
                active, pageable);
        return ResponseEntity.ok(ApiResponse.success(results, "Evidence search completed successfully"));
    }
}
