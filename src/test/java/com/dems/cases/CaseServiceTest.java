package com.dems.cases;

import com.dems.enums.CaseStatus;
import com.dems.enums.CrimeSeverity;
import com.dems.enums.CrimeType;
import com.dems.enums.UserRole;
import com.dems.exception.BadRequestException;
import com.dems.user.UserEntity;
import com.dems.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Spy;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CaseServiceTest {

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private CaseMapper caseMapper;

    @Spy
    private CaseStateMachine caseStateMachine = new CaseStateMachine();

    @Mock
    private com.dems.audit.AuditService auditService;

    @InjectMocks
    private CaseServiceImpl caseService;

    private CaseCreateRequest createRequest;
    private CaseEntity caseEntity;
    private CaseResponse caseResponse;
    private CaseSummaryResponse caseSummaryResponse;
    private UserEntity policeOfficer;
    private UserEntity forensicExpert;

    @BeforeEach
    void setUp() {
        policeOfficer = UserEntity.builder()
                .id(2L)
                .employeeId("OFFICER-001")
                .fullName("Officer Smith")
                .email("smith@dems.gov")
                .role(UserRole.POLICE_OFFICER)
                .active(true)
                .build();

        forensicExpert = UserEntity.builder()
                .id(3L)
                .employeeId("EXPERT-001")
                .fullName("Expert Jane")
                .email("jane@dems.gov")
                .role(UserRole.FORENSIC_EXPERT)
                .active(true)
                .build();

        createRequest = CaseCreateRequest.builder()
                .caseNumber("CASE-2026-001")
                .crimeNumber("CRIME-999")
                .caseName("Grand Theft Investigation")
                .caseSummary("Investigation into major theft")
                .crimeType(CrimeType.THEFT)
                .severity(CrimeSeverity.HIGH)
                .incidentDate(LocalDate.now().minusDays(2))
                .crimeSceneLocation("Downtown District")
                .assignedOfficerId(2L)
                .build();

        caseEntity = CaseEntity.builder()
                .id(100L)
                .caseNumber("CASE-2026-001")
                .crimeNumber("CRIME-999")
                .caseName("Grand Theft Investigation")
                .caseSummary("Investigation into major theft")
                .crimeType(CrimeType.THEFT)
                .severity(CrimeSeverity.HIGH)
                .status(CaseStatus.OPEN)
                .incidentDate(LocalDate.now().minusDays(2))
                .crimeSceneLocation("Downtown District")
                .assignedOfficer(policeOfficer)
                .active(true)
                .build();

        caseResponse = CaseResponse.builder()
                .id(100L)
                .caseNumber("CASE-2026-001")
                .crimeNumber("CRIME-999")
                .caseName("Grand Theft Investigation")
                .status(CaseStatus.OPEN)
                .build();

        caseSummaryResponse = CaseSummaryResponse.builder()
                .id(100L)
                .caseNumber("CASE-2026-001")
                .crimeNumber("CRIME-999")
                .caseName("Grand Theft Investigation")
                .status(CaseStatus.OPEN)
                .assignedOfficerName("Officer Smith")
                .build();
    }

    @Test
    void createCase_Success() {
        when(caseRepository.existsByCaseNumber("CASE-2026-001")).thenReturn(false);
        when(caseRepository.existsByCrimeNumber("CRIME-999")).thenReturn(false);
        when(userRepository.findById(2L)).thenReturn(Optional.of(policeOfficer));
        when(caseRepository.save(any(CaseEntity.class))).thenReturn(caseEntity);
        when(caseMapper.toResponse(caseEntity)).thenReturn(caseResponse);

        CaseResponse result = caseService.createCase(createRequest);

        assertNotNull(result);
        assertEquals("CASE-2026-001", result.getCaseNumber());
        verify(caseRepository).save(any(CaseEntity.class));
    }

    @Test
    void createCase_FutureDate_ThrowsBadRequestException() {
        createRequest.setIncidentDate(LocalDate.now().plusDays(1));

        assertThrows(BadRequestException.class, () -> caseService.createCase(createRequest));
    }

    @Test
    void createCase_AssignedNonPoliceOfficer_ThrowsBadRequestException() {
        when(caseRepository.existsByCaseNumber("CASE-2026-001")).thenReturn(false);
        when(caseRepository.existsByCrimeNumber("CRIME-999")).thenReturn(false);
        when(userRepository.findById(3L)).thenReturn(Optional.of(forensicExpert));

        createRequest.setAssignedOfficerId(3L);

        assertThrows(BadRequestException.class, () -> caseService.createCase(createRequest));
    }

    @Test
    void updateCaseStatus_ClosedToOpen_ThrowsBadRequestException() {
        caseEntity.setStatus(CaseStatus.CLOSED);
        when(caseRepository.findById(100L)).thenReturn(Optional.of(caseEntity));
        doThrow(new BadRequestException("Invalid status transition"))
                .when(caseStateMachine).validateAndHandleTransition(caseEntity, CaseStatus.OPEN);

        CaseStatusUpdateRequest request = new CaseStatusUpdateRequest(CaseStatus.OPEN, "Reopening");

        assertThrows(BadRequestException.class, () -> caseService.updateCaseStatus(100L, request));
    }

    @Test
    void getMyAssignedCases_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<CaseEntity> casePage = new PageImpl<>(List.of(caseEntity), pageable, 1);

        when(caseRepository.findByAssignedOfficer_Email("smith@dems.gov", pageable)).thenReturn(casePage);
        when(caseMapper.toSummaryResponse(caseEntity)).thenReturn(caseSummaryResponse);

        Page<CaseSummaryResponse> result = caseService.getMyAssignedCases("smith@dems.gov", pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("CASE-2026-001", result.getContent().get(0).getCaseNumber());
    }
}
