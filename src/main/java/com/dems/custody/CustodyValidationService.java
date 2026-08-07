package com.dems.custody;

import com.dems.enums.EvidenceStatus;
import com.dems.enums.TransferStatus;
import com.dems.evidence.EvidenceEntity;
import com.dems.exception.BadRequestException;
import com.dems.user.UserEntity;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Dedicated validation component checking custody transfer business rules, user ownership, and single-pending-transfer constraints.
 */
@Slf4j
@Component
public class CustodyValidationService {

    private final CustodyRecordRepository custodyRecordRepository;

    public CustodyValidationService(CustodyRecordRepository custodyRecordRepository) {
        this.custodyRecordRepository = custodyRecordRepository;
    }

    public void validateInitiateTransfer(EvidenceEntity evidence, UserEntity transferredBy, UserEntity transferredTo) {
        if (evidence == null || !Boolean.TRUE.equals(evidence.getActive())) {
            log.warn("Custody Validation Failed: Evidence is null or inactive");
            throw new BadRequestException("Target evidence record does not exist or is inactive.");
        }

        if (evidence.getStatus() == EvidenceStatus.ARCHIVED) {
            log.warn("Custody Validation Failed: Evidence ID [{}] is ARCHIVED", evidence.getId());
            throw new BadRequestException("Evidence is archived and cannot undergo custody transfer.");
        }

        // Single pending transfer rule
        if (custodyRecordRepository.existsByEvidence_IdAndTransferStatus(evidence.getId(), TransferStatus.PENDING)) {
            log.warn("Custody Validation Failed: Pending transfer already exists for Evidence ID [{}]", evidence.getId());
            throw new BadRequestException("A pending custody transfer already exists for this evidence item.");
        }

        // Validate current custodian ownership
        UserEntity currentCustodian = evidence.getCurrentCustodian() != null
                ? evidence.getCurrentCustodian() : evidence.getUploadedBy();

        if (currentCustodian == null || !currentCustodian.getId().equals(transferredBy.getId())) {
            log.warn("Custody Validation Failed: User ID [{}] is not current custodian for Evidence ID [{}]",
                    transferredBy.getId(), evidence.getId());
            throw new BadRequestException("Only the current evidence custodian can initiate a custody transfer.");
        }

        // Validate recipient eligibility
        if (transferredTo == null || !transferredTo.isEnabled()) {
            log.warn("Custody Validation Failed: Target recipient is null or deactivated");
            throw new BadRequestException("Target transfer recipient account does not exist or is deactivated.");
        }

        if (transferredTo.getId().equals(transferredBy.getId())) {
            log.warn("Custody Validation Failed: Self-transfer attempted by User ID [{}]", transferredBy.getId());
            throw new BadRequestException("Transferred recipient cannot be the same user as transfer initiator.");
        }
    }

    public void validateAcceptOrReject(CustodyRecordEntity custodyRecord, UserEntity recipient) {
        if (custodyRecord == null || !Boolean.TRUE.equals(custodyRecord.getActive())) {
            throw new BadRequestException("Target custody transfer record does not exist or is inactive.");
        }

        if (custodyRecord.getTransferStatus() != TransferStatus.PENDING) {
            log.warn("Custody Validation Failed: Record ID [{}] is in status [{}] instead of PENDING",
                    custodyRecord.getId(), custodyRecord.getTransferStatus());
            throw new BadRequestException("Custody transfer record is not in PENDING state.");
        }

        if (recipient == null || !custodyRecord.getTransferredTo().getId().equals(recipient.getId())) {
            log.warn("Custody Validation Failed: User ID [{}] is not designated recipient for Custody ID [{}]",
                    recipient != null ? recipient.getId() : null, custodyRecord.getId());
            throw new BadRequestException("Only the designated transfer recipient can accept or reject this custody transfer.");
        }
    }
}
