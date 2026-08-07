package com.dems.audit;

import com.dems.enums.AuditAction;
import com.dems.enums.AuditEntityType;
import com.dems.enums.AuditStatus;
import com.dems.exception.ResourceNotFoundException;
import com.dems.user.UserEntity;
import com.dems.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.time.OffsetDateTime;
import java.util.EnumMap;
import java.util.Locale;
import java.util.Map;
import java.util.UUID;

/**
 * Implementation of AuditService recording permanent, immutable audit log events with AuditContext extraction and summary dashboards.
 */
@Slf4j
@Service
public class AuditServiceImpl implements AuditService {

    private final AuditLogRepository auditLogRepository;
    private final UserRepository userRepository;
    private final AuditLogMapper auditLogMapper;

    public AuditServiceImpl(
            AuditLogRepository auditLogRepository,
            UserRepository userRepository,
            AuditLogMapper auditLogMapper) {
        this.auditLogRepository = auditLogRepository;
        this.userRepository = userRepository;
        this.auditLogMapper = auditLogMapper;
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordAudit(
            AuditAction action,
            AuditEntityType entityType,
            String moduleName,
            String entityReference,
            Long entityId,
            UserEntity performedBy,
            String username,
            String userRole,
            String ipAddress,
            String userAgent,
            String correlationId,
            String previousValue,
            String newValue,
            Long executionTimeMs,
            AuditStatus status,
            String description,
            String failureReason
    ) {
        try {
            String auditNumber = generateAuditNumber();

            AuditLogEntity entity = AuditLogEntity.builder()
                    .auditNumber(auditNumber)
                    .action(action)
                    .entityType(entityType)
                    .moduleName(moduleName)
                    .entityReference(entityReference)
                    .entityId(entityId)
                    .performedBy(performedBy)
                    .username(username != null ? username : "ANONYMOUS")
                    .userRole(userRole)
                    .ipAddress(ipAddress)
                    .userAgent(userAgent)
                    .correlationId(correlationId)
                    .previousValue(previousValue)
                    .newValue(newValue)
                    .executionTimeMs(executionTimeMs)
                    .status(status)
                    .description(description)
                    .failureReason(failureReason)
                    .actionTimestamp(OffsetDateTime.now())
                    .active(true)
                    .build();

            auditLogRepository.save(entity);

            log.info("Audit Record Saved: Number [{}], Action [{}], EntityType [{}], User [{}], Status [{}]",
                    auditNumber, action, entityType, username, status);

        } catch (Exception e) {
            log.error("Failed to persist audit log entry for action [{}]", action, e);
        }
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordEvent(
            AuditAction action,
            AuditEntityType entityType,
            String moduleName,
            String entityReference,
            Long entityId,
            AuditStatus status,
            String description
    ) {
        recordEvent(action, entityType, moduleName, entityReference, entityId, status, description, null);
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordEvent(
            AuditAction action,
            AuditEntityType entityType,
            String moduleName,
            String entityReference,
            Long entityId,
            AuditStatus status,
            String description,
            String failureReason
    ) {
        AuditContext context = AuditContextHolder.currentContext();
        UserEntity performedBy = null;
        if (context.getUsername() != null && !"ANONYMOUS".equalsIgnoreCase(context.getUsername())) {
            performedBy = userRepository.findByEmail(context.getUsername().trim().toLowerCase(Locale.ROOT)).orElse(null);
        }

        recordAudit(
                action,
                entityType,
                moduleName,
                entityReference,
                entityId,
                performedBy,
                context.getUsername(),
                context.getUserRole(),
                context.getIpAddress(),
                context.getUserAgent(),
                context.getCorrelationId(),
                null,
                null,
                null,
                status,
                description,
                failureReason
        );
    }

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void recordEventWithDiff(
            AuditAction action,
            AuditEntityType entityType,
            String moduleName,
            String entityReference,
            Long entityId,
            String previousValue,
            String newValue,
            AuditStatus status,
            String description
    ) {
        AuditContext context = AuditContextHolder.currentContext();
        UserEntity performedBy = null;
        if (context.getUsername() != null && !"ANONYMOUS".equalsIgnoreCase(context.getUsername())) {
            performedBy = userRepository.findByEmail(context.getUsername().trim().toLowerCase(Locale.ROOT)).orElse(null);
        }

        recordAudit(
                action,
                entityType,
                moduleName,
                entityReference,
                entityId,
                performedBy,
                context.getUsername(),
                context.getUserRole(),
                context.getIpAddress(),
                context.getUserAgent(),
                context.getCorrelationId(),
                previousValue,
                newValue,
                null,
                status,
                description,
                null
        );
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditSummaryResponse> getAllAuditLogs(Pageable pageable) {
        return auditLogRepository.findAll(pageable)
                .map(auditLogMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditLogResponse getAuditLogById(Long id) {
        AuditLogEntity entity = auditLogRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Audit log record not found with ID: " + id));
        return auditLogMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditSummaryResponse> getAuditLogsByUserId(Long userId, Pageable pageable) {
        if (!userRepository.existsById(userId)) {
            throw new ResourceNotFoundException("User not found with ID: " + userId);
        }
        return auditLogRepository.findByPerformedBy_Id(userId, pageable)
                .map(auditLogMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditSummaryResponse> getAuditLogsByEntity(AuditEntityType entityType, Long entityId, Pageable pageable) {
        return auditLogRepository.findByEntityTypeAndEntityId(entityType, entityId, pageable)
                .map(auditLogMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<AuditSummaryResponse> searchAuditLogs(AuditSearchRequest request, Pageable pageable) {
        Specification<AuditLogEntity> spec = AuditLogSpecification.filterAuditLogs(
                request.getAction(),
                request.getEntityType(),
                request.getModuleName(),
                request.getUsername(),
                request.getStatus(),
                request.getStartDate(),
                request.getEndDate(),
                request.getEntityReference(),
                request.getIpAddress(),
                request.getCorrelationId(),
                request.getActive()
        );

        return auditLogRepository.findAll(spec, pageable)
                .map(auditLogMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public AuditDashboardResponse getAuditDashboard(Pageable pageable) {
        long totalCount = auditLogRepository.count();
        long successCount = auditLogRepository.countByStatus(AuditStatus.SUCCESS);
        long failedCount = auditLogRepository.countByStatus(AuditStatus.FAILED);
        long warningCount = auditLogRepository.countByStatus(AuditStatus.WARNING);

        Map<AuditAction, Long> actionCounts = new EnumMap<>(AuditAction.class);
        for (AuditAction action : AuditAction.values()) {
            long c = auditLogRepository.countByAction(action);
            if (c > 0) {
                actionCounts.put(action, c);
            }
        }

        Map<AuditEntityType, Long> entityTypeCounts = new EnumMap<>(AuditEntityType.class);
        for (AuditEntityType type : AuditEntityType.values()) {
            long c = auditLogRepository.countByEntityType(type);
            if (c > 0) {
                entityTypeCounts.put(type, c);
            }
        }

        Page<AuditSummaryResponse> recentLogs = getAllAuditLogs(pageable);

        return AuditDashboardResponse.builder()
                .totalAuditCount(totalCount)
                .successCount(successCount)
                .failedCount(failedCount)
                .warningCount(warningCount)
                .countByAction(actionCounts)
                .countByEntityType(entityTypeCounts)
                .recentLogs(recentLogs)
                .build();
    }

    private String generateAuditNumber() {
        return "AUD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.ROOT);
    }
}
