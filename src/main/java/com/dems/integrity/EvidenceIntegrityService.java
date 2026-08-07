package com.dems.integrity;

import com.dems.evidence.EvidenceEntity;
import org.springframework.data.domain.Pageable;

/**
 * Service interface managing evidence hash generation, live integrity verification, forensic audit logging, and summary reporting.
 */
public interface EvidenceIntegrityService {

    /**
     * Computes and stores the initial cryptographic SHA-256 hash for a newly stored evidence record.
     *
     * @param evidence Target EvidenceEntity.
     */
    void computeAndStoreInitialHash(EvidenceEntity evidence);

    /**
     * Executes live integrity verification by re-computing file digest and comparing against stored record.
     * Persists forensic audit trail in EvidenceVerificationHistoryEntity.
     *
     * @param evidenceId    Evidence entity ID.
     * @param verifierEmail Email of executing user.
     * @return Verification response payload.
     */
    IntegrityVerificationResponse verifyEvidenceIntegrity(Long evidenceId, String verifierEmail);

    /**
     * Retrieves stored integrity metadata for an evidence record.
     *
     * @param evidenceId Evidence entity ID.
     * @return Verification response payload.
     */
    IntegrityVerificationResponse getStoredIntegrityInfo(Long evidenceId);

    /**
     * Aggregates summary statistics across all evidence records and returns paginated verification report.
     *
     * @param pageable Pagination configuration.
     * @return Summary report payload.
     */
    IntegritySummaryReportResponse getIntegritySummaryReport(Pageable pageable);
}
