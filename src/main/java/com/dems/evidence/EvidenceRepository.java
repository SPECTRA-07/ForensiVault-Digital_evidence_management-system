package com.dems.evidence;

import com.dems.enums.EvidenceStatus;
import com.dems.enums.EvidenceType;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for EvidenceEntity supporting Specifications and dashboard analytics queries.
 */
@Repository
public interface EvidenceRepository extends JpaRepository<EvidenceEntity, Long>, JpaSpecificationExecutor<EvidenceEntity> {

    boolean existsByEvidenceNumber(String evidenceNumber);

    Optional<EvidenceEntity> findByEvidenceNumber(String evidenceNumber);

    Page<EvidenceEntity> findByCaseEntity_Id(Long caseId, Pageable pageable);

    long countByEvidenceType(EvidenceType evidenceType);

    long countByStatus(EvidenceStatus status);

    long countByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);

    @Query("SELECT e.evidenceType, COUNT(e) FROM EvidenceEntity e GROUP BY e.evidenceType")
    List<Object[]> countByEvidenceTypeGroup();

    @Query("SELECT e.status, COUNT(e) FROM EvidenceEntity e GROUP BY e.status")
    List<Object[]> countByStatusGroup();

    List<EvidenceEntity> findTop5ByOrderByFileSizeDesc();

    List<EvidenceEntity> findTop5ByOrderByUploadedAtDesc();

    @Query("SELECT COALESCE(SUM(e.fileSize), 0) FROM EvidenceEntity e")
    long sumFileSize();
}
