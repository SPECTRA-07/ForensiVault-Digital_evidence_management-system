package com.dems.evidence;

import com.dems.audit.AuditService;
import com.dems.cases.CaseEntity;
import com.dems.cases.CaseRepository;
import com.dems.enums.AuditAction;
import com.dems.enums.AuditEntityType;
import com.dems.enums.AuditStatus;
import com.dems.enums.EvidenceStatus;
import com.dems.enums.EvidenceType;
import com.dems.exception.ResourceNotFoundException;
import com.dems.integrity.EvidenceIntegrityService;
import com.dems.storage.StorageService;
import com.dems.user.UserEntity;
import com.dems.user.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Locale;
import java.util.Objects;
import java.util.UUID;

/**
 * Implementation of EvidenceService orchestrating file uploads, validation, storage engine, metadata management, and audit logging.
 */
@Slf4j
@Service
public class EvidenceServiceImpl implements EvidenceService {

    private final EvidenceRepository evidenceRepository;
    private final CaseRepository caseRepository;
    private final UserRepository userRepository;
    private final StorageService storageService;
    private final EvidenceValidationService validationService;
    private final EvidenceMapper evidenceMapper;
    private final EvidenceIntegrityService integrityService;
    private final AuditService auditService;
    private final com.dems.qr.QRCodeService qrCodeService;

    public EvidenceServiceImpl(
            EvidenceRepository evidenceRepository,
            CaseRepository caseRepository,
            UserRepository userRepository,
            StorageService storageService,
            EvidenceValidationService validationService,
            EvidenceMapper evidenceMapper,
            EvidenceIntegrityService integrityService,
            AuditService auditService,
            com.dems.qr.QRCodeService qrCodeService) {
        this.evidenceRepository = evidenceRepository;
        this.caseRepository = caseRepository;
        this.userRepository = userRepository;
        this.storageService = storageService;
        this.validationService = validationService;
        this.evidenceMapper = evidenceMapper;
        this.integrityService = integrityService;
        this.auditService = auditService;
        this.qrCodeService = qrCodeService;
    }

    @Override
    @Transactional
    public EvidenceResponse uploadEvidence(MultipartFile file, EvidenceUploadRequest request, String uploaderEmail) {
        UserEntity uploader = userRepository.findByEmail(uploaderEmail.trim().toLowerCase(Locale.ROOT))
                .orElseThrow(() -> new ResourceNotFoundException("User not found with email: " + uploaderEmail));

        CaseEntity caseEntity = caseRepository.findById(request.getCaseId())
                .orElseThrow(() -> new ResourceNotFoundException("Case not found with ID: " + request.getCaseId()));

        // Validate Case eligibility and File constraints
        validationService.validateCaseEligibility(caseEntity);
        validationService.validateFile(file, request.getEvidenceType());

        // Store file using pluggable StorageService
        String storagePath = storageService.storeFile(file, caseEntity.getCaseNumber());

        String rawOriginalFilename = StringUtils.cleanPath(Objects.requireNonNull(file.getOriginalFilename()));
        String extension = getFileExtension(rawOriginalFilename);
        String storedFileName = Paths.get(storagePath).getFileName().toString();

        String evidenceNumber = generateEvidenceNumber();

        log.info("Evidence Registered: Evidence Number [{}], Case Number [{}]", evidenceNumber, caseEntity.getCaseNumber());

        EvidenceEntity entity = EvidenceEntity.builder()
                .evidenceNumber(evidenceNumber)
                .evidenceName(request.getEvidenceName().trim())
                .displayName(request.getDisplayName() != null ? request.getDisplayName().trim() : request.getEvidenceName().trim())
                .description(request.getDescription())
                .remarks(request.getRemarks())
                .evidenceType(request.getEvidenceType())
                .status(EvidenceStatus.UPLOADED)
                .originalFileName(rawOriginalFilename)
                .storedFileName(storedFileName)
                .fileExtension(extension)
                .fileSize(file.getSize())
                .mimeType(file.getContentType())
                .storagePath(storagePath)
                .collectedFrom(request.getCollectedFrom())
                .collectionMethod(request.getCollectionMethod())
                .collectedAt(request.getCollectedAt())
                .collectedBy(request.getCollectedBy())
                .downloadCount(0L)
                .fileHash(null)
                .uploadedAt(LocalDateTime.now())
                .uploadedBy(uploader)
                .currentCustodian(uploader)
                .caseEntity(caseEntity)
                .active(true)
                .build();

        EvidenceEntity savedEntity = evidenceRepository.save(entity);

        // Compute and store initial cryptographic SHA-256 hash via EvidenceIntegrityService
        integrityService.computeAndStoreInitialHash(savedEntity);
        savedEntity = evidenceRepository.save(savedEntity);

        // Generate physical QR Code barcode image
        try {
            qrCodeService.generateQRCode(savedEntity);
        } catch (Exception e) {
            log.warn("Could not generate QR Code for Evidence ID [{}] during upload", savedEntity.getId(), e);
        }

        log.info("Evidence Uploaded: ID [{}], Evidence Number [{}], File Size [{} bytes], Storage Path [{}]",
                savedEntity.getId(), savedEntity.getEvidenceNumber(), savedEntity.getFileSize(), savedEntity.getStoragePath());

        auditService.recordEvent(
                AuditAction.UPLOAD,
                AuditEntityType.EVIDENCE,
                "EVIDENCE",
                savedEntity.getEvidenceNumber(),
                savedEntity.getId(),
                AuditStatus.SUCCESS,
                "Uploaded evidence file: " + savedEntity.getOriginalFileName() + " (" + savedEntity.getFileSize() + " bytes)"
        );

        return evidenceMapper.toResponse(savedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public EvidenceResponse getEvidenceById(Long id) {
        EvidenceEntity entity = evidenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence record not found with ID: " + id));
        return evidenceMapper.toResponse(entity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EvidenceSummaryResponse> getAllEvidence(Pageable pageable) {
        return evidenceRepository.findAll(pageable)
                .map(evidenceMapper::toSummaryResponse);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EvidenceSummaryResponse> getEvidenceByCaseId(Long caseId, Pageable pageable) {
        if (!caseRepository.existsById(caseId)) {
            throw new ResourceNotFoundException("Case not found with ID: " + caseId);
        }
        return evidenceRepository.findByCaseEntity_Id(caseId, pageable)
                .map(evidenceMapper::toSummaryResponse);
    }

    @Override
    @Transactional
    public Resource downloadEvidence(Long id) {
        EvidenceEntity entity = evidenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence record not found with ID: " + id));

        Resource resource = storageService.loadFileAsResource(entity.getStoragePath());

        // Increment download counter
        entity.setDownloadCount(entity.getDownloadCount() + 1);
        evidenceRepository.save(entity);

        log.info("Evidence Downloaded: ID [{}], Evidence Number [{}], New Download Count [{}]",
                entity.getId(), entity.getEvidenceNumber(), entity.getDownloadCount());

        auditService.recordEvent(
                AuditAction.DOWNLOAD,
                AuditEntityType.EVIDENCE,
                "EVIDENCE",
                entity.getEvidenceNumber(),
                entity.getId(),
                AuditStatus.SUCCESS,
                "Downloaded evidence file: " + entity.getOriginalFileName()
        );

        return resource;
    }

    @Override
    @Transactional
    public EvidenceResponse updateEvidence(Long id, EvidenceUpdateRequest request) {
        EvidenceEntity entity = evidenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence record not found with ID: " + id));

        validationService.validateEvidenceModification(entity);

        String prevDetails = "Name=" + entity.getEvidenceName() + ", Type=" + entity.getEvidenceType();

        entity.setEvidenceName(request.getEvidenceName().trim());
        entity.setDisplayName(request.getDisplayName() != null ? request.getDisplayName().trim() : request.getEvidenceName().trim());
        entity.setDescription(request.getDescription());
        entity.setRemarks(request.getRemarks());
        entity.setEvidenceType(request.getEvidenceType());
        entity.setCollectedFrom(request.getCollectedFrom());
        entity.setCollectionMethod(request.getCollectionMethod());
        entity.setCollectedAt(request.getCollectedAt());
        entity.setCollectedBy(request.getCollectedBy());

        EvidenceEntity updatedEntity = evidenceRepository.save(entity);
        String newDetails = "Name=" + updatedEntity.getEvidenceName() + ", Type=" + updatedEntity.getEvidenceType();

        log.info("Evidence Updated: ID [{}], Evidence Number [{}]", updatedEntity.getId(), updatedEntity.getEvidenceNumber());

        auditService.recordEventWithDiff(
                AuditAction.UPDATE,
                AuditEntityType.EVIDENCE,
                "EVIDENCE",
                updatedEntity.getEvidenceNumber(),
                updatedEntity.getId(),
                prevDetails,
                newDetails,
                AuditStatus.SUCCESS,
                "Updated metadata for evidence: " + updatedEntity.getEvidenceNumber()
        );

        return evidenceMapper.toResponse(updatedEntity);
    }

    @Override
    @Transactional
    public EvidenceResponse updateEvidenceStatus(Long id, EvidenceStatusUpdateRequest request) {
        EvidenceEntity entity = evidenceRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Evidence record not found with ID: " + id));

        validationService.validateEvidenceModification(entity);

        EvidenceStatus oldStatus = entity.getStatus();
        EvidenceStatus newStatus = request.getStatus();

        entity.setStatus(newStatus);
        if (request.getRemarks() != null && !request.getRemarks().trim().isEmpty()) {
            entity.setRemarks(request.getRemarks().trim());
        }

        if (newStatus == EvidenceStatus.ARCHIVED) {
            entity.setActive(false);
        }

        EvidenceEntity updatedEntity = evidenceRepository.save(entity);

        log.info("Status Changed: Evidence ID [{}], From [{}] To [{}]", id, oldStatus, newStatus);

        auditService.recordEventWithDiff(
                AuditAction.STATUS_CHANGE,
                AuditEntityType.EVIDENCE,
                "EVIDENCE",
                updatedEntity.getEvidenceNumber(),
                updatedEntity.getId(),
                "status=" + oldStatus,
                "status=" + newStatus,
                AuditStatus.SUCCESS,
                "Evidence status transitioned from " + oldStatus + " to " + newStatus
        );

        return evidenceMapper.toResponse(updatedEntity);
    }

    @Override
    @Transactional(readOnly = true)
    public Page<EvidenceSummaryResponse> searchEvidence(
            EvidenceStatus status,
            EvidenceType evidenceType,
            Long caseId,
            Long uploadedById,
            LocalDate uploadDate,
            String evidenceNumber,
            String evidenceName,
            String searchTerm,
            Boolean active,
            Pageable pageable
    ) {
        Specification<EvidenceEntity> spec = EvidenceSpecification.filterEvidence(
                status, evidenceType, caseId, uploadedById, uploadDate, evidenceNumber, evidenceName, searchTerm, active
        );

        return evidenceRepository.findAll(spec, pageable)
                .map(evidenceMapper::toSummaryResponse);
    }

    private String generateEvidenceNumber() {
        return "EVD-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase(Locale.ROOT);
    }

    private String getFileExtension(String filename) {
        int dotIndex = filename.lastIndexOf('.');
        return (dotIndex == -1) ? "" : filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
    }
}
