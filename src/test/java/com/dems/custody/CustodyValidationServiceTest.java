package com.dems.custody;

import com.dems.enums.EvidenceStatus;
import com.dems.enums.TransferStatus;
import com.dems.evidence.EvidenceEntity;
import com.dems.exception.BadRequestException;
import com.dems.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CustodyValidationServiceTest {

    @Mock
    private CustodyRecordRepository custodyRecordRepository;

    @InjectMocks
    private CustodyValidationService validationService;

    private UserEntity custodian;
    private UserEntity recipient;
    private EvidenceEntity evidence;

    @BeforeEach
    void setUp() {
        custodian = UserEntity.builder().id(1L).email("custodian@dems.gov").active(true).build();
        recipient = UserEntity.builder().id(2L).email("recipient@dems.gov").active(true).build();
        evidence = EvidenceEntity.builder().id(10L).evidenceNumber("EVD-10").currentCustodian(custodian).active(true).status(EvidenceStatus.UPLOADED).build();
    }

    @Test
    void validateInitiateTransfer_Success() {
        when(custodyRecordRepository.existsByEvidence_IdAndTransferStatus(10L, TransferStatus.PENDING)).thenReturn(false);

        assertDoesNotThrow(() -> validationService.validateInitiateTransfer(evidence, custodian, recipient));
    }

    @Test
    void validateInitiateTransfer_ArchivedEvidence_ThrowsBadRequestException() {
        evidence.setStatus(EvidenceStatus.ARCHIVED);

        assertThrows(BadRequestException.class, () -> validationService.validateInitiateTransfer(evidence, custodian, recipient));
    }

    @Test
    void validateInitiateTransfer_SinglePendingRule_ThrowsBadRequestException() {
        when(custodyRecordRepository.existsByEvidence_IdAndTransferStatus(10L, TransferStatus.PENDING)).thenReturn(true);

        assertThrows(BadRequestException.class, () -> validationService.validateInitiateTransfer(evidence, custodian, recipient));
    }

    @Test
    void validateInitiateTransfer_NonCustodian_ThrowsBadRequestException() {
        UserEntity stranger = UserEntity.builder().id(99L).email("stranger@dems.gov").active(true).build();
        when(custodyRecordRepository.existsByEvidence_IdAndTransferStatus(10L, TransferStatus.PENDING)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> validationService.validateInitiateTransfer(evidence, stranger, recipient));
    }

    @Test
    void validateInitiateTransfer_SelfTransfer_ThrowsBadRequestException() {
        when(custodyRecordRepository.existsByEvidence_IdAndTransferStatus(10L, TransferStatus.PENDING)).thenReturn(false);

        assertThrows(BadRequestException.class, () -> validationService.validateInitiateTransfer(evidence, custodian, custodian));
    }
}
