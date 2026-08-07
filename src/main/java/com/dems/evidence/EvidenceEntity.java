package com.dems.evidence;

import com.dems.cases.CaseEntity;
import com.dems.entity.BaseEntity;
import com.dems.enums.EvidenceStatus;
import com.dems.enums.EvidenceType;
import com.dems.user.UserEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;

/**
 * Evidence Entity representing digital evidence file metadata stored in DEMS.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "evidence")
public class EvidenceEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "evidence_number", unique = true, nullable = false)
    private String evidenceNumber;

    @Column(name = "evidence_name", nullable = false)
    private String evidenceName;

    @Column(name = "display_name")
    private String displayName;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "remarks")
    private String remarks;

    @Enumerated(EnumType.STRING)
    @Column(name = "evidence_type", nullable = false)
    private EvidenceType evidenceType;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private EvidenceStatus status;

    @Column(name = "original_file_name", nullable = false)
    private String originalFileName;

    @Column(name = "stored_file_name", nullable = false)
    private String storedFileName;

    @Column(name = "file_extension", nullable = false)
    private String fileExtension;

    @Column(name = "file_size", nullable = false)
    private Long fileSize;

    @Column(name = "mime_type", nullable = false)
    private String mimeType;

    @Column(name = "storage_path", nullable = false)
    private String storagePath;

    @Column(name = "collected_from")
    private String collectedFrom;

    @Column(name = "collection_method")
    private String collectionMethod;

    @Column(name = "collected_at")
    private LocalDateTime collectedAt;

    @Column(name = "collected_by")
    private String collectedBy;

    @Builder.Default
    @Column(name = "download_count", nullable = false)
    private Long downloadCount = 0L;

    @Column(name = "file_hash")
    private String fileHash;

    @Builder.Default
    @Column(name = "hash_algorithm")
    private String hashAlgorithm = "SHA-256";

    @Column(name = "qr_file_name")
    private String qrFileName;

    @Column(name = "qr_download_url")
    private String qrDownloadUrl;

    @Column(name = "qr_generated_at")
    private OffsetDateTime qrGeneratedAt;

    @Column(name = "uploaded_at", nullable = false)
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "uploaded_by_id")
    private UserEntity uploadedBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "current_custodian_id")
    private UserEntity currentCustodian;

    @Column(name = "last_transferred_at")
    private java.time.OffsetDateTime lastTransferredAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "case_id", nullable = false)
    private CaseEntity caseEntity;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
