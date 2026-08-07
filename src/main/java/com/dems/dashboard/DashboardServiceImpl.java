package com.dems.dashboard;

import com.dems.audit.AuditLogEntity;
import com.dems.audit.AuditLogMapper;
import com.dems.audit.AuditLogRepository;
import com.dems.audit.AuditSummaryResponse;
import com.dems.cases.CaseEntity;
import com.dems.cases.CaseRepository;
import com.dems.custody.CustodyMapper;
import com.dems.custody.CustodyRecordEntity;
import com.dems.custody.CustodyRecordRepository;
import com.dems.custody.CustodyResponse;
import com.dems.enums.AuditAction;
import com.dems.enums.AuditStatus;
import com.dems.enums.CaseStatus;
import com.dems.enums.CrimeSeverity;
import com.dems.enums.CrimeType;
import com.dems.enums.EvidenceStatus;
import com.dems.enums.EvidenceType;
import com.dems.enums.IntegrityStatus;
import com.dems.enums.TransferStatus;
import com.dems.evidence.EvidenceEntity;
import com.dems.evidence.EvidenceMapper;
import com.dems.evidence.EvidenceRepository;
import com.dems.evidence.EvidenceSummaryResponse;
import com.dems.integrity.EvidenceVerificationHistoryEntity;
import com.dems.integrity.EvidenceVerificationHistoryRepository;
import com.dems.integrity.IntegrityVerificationResponse;
import com.dems.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.ArrayList;
import java.util.EnumMap;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

/**
 * Service implementation aggregating enterprise read-only dashboard metrics from existing repositories using JPQL projections.
 */
@Slf4j
@Service
public class DashboardServiceImpl implements DashboardService {

    private final CaseRepository caseRepository;
    private final EvidenceRepository evidenceRepository;
    private final EvidenceVerificationHistoryRepository historyRepository;
    private final CustodyRecordRepository custodyRepository;
    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final EvidenceMapper evidenceMapper;
    private final CustodyMapper custodyMapper;
    private final AuditLogMapper auditLogMapper;

    public DashboardServiceImpl(
            CaseRepository caseRepository,
            EvidenceRepository evidenceRepository,
            EvidenceVerificationHistoryRepository historyRepository,
            CustodyRecordRepository custodyRepository,
            AuditLogRepository auditLogRepository,
            UserRepository userRepository,
            EvidenceMapper evidenceMapper,
            CustodyMapper custodyMapper,
            AuditLogMapper auditLogMapper) {
        this.caseRepository = caseRepository;
        this.evidenceRepository = evidenceRepository;
        this.historyRepository = historyRepository;
        this.custodyRepository = custodyRepository;
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.evidenceMapper = evidenceMapper;
        this.custodyMapper = custodyMapper;
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    @Transactional(readOnly = true)
    public DashboardSummaryResponse getSummary(OffsetDateTime startDate, OffsetDateTime endDate) {
        long totalCases = (startDate != null && endDate != null)
                ? caseRepository.countByCreatedAtBetween(startDate, endDate) : caseRepository.count();

        long openCases = caseRepository.countByStatus(CaseStatus.OPEN);
        long underInvCases = caseRepository.countByStatus(CaseStatus.UNDER_INVESTIGATION);
        long closedCases = caseRepository.countByStatus(CaseStatus.CLOSED);
        long archivedCases = caseRepository.countByStatus(CaseStatus.ARCHIVED);

        long totalEvidence = (startDate != null && endDate != null)
                ? evidenceRepository.countByCreatedAtBetween(startDate, endDate) : evidenceRepository.count();

        Map<EvidenceType, Long> evidenceByType = new EnumMap<>(EvidenceType.class);
        for (Object[] row : evidenceRepository.countByEvidenceTypeGroup()) {
            if (row[0] != null) {
                evidenceByType.put((EvidenceType) row[0], (Long) row[1]);
            }
        }

        long verifiedCount = historyRepository.countByStatus(IntegrityStatus.VERIFIED);
        long tamperedCount = historyRepository.countByStatus(IntegrityStatus.TAMPERED);
        long pendingTransfers = custodyRepository.countByTransferStatus(TransferStatus.PENDING);

        OffsetDateTime startOfToday = OffsetDateTime.now(ZoneOffset.UTC).toLocalDate().atStartOfDay().atOffset(ZoneOffset.UTC);
        long todaysAuditEvents = auditLogRepository.countByActionTimestampAfter(startOfToday);
        long successfulAudits = auditLogRepository.countByStatus(AuditStatus.SUCCESS);
        long failedAudits = auditLogRepository.countByStatus(AuditStatus.FAILED);

        return DashboardSummaryResponse.builder()
                .totalCases(totalCases)
                .openCases(openCases)
                .underInvestigationCases(underInvCases)
                .closedCases(closedCases)
                .archivedCases(archivedCases)
                .totalEvidence(totalEvidence)
                .evidenceByType(evidenceByType)
                .verifiedEvidence(verifiedCount)
                .tamperedEvidence(tamperedCount)
                .pendingCustodyTransfers(pendingTransfers)
                .todaysAuditEvents(todaysAuditEvents)
                .successfulAuditEvents(successfulAudits)
                .failedAuditEvents(failedAudits)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CaseAnalyticsResponse getCaseAnalytics(OffsetDateTime startDate, OffsetDateTime endDate) {
        Map<CaseStatus, Long> casesByStatus = new EnumMap<>(CaseStatus.class);
        for (Object[] row : caseRepository.countByStatusGroup()) {
            if (row[0] != null) {
                casesByStatus.put((CaseStatus) row[0], (Long) row[1]);
            }
        }

        Map<CrimeType, Long> casesByCrimeType = new EnumMap<>(CrimeType.class);
        for (Object[] row : caseRepository.countByCrimeTypeGroup()) {
            if (row[0] != null) {
                casesByCrimeType.put((CrimeType) row[0], (Long) row[1]);
            }
        }

        Map<CrimeSeverity, Long> casesBySeverity = new EnumMap<>(CrimeSeverity.class);
        for (Object[] row : caseRepository.countBySeverityGroup()) {
            if (row[0] != null) {
                casesBySeverity.put((CrimeSeverity) row[0], (Long) row[1]);
            }
        }

        List<MonthlyCountDto> monthlyTrend = buildMonthlyTrend(caseRepository.findAll().stream()
                .map(CaseEntity::getCreatedAt).filter(t -> t != null).toList());

        return CaseAnalyticsResponse.builder()
                .casesByStatus(casesByStatus)
                .casesByCrimeType(casesByCrimeType)
                .casesBySeverity(casesBySeverity)
                .casesCreatedPerMonth(monthlyTrend)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public EvidenceAnalyticsResponse getEvidenceAnalytics(OffsetDateTime startDate, OffsetDateTime endDate) {
        Map<EvidenceType, Long> evidenceByType = new EnumMap<>(EvidenceType.class);
        for (Object[] row : evidenceRepository.countByEvidenceTypeGroup()) {
            if (row[0] != null) {
                evidenceByType.put((EvidenceType) row[0], (Long) row[1]);
            }
        }

        Map<EvidenceStatus, Long> evidenceByStatus = new EnumMap<>(EvidenceStatus.class);
        for (Object[] row : evidenceRepository.countByStatusGroup()) {
            if (row[0] != null) {
                evidenceByStatus.put((EvidenceStatus) row[0], (Long) row[1]);
            }
        }

        List<EvidenceSummaryResponse> largestFiles = evidenceRepository.findTop5ByOrderByFileSizeDesc().stream()
                .map(evidenceMapper::toSummaryResponse).toList();

        List<EvidenceSummaryResponse> latestUploads = evidenceRepository.findTop5ByOrderByUploadedAtDesc().stream()
                .map(evidenceMapper::toSummaryResponse).toList();

        List<MonthlyCountDto> monthlyTrend = buildMonthlyTrend(evidenceRepository.findAll().stream()
                .map(EvidenceEntity::getCreatedAt).filter(t -> t != null).toList());

        return EvidenceAnalyticsResponse.builder()
                .evidenceByType(evidenceByType)
                .evidenceByStatus(evidenceByStatus)
                .largestFiles(largestFiles)
                .latestUploads(latestUploads)
                .evidenceUploadedPerMonth(monthlyTrend)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public IntegrityAnalyticsResponse getIntegrityAnalytics(OffsetDateTime startDate, OffsetDateTime endDate) {
        long verified = historyRepository.countByStatus(IntegrityStatus.VERIFIED);
        long tampered = historyRepository.countByStatus(IntegrityStatus.TAMPERED);
        long hashMissing = historyRepository.countByStatus(IntegrityStatus.HASH_MISSING);
        long verificationFailed = historyRepository.countByStatus(IntegrityStatus.VERIFICATION_FAILED);

        List<IntegrityVerificationResponse> latestVerifications = historyRepository.findTop5ByOrderByVerifiedAtDesc().stream()
                .map(h -> IntegrityVerificationResponse.builder()
                        .verificationNumber(h.getVerificationNumber())
                        .evidenceId(h.getEvidence() != null ? h.getEvidence().getId() : null)
                        .evidenceNumber(h.getEvidence() != null ? h.getEvidence().getEvidenceNumber() : null)
                        .evidenceName(h.getEvidence() != null ? h.getEvidence().getEvidenceName() : null)
                        .integrityStatus(h.getStatus())
                        .storedHash(h.getStoredHash())
                        .currentHash(h.getCurrentHash())
                        .hashAlgorithm("SHA-256")
                        .verifiedAt(h.getVerifiedAt())
                        .verifiedBy(h.getVerifiedBy() != null ? h.getVerifiedBy().getFullName() : "System")
                        .message(h.getRemarks())
                        .build()).toList();

        return IntegrityAnalyticsResponse.builder()
                .verifiedCount(verified)
                .tamperedCount(tampered)
                .hashMissingCount(hashMissing)
                .verificationFailedCount(verificationFailed)
                .latestVerifications(latestVerifications)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public CustodyAnalyticsResponse getCustodyAnalytics(OffsetDateTime startDate, OffsetDateTime endDate) {
        long pending = custodyRepository.countByTransferStatus(TransferStatus.PENDING);
        long accepted = custodyRepository.countByTransferStatus(TransferStatus.ACCEPTED);
        long rejected = custodyRepository.countByTransferStatus(TransferStatus.REJECTED);

        double avgMinutes = 0.0;
        List<CustodyRecordEntity> acceptedRecords = custodyRepository.findByTransferStatus(TransferStatus.ACCEPTED);
        if (!acceptedRecords.isEmpty()) {
            long totalSeconds = 0;
            int count = 0;
            for (CustodyRecordEntity record : acceptedRecords) {
                if (record.getTransferredAt() != null && record.getAcceptedAt() != null) {
                    totalSeconds += Duration.between(record.getTransferredAt(), record.getAcceptedAt()).getSeconds();
                    count++;
                }
            }
            if (count > 0) {
                avgMinutes = (totalSeconds / 60.0) / count;
            }
        }

        List<CustodyResponse> latestTransfers = custodyRepository.findTop5ByOrderByTransferredAtDesc().stream()
                .map(custodyMapper::toResponse).toList();

        return CustodyAnalyticsResponse.builder()
                .pendingTransfers(pending)
                .acceptedTransfers(accepted)
                .rejectedTransfers(rejected)
                .averageTransferTimeMinutes(Math.round(avgMinutes * 100.0) / 100.0)
                .latestCustodyTransfers(latestTransfers)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public AuditAnalyticsResponse getAuditAnalytics(OffsetDateTime startDate, OffsetDateTime endDate) {
        Map<AuditAction, Long> eventsByAction = new EnumMap<>(AuditAction.class);
        for (Object[] row : auditLogRepository.countByActionGroup()) {
            if (row[0] != null) {
                eventsByAction.put((AuditAction) row[0], (Long) row[1]);
            }
        }

        Map<String, Long> eventsByModule = new HashMap<>();
        for (Object[] row : auditLogRepository.countByModuleNameGroup()) {
            if (row[0] != null) {
                eventsByModule.put((String) row[0], (Long) row[1]);
            }
        }

        Map<AuditStatus, Long> eventsByStatus = new EnumMap<>(AuditStatus.class);
        for (Object[] row : auditLogRepository.countByStatusGroup()) {
            if (row[0] != null) {
                eventsByStatus.put((AuditStatus) row[0], (Long) row[1]);
            }
        }

        List<TopActiveUserDto> topUsers = new ArrayList<>();
        for (Object[] row : auditLogRepository.findTopActiveUsers(PageRequest.of(0, 5))) {
            if (row[0] != null) {
                topUsers.add(TopActiveUserDto.builder()
                        .username((String) row[0])
                        .activityCount((Long) row[1])
                        .build());
            }
        }

        List<AuditSummaryResponse> recentActivities = auditLogRepository.findTop5ByOrderByActionTimestampDesc().stream()
                .map(auditLogMapper::toSummaryResponse).toList();

        return AuditAnalyticsResponse.builder()
                .eventsByAction(eventsByAction)
                .eventsByModule(eventsByModule)
                .eventsByStatus(eventsByStatus)
                .topActiveUsers(topUsers)
                .recentActivities(recentActivities)
                .build();
    }

    @Override
    @Transactional(readOnly = true)
    public List<RecentActivityDto> getRecentActivities() {
        List<RecentActivityDto> activities = new ArrayList<>();

        List<AuditLogEntity> recentAudits = auditLogRepository.findTop5ByOrderByActionTimestampDesc();
        for (AuditLogEntity audit : recentAudits) {
            activities.add(RecentActivityDto.builder()
                    .activityType(audit.getAction().name())
                    .reference(audit.getEntityReference())
                    .description(audit.getDescription())
                    .performedBy(audit.getUsername())
                    .timestamp(audit.getActionTimestamp())
                    .build());
        }

        return activities;
    }

    @Override
    @Transactional(readOnly = true)
    public SystemHealthResponse getSystemHealth() {
        long totalCases = caseRepository.count();
        long totalEvidenceFiles = evidenceRepository.count();
        long totalBytes = evidenceRepository.sumFileSize();
        long activeUsers = userRepository.count();

        long verifiedCount = historyRepository.countByStatus(IntegrityStatus.VERIFIED);
        long tamperedCount = historyRepository.countByStatus(IntegrityStatus.TAMPERED);
        long totalVerifications = historyRepository.count();

        double successRate = 100.0;
        if (totalVerifications > 0) {
            successRate = ((double) verifiedCount / totalVerifications) * 100.0;
        }

        String formattedSize = formatStorageSize(totalBytes);

        return SystemHealthResponse.builder()
                .status("UP")
                .database("HEALTHY")
                .totalCases(totalCases)
                .totalEvidenceFiles(totalEvidenceFiles)
                .totalStorageBytes(totalBytes)
                .formattedStorageSize(formattedSize)
                .activeUsersCount(activeUsers)
                .integritySuccessRatePercentage(Math.round(successRate * 100.0) / 100.0)
                .checkedAt(OffsetDateTime.now())
                .build();
    }

    private List<MonthlyCountDto> buildMonthlyTrend(List<OffsetDateTime> timestamps) {
        Map<String, Long> monthlyCounts = new HashMap<>();
        for (OffsetDateTime ts : timestamps) {
            String key = ts.getYear() + "-" + String.format("%02d", ts.getMonthValue());
            monthlyCounts.put(key, monthlyCounts.getOrDefault(key, 0L) + 1);
        }

        List<MonthlyCountDto> result = new ArrayList<>();
        monthlyCounts.forEach((key, count) -> {
            String[] parts = key.split("-");
            int year = Integer.parseInt(parts[0]);
            int month = Integer.parseInt(parts[1]);
            result.add(MonthlyCountDto.builder()
                    .year(year)
                    .month(month)
                    .monthName(java.time.Month.of(month).name())
                    .count(count)
                    .build());
        });
        return result;
    }

    private String formatStorageSize(long bytes) {
        if (bytes < 1024) return bytes + " B";
        int exp = (int) (Math.log(bytes) / Math.log(1024));
        char pre = "KMGTPE".charAt(exp - 1);
        return String.format(Locale.ROOT, "%.2f %cB", bytes / Math.pow(1024, exp), pre);
    }
}
