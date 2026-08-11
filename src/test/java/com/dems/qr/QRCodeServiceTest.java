package com.dems.qr;

import com.dems.audit.AuditService;
import com.dems.enums.EvidenceStatus;
import com.dems.enums.IntegrityStatus;
import com.dems.evidence.EvidenceEntity;
import com.dems.evidence.EvidenceRepository;
import com.dems.exception.BadRequestException;
import com.dems.integrity.EvidenceIntegrityService;
import com.dems.integrity.IntegrityVerificationResponse;
import com.dems.user.UserEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class QRCodeServiceTest {

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private EvidenceIntegrityService integrityService;

    @Mock
    private AuditService auditService;

    @Mock
    private com.dems.storage.StorageService storageService;

    @InjectMocks
    private QRCodeServiceImpl qrCodeService;

    private EvidenceEntity evidence;
    private UserEntity custodian;

    @BeforeEach
    void setUp() {
        custodian = UserEntity.builder().id(1L).email("officer@dems.gov").fullName("Officer John").build();

        evidence = EvidenceEntity.builder()
                .id(100L)
                .evidenceNumber("EVD-100")
                .evidenceName("CCTV Recording")
                .status(EvidenceStatus.UPLOADED)
                .currentCustodian(custodian)
                .active(true)
                .build();
    }

    @Test
    void generateQRCode_Success() {
        when(evidenceRepository.save(any(EvidenceEntity.class))).thenReturn(evidence);
        when(storageService.storeFile(any(byte[].class), any(String.class), any(String.class))).thenReturn("qr/QR-EVD-100.png");

        QRCodeResponse response = qrCodeService.generateQRCode(evidence);

        assertNotNull(response);
        assertEquals(100L, response.getEvidenceId());
        assertEquals("EVD-100", response.getEvidenceNumber());
        assertEquals("QR-EVD-100.png", response.getQrFileName());
        assertEquals("/qr/evidence/100/image", response.getQrDownloadUrl());
        verify(auditService).recordEvent(any(), any(), any(), any(), any(), any(), any());
    }

    @Test
    void generateQRCode_ArchivedEvidence_ThrowsBadRequestException() {
        evidence.setStatus(EvidenceStatus.ARCHIVED);

        assertThrows(BadRequestException.class, () -> qrCodeService.generateQRCode(evidence));
    }

    @Test
    void resolveQRCode_Success() {
        when(evidenceRepository.findByEvidenceNumber("EVD-100")).thenReturn(Optional.of(evidence));
        when(integrityService.getStoredIntegrityInfo(100L))
                .thenReturn(IntegrityVerificationResponse.builder().integrityStatus(IntegrityStatus.VERIFIED).build());

        QRResolveResponse response = qrCodeService.resolveQRCode("EVD-100");

        assertNotNull(response);
        assertEquals(100L, response.getEvidenceId());
        assertEquals("EVD-100", response.getEvidenceNumber());
        assertEquals("Officer John", response.getCurrentCustodian());
        assertEquals(IntegrityStatus.VERIFIED, response.getIntegrityStatus());
    }
}
