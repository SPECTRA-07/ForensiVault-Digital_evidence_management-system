package com.dems.dashboard;

import com.dems.audit.AuditLogMapper;
import com.dems.audit.AuditLogRepository;
import com.dems.cases.CaseRepository;
import com.dems.custody.CustodyMapper;
import com.dems.custody.CustodyRecordRepository;
import com.dems.enums.CaseStatus;
import com.dems.enums.EvidenceType;
import com.dems.enums.IntegrityStatus;
import com.dems.enums.TransferStatus;
import com.dems.evidence.EvidenceMapper;
import com.dems.evidence.EvidenceRepository;
import com.dems.integrity.EvidenceVerificationHistoryRepository;
import com.dems.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.quality.Strictness;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@org.mockito.junit.jupiter.MockitoSettings(strictness = Strictness.LENIENT)
class DashboardServiceTest {

    @Mock
    private CaseRepository caseRepository;

    @Mock
    private EvidenceRepository evidenceRepository;

    @Mock
    private EvidenceVerificationHistoryRepository historyRepository;

    @Mock
    private CustodyRecordRepository custodyRepository;

    @Mock
    private AuditLogRepository auditLogRepository;

    @Mock
    private UserRepository userRepository;

    @Mock
    private EvidenceMapper evidenceMapper;

    @Mock
    private CustodyMapper custodyMapper;

    @Mock
    private AuditLogMapper auditLogMapper;

    @InjectMocks
    private DashboardServiceImpl dashboardService;

    @BeforeEach
    void setUp() {
        when(caseRepository.count()).thenReturn(10L);
        when(caseRepository.countByStatus(CaseStatus.OPEN)).thenReturn(5L);
        when(evidenceRepository.count()).thenReturn(25L);
        when(historyRepository.countByStatus(IntegrityStatus.VERIFIED)).thenReturn(20L);
        when(custodyRepository.countByTransferStatus(TransferStatus.PENDING)).thenReturn(2L);
    }

    @Test
    void getSummary_Success() {
        Object[] row = new Object[]{EvidenceType.IMAGE, 15L};
        List<Object[]> rows = new java.util.ArrayList<>();
        rows.add(row);
        when(evidenceRepository.countByEvidenceTypeGroup()).thenReturn(rows);

        DashboardSummaryResponse response = dashboardService.getSummary(null, null);

        assertNotNull(response);
        assertEquals(10L, response.getTotalCases());
        assertEquals(5L, response.getOpenCases());
        assertEquals(25L, response.getTotalEvidence());
        assertEquals(20L, response.getVerifiedEvidence());
        assertEquals(2L, response.getPendingCustodyTransfers());
    }

    @Test
    void getCaseAnalytics_Success() {
        Object[] row = new Object[]{CaseStatus.OPEN, 5L};
        List<Object[]> rows = new java.util.ArrayList<>();
        rows.add(row);
        when(caseRepository.countByStatusGroup()).thenReturn(rows);

        CaseAnalyticsResponse response = dashboardService.getCaseAnalytics(null, null);

        assertNotNull(response);
        assertEquals(1, response.getCasesByStatus().size());
        assertEquals(5L, response.getCasesByStatus().get(CaseStatus.OPEN));
    }

    @Test
    void getSystemHealth_Success() {
        when(evidenceRepository.sumFileSize()).thenReturn(1048576L); // 1 MB
        when(userRepository.count()).thenReturn(3L);
        when(historyRepository.count()).thenReturn(10L);

        SystemHealthResponse response = dashboardService.getSystemHealth();

        assertNotNull(response);
        assertEquals("UP", response.getStatus());
        assertEquals(10L, response.getTotalCases());
        assertEquals(25L, response.getTotalEvidenceFiles());
        assertEquals(1048576L, response.getTotalStorageBytes());
        assertEquals("1.00 MB", response.getFormattedStorageSize());
    }
}
