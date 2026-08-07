package com.dems.custody;

import com.dems.enums.IntegrityStatus;
import com.dems.enums.TransferPurpose;
import com.dems.enums.TransferStatus;
import com.dems.evidence.EvidenceEntity;
import com.dems.evidence.EvidenceRepository;
import com.dems.integrity.EvidenceIntegrityService;
import com.dems.integrity.IntegrityVerificationResponse;
import com.dems.user.UserEntity;
import com.dems.user.UserRepository;
import com.dems.user.UserResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustodyServiceTest {

    @Mock
    private CustodyRecordRepository custodyRepository;

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CustodyValidationService validationService;

    @Mock
    private EvidenceIntegrityService integrityService;

    @Mock
    private CustodyMapper custodyMapper;

    @Mock
    private com.dems.audit.AuditService auditService;

    @InjectMocks
    private CustodyServiceImpl custodyService;

    private UserEntity initiator;
    private UserEntity recipient;
    private EvidenceEntity evidence;
    private CustodyRecordEntity custodyRecord;
    private CustodyResponse custodyResponse;

    @BeforeEach
    void setUp() {
        initiator = UserEntity.builder().id(1L).email("initiator@dems.gov").fullName("Officer Initiator").build();
        recipient = UserEntity.builder().id(2L).email("recipient@dems.gov").fullName("Officer Recipient").build();
        evidence = EvidenceEntity.builder().id(10L).evidenceNumber("EVD-10").evidenceName("Laptop").currentCustodian(initiator).active(true).build();

        custodyRecord = CustodyRecordEntity.builder()
                .id(100L)
                .custodyNumber("CUST-100")
                .custodySequence(1)
                .evidence(evidence)
                .transferredBy(initiator)
                .transferredTo(recipient)
                .transferStatus(TransferStatus.PENDING)
                .transferPurpose(TransferPurpose.FORENSIC_ANALYSIS)
                .transferLocation("Lab")
                .transferredAt(OffsetDateTime.now())
                .active(true)
                .build();

        custodyResponse = CustodyResponse.builder()
                .id(100L)
                .custodyNumber("CUST-100")
                .custodySequence(1)
                .evidenceId(10L)
                .transferStatus(TransferStatus.PENDING)
                .build();
    }

    @Test
    void initiateTransfer_Success() {
        CustodyTransferRequest request = CustodyTransferRequest.builder()
                .evidenceId(10L)
                .transferredToId(2L)
                .transferPurpose(TransferPurpose.FORENSIC_ANALYSIS)
                .transferLocation("Forensic Lab Room 101")
                .transferRemarks("Sending for disk dump")
                .build();

        when(userRepository.findByEmail("initiator@dems.gov")).thenReturn(Optional.of(initiator));
        when(userRepository.findById(2L)).thenReturn(Optional.of(recipient));
        when(evidenceRepository.findById(10L)).thenReturn(Optional.of(evidence));
        when(integrityService.getStoredIntegrityInfo(10L))
                .thenReturn(IntegrityVerificationResponse.builder().integrityStatus(IntegrityStatus.VERIFIED).build());
        when(custodyRepository.countByEvidence_Id(10L)).thenReturn(0);
        when(custodyRepository.save(any(CustodyRecordEntity.class))).thenReturn(custodyRecord);
        when(custodyMapper.toResponse(custodyRecord)).thenReturn(custodyResponse);

        CustodyResponse response = custodyService.initiateTransfer(request, "initiator@dems.gov");

        assertNotNull(response);
        assertEquals("CUST-100", response.getCustodyNumber());
        verify(custodyRepository).save(any(CustodyRecordEntity.class));
    }

    @Test
    void acceptTransfer_UpdatesEvidenceCustodian() {
        CustodyAcceptRequest acceptRequest = CustodyAcceptRequest.builder().accepted(true).acceptanceRemarks("Received in good order").build();

        when(custodyRepository.findById(100L)).thenReturn(Optional.of(custodyRecord));
        when(userRepository.findByEmail("recipient@dems.gov")).thenReturn(Optional.of(recipient));
        when(custodyRepository.save(custodyRecord)).thenReturn(custodyRecord);

        custodyResponse.setTransferStatus(TransferStatus.ACCEPTED);
        when(custodyMapper.toResponse(custodyRecord)).thenReturn(custodyResponse);

        CustodyResponse response = custodyService.acceptOrRejectTransfer(100L, acceptRequest, "recipient@dems.gov");

        assertNotNull(response);
        assertEquals(TransferStatus.ACCEPTED, response.getTransferStatus());
        assertEquals(recipient, evidence.getCurrentCustodian());
        verify(evidenceRepository).save(evidence);
    }

    @Test
    void getCustodyTimeline_Success() {
        when(evidenceRepository.findById(10L)).thenReturn(Optional.of(evidence));
        when(custodyRepository.findByEvidence_IdOrderByCustodySequenceAsc(10L)).thenReturn(List.of(custodyRecord));
        when(custodyMapper.toResponse(custodyRecord)).thenReturn(custodyResponse);

        CustodyTimelineResponse response = custodyService.getCustodyTimeline(10L);

        assertNotNull(response);
        assertEquals(10L, response.getEvidenceId());
        assertEquals(1, response.getTotalTransfers());
        assertEquals("Officer Initiator", response.getCurrentCustodianName());
    }
}
