package com.dems.custody;

import com.dems.user.UserMapper;
import org.springframework.stereotype.Component;

/**
 * Mapper component converting CustodyRecordEntity to response DTOs.
 */
@Component
public class CustodyMapper {

    private final UserMapper userMapper;

    public CustodyMapper(UserMapper userMapper) {
        this.userMapper = userMapper;
    }

    public CustodyResponse toResponse(CustodyRecordEntity entity) {
        if (entity == null) {
            return null;
        }
        return CustodyResponse.builder()
                .id(entity.getId())
                .custodyNumber(entity.getCustodyNumber())
                .custodySequence(entity.getCustodySequence())
                .evidenceId(entity.getEvidence() != null ? entity.getEvidence().getId() : null)
                .evidenceNumber(entity.getEvidence() != null ? entity.getEvidence().getEvidenceNumber() : null)
                .evidenceName(entity.getEvidence() != null ? entity.getEvidence().getEvidenceName() : null)
                .transferredBy(userMapper.toResponse(entity.getTransferredBy()))
                .transferredTo(userMapper.toResponse(entity.getTransferredTo()))
                .transferStatus(entity.getTransferStatus())
                .transferPurpose(entity.getTransferPurpose())
                .transferLocation(entity.getTransferLocation())
                .integrityStatusAtTransfer(entity.getIntegrityStatusAtTransfer())
                .transferredAt(entity.getTransferredAt())
                .acceptedAt(entity.getAcceptedAt())
                .transferRemarks(entity.getTransferRemarks())
                .acceptanceRemarks(entity.getAcceptanceRemarks())
                .active(entity.getActive())
                .createdAt(entity.getCreatedAt())
                .build();
    }
}
