package com.dems.integrity;

import com.dems.audit.AuditService;
import com.dems.enums.AuditAction;
import com.dems.enums.AuditEntityType;
import com.dems.enums.AuditStatus;
import com.dems.enums.IntegrityStatus;
import com.dems.evidence.EvidenceEntity;
import com.dems.evidence.EvidenceRepository;
import com.dems.exception.ResourceNotFoundException;
import com.dems.user.UserEntity;
import com.dems.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Implementation of EvidenceIntegrityService managing initial hash generation, live verification,
 * forensic history persistence, summary report statistics, and audit logging.
 */
@Slf4j
@Service
public class EvidenceIntegrityServiceImpl implements EvidenceIntegrityService {

    private final EvidenceRepository evidenceRepository;
    private final UserRepository userRepository;
    private final EvidenceVerificationHistoryRepository historyRepository;
    private final HashService hashService;
    private final AuditService auditService;

    public EvidenceIntegrityServiceImpl(
            EvidenceRepository evidenceRepository,
            UserRepository userRepository,
            EvidenceVerificationHistoryRepository historyRepository,
            HashService hashService,
            AuditService auditService) {
        this.evidenceRepository = evidenceRepository;
        this.userRepository = userRepository;
        this.historyRepository = historyRepository;
        this.hashService = hashService;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public void computeAndStoreInitialHash(EvidenceEntity evidence) {
        Path filePath = Paths.get(evidence.getStoragePath());
        String hash = hashService.generateHash(filePath);

        evidence.setFileHash(hash);
        evidence.setHashAlgorithm("SHA-256");

        log.info("Evidence Hash Generated: Evidence Number [{}], Algorithm [SHA-256], Hash [{}]",
                evidence.getEvidenceNumber(), hash);
    }

    @Override
    @Transactional
    public IntegrityVerificationResponse verifyEvidenceIntegrity(Long evidenceId, String verifierEmail) {
        log.info("Integrity Verification Started: Evidence ID [{}] by User [{}]", evidenceId, verifierEmail);

        EvidenceEntity evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence record not found with ID: " + evidenceId));

        UserEntity verifier = null;
        if (verifierEmail != null && !verifierEmail.isBlank()) {
            verifier = userRepository.findByEmail(verifierEmail.trim().toLowerCase(Locale.ROOT)).orElse(null);
        }

        Path filePath = Paths.get(evidence.getStoragePath());
        IntegrityStatus status;
        String currentHash = null;
        String message;

        if (!Files.exists(filePath)) {
            log.warn("File Missing: Evidence ID [{}], Storage Path [{}]", evidenceId, evidence.getStoragePath());
            status = IntegrityStatus.FILE_MISSING;
            message = "Physical evidence file is missing from local storage path.";
        } else if (evidence.getFileHash() == null || evidence.getFileHash().trim().isEmpty()) {
            log.warn("Verification Failed: Stored SHA-256 hash missing for Evidence ID [{}]", evidenceId);
            status = IntegrityStatus.HASH_MISSING;
            message = "Stored SHA-256 cryptographic digest is missing on evidence record.";
            try {
                currentHash = hashService.generateHash(filePath);
            } catch (Exception e) {
                log.error("Failed to compute current hash for Evidence ID [{}]", evidenceId, e);
            }
        } else {
            try {
                currentHash = hashService.generateHash(filePath);
                if (evidence.getFileHash().equalsIgnoreCase(currentHash)) {
                    log.info("Verification Successful: Evidence ID [{}], Hash [{}]", evidenceId, currentHash);
                    status = IntegrityStatus.VERIFIED;
                    message = "Evidence integrity successfully verified. Hash matches stored record.";
                } else {
                    log.warn("Tampering Detected: Evidence ID [{}], Stored Hash [{}], Current Hash [{}]",
                            evidenceId, evidence.getFileHash(), currentHash);
                    status = IntegrityStatus.TAMPERED;
                    message = "TAMPER WARNING: File content has been modified or corrupted! Hash mismatch detected.";
                }
            } catch (Exception e) {
                log.error("Verification Failed: Error calculating hash for Evidence ID [{}]", evidenceId, e);
                status = IntegrityStatus.VERIFICATION_FAILED;
                message = "Verification failed due to file read or cryptographic calculation error.";
            }
        }

        String verificationNumber = generateVerificationNumber();
        OffsetDateTime verifiedAt = OffsetDateTime.now();

        // Persist forensic audit record
        EvidenceVerificationHistoryEntity history = EvidenceVerificationHistoryEntity.builder()
                .verificationNumber(verificationNumber)
                .evidence(evidence)
                .status(status)
                .storedHash(evidence.getFileHash())
                .currentHash(currentHash)
                .verifiedAt(verifiedAt)
                .verifiedBy(verifier)
                .remarks(message)
                .build();

        historyRepository.save(history);

        AuditStatus auditStatus = (status == IntegrityStatus.VERIFIED) ? AuditStatus.SUCCESS :
                (status == IntegrityStatus.TAMPERED ? AuditStatus.WARNING : AuditStatus.FAILED);

        auditService.recordEvent(
                AuditAction.VERIFY,
                AuditEntityType.INTEGRITY,
                "INTEGRITY",
                evidence.getEvidenceNumber(),
                evidence.getId(),
                auditStatus,
                "Integrity verification performed for evidence " + evidence.getEvidenceNumber() + ": " + message
        );

        String verifierName = verifier != null ? verifier.getFullName() : "System / Automated";

        return IntegrityVerificationResponse.builder()
                .verificationNumber(verificationNumber)
                .evidenceId(evidence.getId())
                .evidenceNumber(evidence.getEvidenceNumber())
                .evidenceName(evidence.getEvidenceName())
                .integrityStatus(status)
                .storedHash(evidence.getFileHash())
                .currentHash(currentHash)
                .hashAlgorithm(evidence.getHashAlgorithm() != null ? evidence.getHashAlgorithm() : "SHA-256")
                .verifiedAt(verifiedAt)
                .verifiedBy(verifierName)
                .message(message)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public IntegrityVerificationResponse getStoredIntegrityInfo(Long evidenceId) {
        EvidenceEntity evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence record not found with ID: " + evidenceId));

        IntegrityStatus status = (evidence.getFileHash() != null && !evidence.getFileHash().isBlank())
                ? IntegrityStatus.VERIFIED : IntegrityStatus.HASH_MISSING;

        return IntegrityVerificationResponse.builder()
                .evidenceId(evidence.getId())
                .evidenceNumber(evidence.getEvidenceNumber())
                .evidenceName(evidence.getEvidenceName())
                .integrityStatus(status)
                .storedHash(evidence.getFileHash())
                .currentHash(null)
                .hashAlgorithm(evidence.getHashAlgorithm() != null ? evidence.getHashAlgorithm() : "SHA-256")
                .verifiedAt(evidence.getUpdatedAt() != null ? evidence.getUpdatedAt() : evidence.getCreatedAt())
                .verifiedBy("System Storage Record")
                .message("Stored cryptographic metadata payload")
                .build();
    }

    @Override
    @Transactional
    public IntegritySummaryReportResponse getIntegritySummaryReport(Pageable pageable) {
        List<EvidenceEntity> allEvidence = evidenceRepository.findAll();
        long totalCount = allEvidence.size();

        long verifiedCount = 0;
        long tamperedCount = 0;
        long fileMissingCount = 0;
        long hashMissingCount = 0;
        long verificationFailedCount = 0;

        List<IntegrityVerificationResponse> responses = new ArrayList<>();

        for (EvidenceEntity entity : allEvidence) {
            IntegrityVerificationResponse response = verifyEvidenceIntegrity(entity.getId(), null);
            responses.add(response);

            switch (response.getIntegrityStatus()) {
                case VERIFIED:
                    verifiedCount++;
                    break;
                case TAMPERED:
                    tamperedCount++;
                    break;
                case FILE_MISSING:
                    fileMissingCount++;
                    break;
                case HASH_MISSING:
                    hashMissingCount++;
                    break;
                case VERIFICATION_FAILED:
                    verificationFailedCount++;
                    break;
            }
        }

        int start = (int) pageable.getOffset();
        int end = Math.min((start + pageable.getPageSize()), responses.size());
        List<IntegrityVerificationResponse> pageContent = (start <= end && start < responses.size())
                ? responses.subList(start, end) : new ArrayList<>();

        Page<IntegrityVerificationResponse> pagedResponses = new PageImpl<>(pageContent, pageable, responses.size());

        return IntegritySummaryReportResponse.builder()
                .totalEvidenceCount(totalCount)
                .verifiedCount(verifiedCount)
                .tamperedCount(tamperedCount)
                .fileMissingCount(fileMissingCount)
                .hashMissingCount(hashMissingCount)
                .verificationFailedCount(verificationFailedCount)
                .records(pagedResponses)
                .build();
    }

    private String generateVerificationNumber() {
        return "VRF-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.ROOT);
    }
}
