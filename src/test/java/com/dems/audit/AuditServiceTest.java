package com.dems.audit;

import com.dems.enums.AuditAction;
import com.dems.enums.AuditEntityType;
import com.dems.enums.AuditStatus;
import com.dems.user.UserEntity;
import com.dems.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import org.mockito.quality.Strictness;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class AuditServiceTest {

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private AuditServiceImpl auditService;

    private AuditLogEntity auditLogEntity;
    private AuditLogResponse auditLogResponse;
    private AuditSummaryResponse auditSummaryResponse;

    @BeforeEach
    void setUp() {
        auditLogEntity = AuditLogEntity.builder()
                .id(1L)
                .auditNumber("AUD-100")
                .action(AuditAction.LOGIN)
                .entityType(AuditEntityType.AUTHENTICATION)
                .moduleName("AUTH")
                .entityReference("officer@dems.gov")
                .username("officer@dems.gov")
                .status(AuditStatus.SUCCESS)
                .actionTimestamp(OffsetDateTime.now())
                .active(true)
                .build();

        auditLogResponse = AuditLogResponse.builder()
                .id(1L)
                .auditNumber("AUD-100")
                .action(AuditAction.LOGIN)
                .status(AuditStatus.SUCCESS)
                .build();

        auditSummaryResponse = AuditSummaryResponse.builder()
                .id(1L)
                .auditNumber("AUD-100")
                .action(AuditAction.LOGIN)
                .status(AuditStatus.SUCCESS)
                .build();
    }

    @Test
    void recordAudit_Success() {
        UserEntity user = UserEntity.builder().id(1L).email("officer@dems.gov").build();

        auditService.recordAudit(
                AuditAction.CREATE,
                AuditEntityType.CASE,
                "CASE",
                "CASE-2026-001",
                10L,
                user,
                "officer@dems.gov",
                "ROLE_POLICE_OFFICER",
                "127.0.0.1",
                "JUnit",
                "CORR-123",
                null,
                null,
                50L,
                AuditStatus.SUCCESS,
                "Created new case record",
                null
        );

        verify(auditLogRepository).save(any(AuditLogEntity.class));
    }

    @Test
    void getAuditLogById_Success() {
        when(auditLogRepository.findById(1L)).thenReturn(Optional.of(auditLogEntity));
        when(auditLogMapper.toResponse(auditLogEntity)).thenReturn(auditLogResponse);

        AuditLogResponse response = auditService.getAuditLogById(1L);

        assertNotNull(response);
        assertEquals("AUD-100", response.getAuditNumber());
        assertEquals(AuditStatus.SUCCESS, response.getStatus());
    }

    @Test
    void getAuditDashboard_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLogEntity> page = new PageImpl<>(List.of(auditLogEntity));

        when(auditLogRepository.count()).thenReturn(1L);
        when(auditLogRepository.countByStatus(AuditStatus.SUCCESS)).thenReturn(1L);
        when(auditLogRepository.countByAction(AuditAction.LOGIN)).thenReturn(1L);
        when(auditLogRepository.countByEntityType(AuditEntityType.AUTHENTICATION)).thenReturn(1L);
        when(auditLogRepository.findAll(pageable)).thenReturn(page);
        when(auditLogMapper.toSummaryResponse(auditLogEntity)).thenReturn(auditSummaryResponse);

        AuditDashboardResponse dashboard = auditService.getAuditDashboard(pageable);

        assertNotNull(dashboard);
        assertEquals(1L, dashboard.getTotalAuditCount());
        assertEquals(1L, dashboard.getSuccessCount());
        assertEquals(1, dashboard.getRecentLogs().getContent().size());
    }

    @Test
    @SuppressWarnings("unchecked")
    void searchAuditLogs_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<AuditLogEntity> page = new PageImpl<>(List.of(auditLogEntity));
        AuditSearchRequest searchRequest = AuditSearchRequest.builder().action(AuditAction.LOGIN).build();

        when(auditLogRepository.findAll(any(Specification.class), any(Pageable.class))).thenReturn(page);
        when(auditLogMapper.toSummaryResponse(auditLogEntity)).thenReturn(auditSummaryResponse);

        Page<AuditSummaryResponse> results = auditService.searchAuditLogs(searchRequest, pageable);

        assertNotNull(results);
        assertEquals(1, results.getContent().size());
    }
}
