package com.dems.custody;

import com.dems.entity.BaseEntity;
import com.dems.enums.IntegrityStatus;
import com.dems.enums.TransferPurpose;
import com.dems.enums.TransferStatus;
import com.dems.evidence.EvidenceEntity;
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

import java.time.OffsetDateTime;

/**
 * CustodyRecordEntity representing legally auditable, immutable chain of custody transfer records.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "custody_records")
public class CustodyRecordEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "custody_number", unique = true, nullable = false)
    private String custodyNumber;

    @Column(name = "custody_sequence", nullable = false)
    private Integer custodySequence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evidence_id", nullable = false)
    private EvidenceEntity evidence;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferred_by_id", nullable = false)
    private UserEntity transferredBy;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "transferred_to_id", nullable = false)
    private UserEntity transferredTo;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_status", nullable = false)
    private TransferStatus transferStatus;

    @Enumerated(EnumType.STRING)
    @Column(name = "transfer_purpose", nullable = false)
    private TransferPurpose transferPurpose;

    @Column(name = "transfer_location")
    private String transferLocation;

    @Enumerated(EnumType.STRING)
    @Column(name = "integrity_status_at_transfer")
    private IntegrityStatus integrityStatusAtTransfer;

    @Column(name = "transferred_at", nullable = false)
    private OffsetDateTime transferredAt;

    @Column(name = "accepted_at")
    private OffsetDateTime acceptedAt;

    @Column(name = "transfer_remarks", columnDefinition = "TEXT")
    private String transferRemarks;

    @Column(name = "acceptance_remarks", columnDefinition = "TEXT")
    private String acceptanceRemarks;

    @Builder.Default
    @Column(name = "active", nullable = false)
    private Boolean active = true;
}
