package com.dems.evidence;

import com.dems.enums.EvidenceStatus;
import com.dems.enums.EvidenceType;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.web.multipart.MultipartFile;

import java.time.LocalDate;

/**
 * Service interface managing digital evidence uploads, file storage orchestration, downloads, and metadata updates.
 */
public interface EvidenceService {

    EvidenceResponse uploadEvidence(MultipartFile file, EvidenceUploadRequest request, String uploaderEmail);

    EvidenceResponse getEvidenceById(Long id);

    Page<EvidenceSummaryResponse> getAllEvidence(Pageable pageable);

    Page<EvidenceSummaryResponse> getEvidenceByCaseId(Long caseId, Pageable pageable);

    Resource downloadEvidence(Long id);

    EvidenceResponse updateEvidence(Long id, EvidenceUpdateRequest request);

    EvidenceResponse updateEvidenceStatus(Long id, EvidenceStatusUpdateRequest request);

    Page<EvidenceSummaryResponse> searchEvidence(
            EvidenceStatus status,
            EvidenceType evidenceType,
            Long caseId,
            Long uploadedById,
            LocalDate uploadDate,
            String evidenceNumber,
            String evidenceName,
            String searchTerm,
            Boolean active,
            Pageable pageable
    );
}
