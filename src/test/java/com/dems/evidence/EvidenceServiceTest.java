package com.dems.evidence;

import com.dems.cases.CaseEntity;
import com.dems.cases.CaseRepository;
import com.dems.enums.CaseStatus;
import com.dems.enums.EvidenceStatus;
import com.dems.enums.EvidenceType;
import com.dems.enums.UserRole;
import com.dems.storage.StorageService;
import com.dems.user.UserEntity;
import com.dems.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.mock.web.MockMultipartFile;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvidenceServiceTest {

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private StorageService storageService;

    @Mock
    private com.dems.integrity.EvidenceIntegrityService integrityService;

    @Spy
    private EvidenceValidationService validationService = new EvidenceValidationService();

    @Mock
    private EvidenceMapper evidenceMapper;

    @Mock
    private com.dems.audit.AuditService auditService;

    @Mock
    private com.dems.qr.QRCodeService qrCodeService;

    @InjectMocks
    private EvidenceServiceImpl evidenceService;

    private UserEntity officer;
    private CaseEntity caseEntity;
    private EvidenceEntity evidenceEntity;
    private EvidenceResponse evidenceResponse;
    private EvidenceUploadRequest uploadRequest;
    private MockMultipartFile mockFile;

    @BeforeEach
    void setUp() {
        officer = UserEntity.builder()
                .id(2L)
                .email("officer@dems.gov")
                .fullName("Officer John")
                .role(UserRole.POLICE_OFFICER)
                .active(true)
                .build();

        caseEntity = CaseEntity.builder()
                .id(10L)
                .caseNumber("CASE-2026-001")
                .caseName("Burglary Case")
                .status(CaseStatus.OPEN)
                .active(true)
                .build();

        uploadRequest = EvidenceUploadRequest.builder()
                .caseId(10L)
                .evidenceName("CCTV Recording")
                .evidenceType(EvidenceType.VIDEO)
                .build();

        mockFile = new MockMultipartFile("file", "cctv.mp4", "video/mp4", "video bytes".getBytes());

        evidenceEntity = EvidenceEntity.builder()
                .id(100L)
                .evidenceNumber("EVD-100")
                .evidenceName("CCTV Recording")
                .evidenceType(EvidenceType.VIDEO)
                .status(EvidenceStatus.UPLOADED)
                .storagePath("uploads/cases/CASE-2026-001/cctv.mp4")
                .downloadCount(0L)
                .caseEntity(caseEntity)
                .uploadedBy(officer)
                .active(true)
                .build();

        evidenceResponse = EvidenceResponse.builder()
                .id(100L)
                .evidenceNumber("EVD-100")
                .evidenceName("CCTV Recording")
                .status(EvidenceStatus.UPLOADED)
                .downloadCount(0L)
                .build();
    }

    @Test
    void uploadEvidence_Success() {
        when(userRepository.findByEmail("officer@dems.gov")).thenReturn(Optional.of(officer));
        when(caseRepository.findById(10L)).thenReturn(Optional.of(caseEntity));
        when(storageService.storeFile(eq(mockFile), eq("CASE-2026-001"))).thenReturn("uploads/cases/CASE-2026-001/cctv.mp4");
        when(evidenceRepository.save(any(EvidenceEntity.class))).thenReturn(evidenceEntity);
        when(evidenceMapper.toResponse(evidenceEntity)).thenReturn(evidenceResponse);

        EvidenceResponse result = evidenceService.uploadEvidence(mockFile, uploadRequest, "officer@dems.gov");

        assertNotNull(result);
        assertEquals("EVD-100", result.getEvidenceNumber());
        verify(storageService).storeFile(eq(mockFile), eq("CASE-2026-001"));
        verify(evidenceRepository, org.mockito.Mockito.times(2)).save(any(EvidenceEntity.class));
    }

    @Test
    void downloadEvidence_IncrementsDownloadCount() {
        Resource mockResource = new ByteArrayResource("test payload".getBytes());
        when(evidenceRepository.findById(100L)).thenReturn(Optional.of(evidenceEntity));
        when(storageService.loadFileAsResource("uploads/cases/CASE-2026-001/cctv.mp4")).thenReturn(mockResource);

        Resource result = evidenceService.downloadEvidence(100L);

        assertNotNull(result);
        assertEquals(1L, evidenceEntity.getDownloadCount());
        verify(evidenceRepository).save(evidenceEntity);
    }
}
