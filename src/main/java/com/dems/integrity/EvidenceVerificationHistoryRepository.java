package com.dems.integrity;

import com.dems.enums.IntegrityStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Spring Data JPA Repository for EvidenceVerificationHistoryEntity supporting dashboard analytics queries.
 */
@Repository
public interface EvidenceVerificationHistoryRepository extends JpaRepository<EvidenceVerificationHistoryEntity, Long> {

    List<EvidenceVerificationHistoryEntity> findByEvidence_IdOrderByVerifiedAtDesc(Long evidenceId);

    long countByStatus(IntegrityStatus status);

    long countByVerifiedAtBetween(OffsetDateTime start, OffsetDateTime end);

    List<EvidenceVerificationHistoryEntity> findTop5ByOrderByVerifiedAtDesc();
}
