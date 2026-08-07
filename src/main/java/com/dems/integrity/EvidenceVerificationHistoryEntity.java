package com.dems.integrity;

import com.dems.entity.BaseEntity;
import com.dems.enums.IntegrityStatus;
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
 * Entity capturing forensic integrity verification audit history for digital evidence records.
 */
@Getter
@Setter
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Entity
@Table(name = "evidence_verification_history")
public class EvidenceVerificationHistoryEntity extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name = "verification_number", unique = true, nullable = false)
    private String verificationNumber;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "evidence_id", nullable = false)
    private EvidenceEntity evidence;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private IntegrityStatus status;

    @Column(name = "stored_hash")
    private String storedHash;

    @Column(name = "current_hash")
    private String currentHash;

    @Column(name = "verified_at", nullable = false)
    private OffsetDateTime verifiedAt;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "verified_by_id")
    private UserEntity verifiedBy;

    @Column(name = "remarks", columnDefinition = "TEXT")
    private String remarks;
}
