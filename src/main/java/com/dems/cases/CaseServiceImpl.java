package com.dems.cases;

import com.dems.audit.AuditService;
import com.dems.enums.AuditAction;
import com.dems.enums.AuditEntityType;
import com.dems.enums.AuditStatus;
import com.dems.enums.CaseStatus;
import com.dems.enums.CrimeSeverity;
import com.dems.enums.CrimeType;
import com.dems.enums.UserRole;
import com.dems.exception.BadRequestException;
import com.dems.exception.ConflictException;
import com.dems.exception.ResourceNotFoundException;
import com.dems.user.UserEntity;
import com.dems.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.Locale;

/**
 * Implementation of CaseService managing case lifecycle, officer assignment rules, and status transitions with audit log tracking.
 */
@Slf4j
@Service
public class CaseServiceImpl implements CaseService {

    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final CaseMapper caseMapper;
    private final CaseStateMachine caseStateMachine;
    private final AuditService auditService;

    public CaseServiceImpl(
            CaseRepository caseRepository,
            UserRepository userRepository,
            CaseMapper caseMapper,
            CaseStateMachine caseStateMachine,
            AuditService auditService) {
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
        this.caseMapper = caseMapper;
        this.caseStateMachine = caseStateMachine;
        this.auditService = auditService;
    }

    @Override
    @Transactional
    public CaseResponse createCase(CaseCreateRequest request) {
        String normalizedCaseNumber = request.getCaseNumber().trim();
        String normalizedCrimeNumber = request.getCrimeNumber().trim();

        if (caseRepository.existsByCaseNumber(normalizedCaseNumber)) {
            log.warn("Case creation failed: Case Number [{}] already exists", normalizedCaseNumber);
            throw new ConflictException("Case number '" + normalizedCaseNumber + "' already exists.");
        }

        if (caseRepository.existsByCrimeNumber(normalizedCrimeNumber)) {
            log.warn("Case creation failed: Crime Number [{}] already exists", normalizedCrimeNumber);
            throw new ConflictException("Crime number '" + normalizedCrimeNumber + "' already exists.");
        }

        if (request.getIncidentDate().isAfter(LocalDate.now())) {
            log.warn("Case creation failed: Incident date [{}] is in the future", request.getIncidentDate());
            throw new BadRequestException("Incident date cannot be in the future.");
        }

        UserEntity assignedOfficer = null;
        if (request.getAssignedOfficerId() != null) {
            assignedOfficer = validateAndFetchPoliceOfficer(request.getAssignedOfficerId());
        }

        CaseEntity entity = CaseEntity.builder()
                .caseNumber(normalizedCaseNumber)
                .crimeNumber(normalizedCrimeNumber)
                .caseName(request.getCaseName().trim())
                .caseSummary(request.getCaseSummary())
                .crimeType(request.getCrimeType())
                .severity(request.getSeverity())
                .status(CaseStatus.OPEN)
                .incidentDate(request.getIncidentDate())
                .crimeSceneLocation(request.getCrimeSceneLocation().trim())
                .investigationStartDate(request.getInvestigationStartDate())
                .assignedOfficer(assignedOfficer)
                .active(true)
                .build();

        CaseEntity savedEntity = caseRepository.save(entity);

        log.info("Case Created: ID [{}], Case Number [{}], Crime Number [{}], Status [{}]",
                savedEntity.getId(), savedEntity.getCaseNumber(), savedEntity.getCrimeNumber(), savedEntity.getStatus());

        auditService.recordEvent(
                AuditAction.CREATE,
                AuditEntityType.CASE,
                "CASE",
                savedEntity.getCaseNumber(),
                savedEntity.getId(),
                AuditStatus.SUCCESS,
                "Created new case: " + savedEntity.getCaseNumber() + " (" + savedEntity.getCaseName() + ")"
        );

        return caseMapper.toResponse(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public CaseResponse getCaseById(Long id) {
        CaseEntity entity = caseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with ID: " + id));
        return caseMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CaseSummaryResponse> getAllCases(Pageable pageable) {
        return caseRepository.findAll(pageable)
                .map(caseMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CaseSummaryResponse> getMyAssignedCases(String userEmail, Pageable pageable) {
        return caseRepository.findByAssignedOfficer_Email(userEmail.trim().toLowerCase(Locale.ROOT), pageable)
                .map(caseMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public CaseResponse updateCase(Long id, CaseUpdateRequest request) {
        CaseEntity entity = caseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with ID: " + id));

        if (request.getIncidentDate().isAfter(LocalDate.now())) {
            log.warn("Case update failed for ID [{}]: Incident date [{}] is in the future", id, request.getIncidentDate());
            throw new BadRequestException("Incident date cannot be in the future.");
        }

        caseStateMachine.validateDates(request.getInvestigationStartDate(), request.getInvestigationEndDate());

        String prevVal = "Name=" + entity.getCaseName() + ", CrimeType=" + entity.getCrimeType() + ", Severity=" + entity.getSeverity();

        entity.setCaseName(request.getCaseName().trim());
        entity.setCaseSummary(request.getCaseSummary());
        entity.setCrimeType(request.getCrimeType());
        entity.setSeverity(request.getSeverity());
        entity.setIncidentDate(request.getIncidentDate());
        entity.setCrimeSceneLocation(request.getCrimeSceneLocation().trim());
        entity.setInvestigationStartDate(request.getInvestigationStartDate());
        entity.setInvestigationEndDate(request.getInvestigationEndDate());

        CaseEntity updatedEntity = caseRepository.save(entity);
        String newVal = "Name=" + updatedEntity.getCaseName() + ", CrimeType=" + updatedEntity.getCrimeType() + ", Severity=" + updatedEntity.getSeverity();

        log.info("Case Updated: ID [{}], Case Number [{}]", updatedEntity.getId(), updatedEntity.getCaseNumber());

        auditService.recordEventWithDiff(
                AuditAction.UPDATE,
                AuditEntityType.CASE,
                "CASE",
                updatedEntity.getCaseNumber(),
                updatedEntity.getId(),
                prevVal,
                newVal,
                AuditStatus.SUCCESS,
                "Updated details for case: " + updatedEntity.getCaseNumber()
        );

        return caseMapper.toResponse(updatedEntity);
    }

    @Override
    @Transactional
    public CaseResponse updateCaseStatus(Long id, CaseStatusUpdateRequest request) {
        CaseEntity entity = caseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with ID: " + id));

        CaseStatus oldStatus = entity.getStatus();
        CaseStatus newStatus = request.getStatus();

        caseStateMachine.validateAndHandleTransition(entity, newStatus);

        if (newStatus == CaseStatus.ARCHIVED) {
            entity.setActive(false);
        }

        CaseEntity updatedEntity = caseRepository.save(entity);

        log.info("Status Changed: Case ID [{}], From [{}] To [{}]", id, oldStatus, newStatus);

        auditService.recordEventWithDiff(
                AuditAction.STATUS_CHANGE,
                AuditEntityType.CASE,
                "CASE",
                updatedEntity.getCaseNumber(),
                updatedEntity.getId(),
                "status=" + oldStatus,
                "status=" + newStatus,
                AuditStatus.SUCCESS,
                "Case status transitioned from " + oldStatus + " to " + newStatus
        );

        return caseMapper.toResponse(updatedEntity);
    }

    @Override
    @Transactional
    public CaseResponse assignOfficer(Long id, CaseAssignOfficerRequest request) {
        CaseEntity entity = caseRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with ID: " + id));

        UserEntity officer = validateAndFetchPoliceOfficer(request.getOfficerId());
        String prevOfficer = entity.getAssignedOfficer() != null ? entity.getAssignedOfficer().getEmail() : "UNASSIGNED";

        entity.setAssignedOfficer(officer);
        CaseEntity updatedEntity = caseRepository.save(entity);

        log.info("Officer Assigned: Case ID [{}], Officer ID [{}], Officer Employee ID [{}]",
                id, officer.getId(), officer.getEmployeeId());

        auditService.recordEventWithDiff(
                AuditAction.ASSIGN,
                AuditEntityType.CASE,
                "CASE",
                updatedEntity.getCaseNumber(),
                updatedEntity.getId(),
                "assignedOfficer=" + prevOfficer,
                "assignedOfficer=" + officer.getEmail(),
                AuditStatus.SUCCESS,
                "Assigned officer " + officer.getEmail() + " to case " + updatedEntity.getCaseNumber()
        );

        return caseMapper.toResponse(updatedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<CaseSummaryResponse> searchCases(
            String caseNumber,
            String crimeNumber,
            String caseName,
            CaseStatus status,
            CrimeType crimeType,
            CrimeSeverity severity,
            Long assignedOfficerId,
            LocalDate incidentDate,
            Boolean active,
            String searchTerm,
            Pageable pageable
    ) {
        Specification<CaseEntity> spec = CaseSpecification.filterCases(
                caseNumber, crimeNumber, caseName, status, crimeType, severity, assignedOfficerId, incidentDate, active, searchTerm
        );

        return caseRepository.findAll(spec, pageable)
                .map(caseMapper::toSummaryResponse);
    }

    private UserEntity validateAndFetchPoliceOfficer(Long officerId) {
        UserEntity officer = userRepository.findById(officerId)
                .orElseThrow(() -> {
                    log.warn("Invalid Assignment Attempt: Officer ID [{}] not found", officerId);
                    return new ResourceNotFoundException("User not found with ID: " + officerId);
                });

        if (!officer.isEnabled()) {
            log.warn("Invalid Assignment Attempt: Officer ID [{}] is deactivated", officerId);
            throw new BadRequestException("Invalid assignment attempt: Assigned officer account is deactivated.");
        }

        if (officer.getRole() != UserRole.POLICE_OFFICER) {
            log.warn("Invalid Assignment Attempt: User ID [{}] has role [{}] instead of POLICE_OFFICER",
                    officerId, officer.getRole());
            throw new BadRequestException("Invalid assignment attempt: User must have POLICE_OFFICER role.");
        }

        return officer;
    }
}
