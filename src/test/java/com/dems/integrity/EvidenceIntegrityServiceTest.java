package com.dems.integrity;

import com.dems.enums.IntegrityStatus;
import com.dems.evidence.EvidenceEntity;
import com.dems.evidence.EvidenceRepository;
import com.dems.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.api.io.TempDir;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class EvidenceIntegrityServiceTest {

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EvidenceVerificationHistoryRepository historyRepository;

    @Mock
    private HashService hashService;

    @Mock
    private com.dems.audit.AuditService auditService;

    @InjectMocks
    private EvidenceIntegrityServiceImpl integrityService;

    @TempDir
    Path tempDir;

    private EvidenceEntity evidenceEntity;

    @BeforeEach
    void setUp() throws IOException {
        Path dummyFile = tempDir.resolve("sample.txt");
        Files.writeString(dummyFile, "sample content");

        evidenceEntity = EvidenceEntity.builder()
                .id(100L)
                .evidenceNumber("EVD-100")
                .evidenceName("Sample Evidence")
                .fileHash("b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9")
                .hashAlgorithm("SHA-256")
                .storagePath(dummyFile.toString())
                .active(true)
                .build();
    }

    @Test
    void verifyEvidenceIntegrity_Verified() {
        when(evidenceRepository.findById(100L)).thenReturn(Optional.of(evidenceEntity));
        when(hashService.generateHash(any(Path.class)))
                .thenReturn("b94d27b9934d3e08a52e52d7da7dabfac484efe37a5380ee9088f7ace2efcde9");

        IntegrityVerificationResponse result = integrityService.verifyEvidenceIntegrity(100L, null);

        assertNotNull(result);
        assertEquals(IntegrityStatus.VERIFIED, result.getIntegrityStatus());
        verify(historyRepository).save(any(EvidenceVerificationHistoryEntity.class));
    }

    @Test
    void verifyEvidenceIntegrity_Tampered() {
        when(evidenceRepository.findById(100L)).thenReturn(Optional.of(evidenceEntity));
        when(hashService.generateHash(any(Path.class)))
                .thenReturn("different_tampered_hash_value");

        IntegrityVerificationResponse result = integrityService.verifyEvidenceIntegrity(100L, null);

        assertNotNull(result);
        assertEquals(IntegrityStatus.TAMPERED, result.getIntegrityStatus());
        verify(historyRepository).save(any(EvidenceVerificationHistoryEntity.class));
    }

    @Test
    void verifyEvidenceIntegrity_FileMissing() {
        evidenceEntity.setStoragePath(tempDir.resolve("non_existent_file.txt").toString());
        when(evidenceRepository.findById(100L)).thenReturn(Optional.of(evidenceEntity));

        IntegrityVerificationResponse result = integrityService.verifyEvidenceIntegrity(100L, null);

        assertNotNull(result);
        assertEquals(IntegrityStatus.FILE_MISSING, result.getIntegrityStatus());
        verify(historyRepository).save(any(EvidenceVerificationHistoryEntity.class));
    }

    @Test
    void verifyEvidenceIntegrity_HashMissing() {
        evidenceEntity.setFileHash(null);
        when(evidenceRepository.findById(100L)).thenReturn(Optional.of(evidenceEntity));

        IntegrityVerificationResponse result = integrityService.verifyEvidenceIntegrity(100L, null);

        assertNotNull(result);
        assertEquals(IntegrityStatus.HASH_MISSING, result.getIntegrityStatus());
        verify(historyRepository).save(any(EvidenceVerificationHistoryEntity.class));
    }
}
