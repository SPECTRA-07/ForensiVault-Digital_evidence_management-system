package com.dems.custody;

import com.dems.audit.AuditService;
import com.dems.enums.AuditAction;
import com.dems.enums.AuditEntityType;
import com.dems.enums.AuditStatus;
import com.dems.enums.IntegrityStatus;
import com.dems.enums.TransferPurpose;
import com.dems.enums.TransferStatus;
import com.dems.evidence.EvidenceEntity;
import com.dems.evidence.EvidenceRepository;
import com.dems.exception.ResourceNotFoundException;
import com.dems.integrity.EvidenceIntegrityService;
import com.dems.integrity.IntegrityVerificationResponse;
import com.dems.user.UserEntity;
import com.dems.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

/**
 * Service implementation managing legally auditable evidence custody transfers, handshakes, timeline history, and audit logging.
 */
@Slf4j
@Service
public class CustodyServiceImpl implements CustodyService {

    private final CustodyRecordRepository custodyRepository;
    private final EvidenceRepository evidenceRepository;
    private final UserRepository userRepository;
    private final CustodyValidationService validationService;
    private final EvidenceIntegrityService integrityService;
    private final CustodyMapper custodyMapper;
    private final AuditService auditService;

    public CustodyServiceImpl(
            CustodyRecordRepository custodyRepository,
            EvidenceRepository evidenceRepository,
            UserRepository userRepository,
            CustodyValidationService validationService,
            EvidenceIntegrityService integrityService,
            CustodyMapper custodyMapper,
            AuditService auditService) {
        this.custodyRepository = custodyRepository;
        this.evidenceRepository = evidenceRepository;
        this.userRepository = userRepository;
        this.validationService = validationService;
        this.integrityService = integrityService;
        this.custodyMapper = custodyMapper;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public CustodyResponse initiateTransfer(CustodyTransferRequest request, String initiatorEmail) {
        UserEntity initiator = userRepository.findByEmail(initiatorEmail.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + initiatorEmail));

        UserEntity recipient = userRepository.findById(request.getTransferredToId())
                .orElseThrow(() -> new ResourceNotFoundException("Recipient user not found with ID: " + request.getTransferredToId()));

        EvidenceEntity evidence = evidenceRepository.findById(request.getEvidenceId())
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found with ID: " + request.getEvidenceId()));

        // Validate transfer eligibility, ownership, and single-pending-transfer constraint
        validationService.validateInitiateTransfer(evidence, initiator, recipient);

        // Capture integrity status snapshot
        IntegrityStatus integritySnapshot = IntegrityStatus.VERIFIED;
        try {
            IntegrityVerificationResponse integrityResponse = integrityService.getStoredIntegrityInfo(evidence.getId());
            integritySnapshot = integrityResponse.getIntegrityStatus();
        } catch (Exception e) {
            log.warn("Could not fetch integrity snapshot for Evidence ID [{}] during custody transfer", evidence.getId(), e);
        }

        int sequence = custodyRepository.countByEvidence_Id(evidence.getId()) + 1;
        String custodyNumber = generateCustodyNumber();

        CustodyRecordEntity record = CustodyRecordEntity.builder()
                .custodyNumber(custodyNumber)
                .custodySequence(sequence)
                .evidence(evidence)
                .transferredBy(initiator)
                .transferredTo(recipient)
                .transferStatus(TransferStatus.PENDING)
                .transferPurpose(request.getTransferPurpose())
                .transferLocation(request.getTransferLocation().trim())
                .integrityStatusAtTransfer(integritySnapshot)
                .transferredAt(OffsetDateTime.now())
                .transferRemarks(request.getTransferRemarks())
                .active(true)
                .build();

        CustodyRecordEntity savedRecord = custodyRepository.save(record);

        log.info("Transfer Initiated: Custody Number [{}], Evidence [{}], From [{}], To [{}]",
                custodyNumber, evidence.getEvidenceNumber(), initiator.getEmployeeId(), recipient.getEmployeeId());

        auditService.recordEvent(
                AuditAction.TRANSFER,
                AuditEntityType.CUSTODY,
                "CUSTODY",
                custodyNumber,
                savedRecord.getId(),
                AuditStatus.SUCCESS,
                "Initiated custody transfer of evidence " + evidence.getEvidenceNumber() + " to " + recipient.getEmail()
        );

        return custodyMapper.toResponse(savedRecord);
    }

    @Override
    @Transactional
    public CustodyResponse acceptOrRejectTransfer(Long custodyId, CustodyAcceptRequest request, String recipientEmail) {
        CustodyRecordEntity custodyRecord = custodyRepository.findById(custodyId)
                .orElseThrow(() -> new ResourceNotFoundException("Custody transfer record not found with ID: " + custodyId));

        UserEntity recipient = userRepository.findByEmail(recipientEmail.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + recipientEmail));

        // Validate transfer status state and recipient matching
        validationService.validateAcceptOrReject(custodyRecord, recipient);

        OffsetDateTime now = OffsetDateTime.now();
        EvidenceEntity evidence = custodyRecord.getEvidence();

        if (Boolean.TRUE.equals(request.getAccepted())) {
            custodyRecord.setTransferStatus(TransferStatus.ACCEPTED);
            custodyRecord.setAcceptedAt(now);
            custodyRecord.setAcceptanceRemarks(request.getAcceptanceRemarks());

            // Handshake complete: update current custodian on evidence record
            evidence.setCurrentCustodian(recipient);
            evidence.setLastTransferredAt(now);
            evidenceRepository.save(evidence);

            log.info("Transfer Accepted: Custody Number [{}], New Custodian ID [{}], Employee ID [{}]",
                    custodyRecord.getCustodyNumber(), recipient.getId(), recipient.getEmployeeId());

            auditService.recordEventWithDiff(
                    AuditAction.TRANSFER,
                    AuditEntityType.CUSTODY,
                    "CUSTODY",
                    custodyRecord.getCustodyNumber(),
                    custodyRecord.getId(),
                    "custodian=" + custodyRecord.getTransferredBy().getEmail(),
                    "custodian=" + recipient.getEmail(),
                    AuditStatus.SUCCESS,
                    "Accepted custody transfer handshake for evidence: " + evidence.getEvidenceNumber()
            );
        } else {
            custodyRecord.setTransferStatus(TransferStatus.REJECTED);
            custodyRecord.setAcceptedAt(now);
            custodyRecord.setAcceptanceRemarks(request.getAcceptanceRemarks());

            log.warn("Transfer Rejected: Custody Number [{}], Recipient ID [{}]",
                    custodyRecord.getCustodyNumber(), recipient.getId());

            auditService.recordEvent(
                    AuditAction.TRANSFER,
                    AuditEntityType.CUSTODY,
                    "CUSTODY",
                    custodyRecord.getCustodyNumber(),
                    custodyRecord.getId(),
                    AuditStatus.WARNING,
                    "Rejected custody transfer handshake for evidence: " + evidence.getEvidenceNumber()
            );
        }

        CustodyRecordEntity updatedRecord = custodyRepository.save(custodyRecord);
        return custodyMapper.toResponse(updatedRecord);
    }

    @Override
    @Transactional(readOnly = true)
    public CustodyResponse getCustodyById(Long id) {
        CustodyRecordEntity record = custodyRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Custody record not found with ID: " + id));
        return custodyMapper.toResponse(record);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustodyResponse> getCustodyByEvidenceId(Long evidenceId, Pageable pageable) {
        if (!evidenceRepository.existsById(evidenceId)) {
            throw new ResourceNotFoundException("Evidence not found with ID: " + evidenceId);
        }
        return custodyRepository.findByEvidence_Id(evidenceId, pageable)
                .map(custodyMapper::toResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public CustodyTimelineResponse getCustodyTimeline(Long evidenceId) {
        EvidenceEntity evidence = evidenceRepository.findById(evidenceId)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence not found with ID: " + evidenceId));

        List<CustodyRecordEntity> history = custodyRepository.findByEvidence_IdOrderByCustodySequenceAsc(evidenceId);
        List<CustodyResponse> timeline = history.stream().map(custodyMapper::toResponse).toList();

        UserEntity custodian = evidence.getCurrentCustodian() != null ? evidence.getCurrentCustodian() : evidence.getUploadedBy();
        String custodianName = custodian != null ? custodian.getFullName() : "Unknown";
        Long custodianId = custodian != null ? custodian.getId() : null;

        return CustodyTimelineResponse.builder()
                .evidenceId(evidence.getId())
                .evidenceNumber(evidence.getEvidenceNumber())
                .evidenceName(evidence.getEvidenceName())
                .currentCustodianName(custodianName)
                .currentCustodianId(custodianId)
                .totalTransfers(timeline.size())
                .timeline(timeline)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CustodyResponse> searchCustodyRecords(
            Long evidenceId,
            Long transferredById,
            Long transferredToId,
            TransferStatus transferStatus,
            TransferPurpose transferPurpose,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            Boolean active,
            Pageable pageable
    ) {
        Specification<CustodyRecordEntity> spec = CustodyRecordSpecification.filterCustody(
                evidenceId, transferredById, transferredToId, transferStatus, transferPurpose, startDate, endDate, active
        );

        return custodyRepository.findAll(spec, pageable)
                .map(custodyMapper::toResponse);
    }

    private String generateCustodyNumber() {
        return "CUST-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.ROOT);
    }
}
