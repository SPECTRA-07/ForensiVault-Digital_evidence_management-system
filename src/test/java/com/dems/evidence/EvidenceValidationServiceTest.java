package com.dems.evidence;

import com.dems.cases.CaseEntity;
import com.dems.enums.CaseStatus;
import com.dems.enums.EvidenceStatus;
import com.dems.enums.EvidenceType;
import com.dems.exception.BadRequestException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;

class EvidenceValidationServiceTest {

    private EvidenceValidationService validationService;

    @BeforeEach
    void setUp() {
        validationService = new EvidenceValidationService();
    }

    @Test
    void validateFile_ValidImage_Success() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "image/png", "bytes".getBytes());

        assertDoesNotThrow(() -> validationService.validateFile(file, EvidenceType.IMAGE));
    }

    @Test
    void validateFile_MismatchedMimeType_ThrowsBadRequestException() {
        MockMultipartFile file = new MockMultipartFile("file", "photo.png", "text/plain", "bytes".getBytes());

        assertThrows(BadRequestException.class, () -> validationService.validateFile(file, EvidenceType.IMAGE));
    }

    @Test
    void validateCaseEligibility_ClosedCase_ThrowsBadRequestException() {
        CaseEntity caseEntity = CaseEntity.builder()
                .id(1L)
                .status(CaseStatus.CLOSED)
                .active(true)
                .build();

        assertThrows(BadRequestException.class, () -> validationService.validateCaseEligibility(caseEntity));
    }

    @Test
    void validateCaseEligibility_OpenCase_Success() {
        CaseEntity caseEntity = CaseEntity.builder()
                .id(1L)
                .status(CaseStatus.OPEN)
                .active(true)
                .build();

        assertDoesNotThrow(() -> validationService.validateCaseEligibility(caseEntity));
    }

    @Test
    void validateEvidenceModification_Archived_ThrowsBadRequestException() {
        EvidenceEntity evidence = EvidenceEntity.builder()
                .id(10L)
                .status(EvidenceStatus.ARCHIVED)
                .active(true)
                .build();

        assertThrows(BadRequestException.class, () -> validationService.validateEvidenceModification(evidence));
    }
}
