package com.dems.evidence;

import com.dems.user.UserMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper component converting Evidence entities to response DTOs.
 */
@Component
public class EvidenceMapper {

    private final UserMapper userMapper;

    public EvidenceMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public EvidenceResponse toResponse(EvidenceEntity entity) {
        if (entity == null) {
            return null;
        }
        return EvidenceResponse.builder()
                .id(entity.getId())
                .evidenceNumber(entity.getEvidenceNumber())
                .evidenceName(entity.getEvidenceName())
                .displayName(entity.getDisplayName())
                .description(entity.getDescription())
                .remarks(entity.getRemarks())
                .evidenceType(entity.getEvidenceType())
                .status(entity.getStatus())
                .originalFileName(entity.getOriginalFileName())
                .storedFileName(entity.getStoredFileName())
                .fileExtension(entity.getFileExtension())
                .fileSize(entity.getFileSize())
                .mimeType(entity.getMimeType())
                .storagePath(entity.getStoragePath())
                .collectedFrom(entity.getCollectedFrom())
                .collectionMethod(entity.getCollectionMethod())
                .collectedAt(entity.getCollectedAt())
                .collectedBy(entity.getCollectedBy())
                .downloadCount(entity.getDownloadCount())
                .fileHash(entity.getFileHash())
                .hashAlgorithm(entity.getHashAlgorithm())
                .qrFileName(entity.getQrFileName())
                .qrDownloadUrl(entity.getQrDownloadUrl())
                .qrGeneratedAt(entity.getQrGeneratedAt())
                .uploadedAt(entity.getUploadedAt())
                .uploadedBy(userMapper.toResponse(entity.getUploadedBy()))
                .currentCustodian(userMapper.toResponse(entity.getCurrentCustodian()))
                .lastTransferredAt(entity.getLastTransferredAt())
                .caseId(entity.getCaseEntity() != null ? entity.getCaseEntity().getId() : null)
                .caseNumber(entity.getCaseEntity() != null ? entity.getCaseEntity().getCaseNumber() : null)
                .caseName(entity.getCaseEntity() != null ? entity.getCaseEntity().getCaseName() : null)
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .updatedAt(entity.getUpdatedAt())
                .build();
    }

    public EvidenceSummaryResponse toSummaryResponse(EvidenceEntity entity) {
        if (entity == null) {
            return null;
        }
        String uploaderName = entity.getUploadedBy() != null ? entity.getUploadedBy().getFullName() : "Unknown";
        Long uploaderId = entity.getUploadedBy() != null ? entity.getUploadedBy().getId() : null;
        String custodianName = entity.getCurrentCustodian() != null ? entity.getCurrentCustodian().getFullName() : uploaderName;
        Long custodianId = entity.getCurrentCustodian() != null ? entity.getCurrentCustodian().getId() : uploaderId;

        return EvidenceSummaryResponse.builder()
                .id(entity.getId())
                .evidenceNumber(entity.getEvidenceNumber())
                .evidenceName(entity.getEvidenceName())
                .displayName(entity.getDisplayName())
                .evidenceType(entity.getEvidenceType())
                .status(entity.getStatus())
                .originalFileName(entity.getOriginalFileName())
                .fileSize(entity.getFileSize())
                .mimeType(entity.getMimeType())
                .caseId(entity.getCaseEntity() != null ? entity.getCaseEntity().getId() : null)
                .caseNumber(entity.getCaseEntity() != null ? entity.getCaseEntity().getCaseNumber() : null)
                .uploadedByName(uploaderName)
                .uploadedById(uploaderId)
                .currentCustodianName(custodianName)
                .currentCustodianId(custodianId)
                .uploadedAt(entity.getUploadedAt())
                .downloadCount(entity.getDownloadCount())
                .active(entity.getActive())
                .build();
    }
}
