package com.dems.qr;

import com.dems.audit.AuditService;
import com.dems.enums.AuditAction;
import com.dems.enums.AuditEntityType;
import com.dems.enums.AuditStatus;
import com.dems.enums.EvidenceStatus;
import com.dems.enums.IntegrityStatus;
import com.dems.evidence.EvidenceEntity;
import com.dems.evidence.EvidenceRepository;
import com.dems.exception.BadRequestException;
import com.dems.exception.InternalServerException;
import com.dems.exception.ResourceNotFoundException;
import com.dems.integrity.EvidenceIntegrityService;
import com.dems.integrity.IntegrityVerificationResponse;
import com.dems.storage.StorageService;
import com.dems.user.UserEntity;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.client.j2se.MatrixToImageWriter;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.ByteArrayOutputStream;
import java.time.OffsetDateTime;

/**
 * Implementation of QRCodeService handling ZXing QR image generation, StorageService integration,
 * metadata resolution, and audit logging. Supports local filesystem and cloud object storage transparently.
 */
@Slf4j
@Service
public class QRCodeServiceImpl implements QRCodeService {

    private final EvidenceRepository evidenceRepository;
    private final EvidenceIntegrityService integrityService;
    private final AuditService auditService;
    private final StorageService storageService;

    public QRCodeServiceImpl(
            EvidenceRepository evidenceRepository,
            EvidenceIntegrityService integrityService,
            AuditService auditService,
            StorageService storageService) {
        this.evidenceRepository = evidenceRepository;
        this.integrityService = integrityService;
        this.auditService = auditService;
        this.storageService = storageService;
    }

    @Override
    @Transactional
    public QRCodeResponse generateQRCode(EvidenceEntity evidence) {
        validateEvidenceEligibility(evidence);

        try {
            String fileName = "QR-" + evidence.getEvidenceNumber() + ".png";

            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(evidence.getEvidenceNumber(), BarcodeFormat.QR_CODE, 250, 250);

            ByteArrayOutputStream pngOutputStream = new ByteArrayOutputStream();
            MatrixToImageWriter.writeToStream(bitMatrix, "PNG", pngOutputStream);
            byte[] pngBytes = pngOutputStream.toByteArray();

            // Store QR image using pluggable StorageService under "qr" subdirectory / prefix
            String storedPath = storageService.storeFile(pngBytes, fileName, "qr");

            OffsetDateTime now = OffsetDateTime.now();
            String downloadUrl = "/qr/evidence/" + evidence.getId() + "/image";

            evidence.setQrFileName(fileName);
            evidence.setQrDownloadUrl(downloadUrl);
            evidence.setQrGeneratedAt(now);
            evidenceRepository.save(evidence);

            log.info("QR Code Generated: Evidence ID [{}], Storage Path [{}]", evidence.getId(), storedPath);

            auditService.recordEvent(
                    AuditAction.CREATE,
                    AuditEntityType.EVIDENCE,
                    "QR",
                    evidence.getEvidenceNumber(),
                    evidence.getId(),
                    AuditStatus.SUCCESS,
                    "Generated physical barcode QR Code for evidence: " + evidence.getEvidenceNumber()
            );

            return QRCodeResponse.builder()
                    .evidenceId(evidence.getId())
                    .evidenceNumber(evidence.getEvidenceNumber())
                    .qrFileName(fileName)
                    .qrDownloadUrl(downloadUrl)
                    .generatedAt(now)
                    .build();

        } catch (Exception e) {
            log.error("Failed to generate QR Code for Evidence ID [{}]", evidence.getId(), e);
            throw new InternalServerException("Failed to generate QR Code image: " + e.getMessage());
        }
    }

    @Override
    @Transactional
    public QRCodeResponse regenerateQRCode(Long evidenceId) {
        EvidenceEntity evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence record not found with ID: " + evidenceId));

        validateEvidenceEligibility(evidence);

        // Delete existing QR file if present
        if (evidence.getQrFileName() != null) {
            try {
                storageService.deleteFile(getQRStorageKey(evidence.getQrFileName()));
            } catch (Exception e) {
                log.warn("Could not delete previous QR file for Evidence ID [{}]", evidenceId, e);
            }
        }

        QRCodeResponse response = generateQRCode(evidence);

        auditService.recordEvent(
                AuditAction.UPDATE,
                AuditEntityType.EVIDENCE,
                "QR",
                evidence.getEvidenceNumber(),
                evidence.getId(),
                AuditStatus.SUCCESS,
                "Regenerated QR Code image for evidence: " + evidence.getEvidenceNumber()
        );

        return response;
    }

    @Override
    @Transactional
    public QRCodeResponse getQRCodeInfo(Long evidenceId) {
        EvidenceEntity evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence record not found with ID: " + evidenceId));

        if (evidence.getQrFileName() == null || evidence.getQrDownloadUrl() == null) {
            // Auto-generate if missing
            return generateQRCode(evidence);
        }

        return QRCodeResponse.builder()
                .evidenceId(evidence.getId())
                .evidenceNumber(evidence.getEvidenceNumber())
                .qrFileName(evidence.getQrFileName())
                .qrDownloadUrl(evidence.getQrDownloadUrl())
                .generatedAt(evidence.getQrGeneratedAt())
                .build();
    }

    @Override
    @Transactional
    public Resource getQRCodeImageResource(Long evidenceId) {
        EvidenceEntity evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence record not found with ID: " + evidenceId));

        String fileName = evidence.getQrFileName();
        if (fileName == null) {
            QRCodeResponse generated = generateQRCode(evidence);
            fileName = generated.getQrFileName();
        }

        try {
            String storageKey = getQRStorageKey(fileName);
            return storageService.loadFileAsResource(storageKey);
        } catch (Exception e) {
            log.warn("QR image resource missing for Evidence ID [{}], auto-regenerating...", evidenceId);
            QRCodeResponse regenerated = generateQRCode(evidence);
            return storageService.loadFileAsResource(getQRStorageKey(regenerated.getQrFileName()));
        }
    }

    @Override
    @Transactional(readOnly = true)
    public QRResolveResponse resolveQRCode(String evidenceNumber) {
        EvidenceEntity evidence = evidenceRepository.findByEvidenceNumber(evidenceNumber.trim())
                .orElseThrow(() -> new ResourceNotFoundException("Evidence record not found for evidence number: " + evidenceNumber));

        if (!Boolean.TRUE.equals(evidence.getActive())) {
            throw new BadRequestException("Target evidence record is inactive.");
        }

        IntegrityStatus integritySnapshot = IntegrityStatus.VERIFIED;
        try {
            IntegrityVerificationResponse integrityResponse = integrityService.getStoredIntegrityInfo(evidence.getId());
            integritySnapshot = integrityResponse.getIntegrityStatus();
        } catch (Exception e) {
            log.warn("Could not fetch integrity info during QR resolution for Evidence ID [{}]", evidence.getId(), e);
        }

        UserEntity custodian = evidence.getCurrentCustodian() != null ? evidence.getCurrentCustodian() : evidence.getUploadedBy();
        String custodianName = custodian != null ? custodian.getFullName() : "Unknown";
        String caseNumber = evidence.getCaseEntity() != null ? evidence.getCaseEntity().getCaseNumber() : "N/A";

        auditService.recordEvent(
                AuditAction.VIEW,
                AuditEntityType.EVIDENCE,
                "QR",
                evidence.getEvidenceNumber(),
                evidence.getId(),
                AuditStatus.SUCCESS,
                "Resolved QR code barcode scanner check for evidence: " + evidence.getEvidenceNumber()
        );

        return QRResolveResponse.builder()
                .evidenceId(evidence.getId())
                .evidenceNumber(evidence.getEvidenceNumber())
                .caseNumber(caseNumber)
                .evidenceName(evidence.getEvidenceName())
                .evidenceStatus(evidence.getStatus())
                .currentCustodian(custodianName)
                .integrityStatus(integritySnapshot)
                .qrGeneratedAt(evidence.getQrGeneratedAt())
                .build();
    }

    private String getQRStorageKey(String fileName) {
        if (fileName.contains("/") || fileName.contains("\\")) {
            return fileName;
        }
        return "qr/" + fileName;
    }

    private void validateEvidenceEligibility(EvidenceEntity evidence) {
        if (evidence == null || !Boolean.TRUE.equals(evidence.getActive())) {
            throw new BadRequestException("Evidence record does not exist or is inactive.");
        }
        if (evidence.getStatus() == EvidenceStatus.ARCHIVED) {
            throw new BadRequestException("Cannot generate QR code for archived evidence.");
        }
    }
}
