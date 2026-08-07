package com.dems.custody;

import com.dems.enums.TransferStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Spring Data JPA Repository for CustodyRecordEntity supporting Specifications and dashboard analytics queries.
 */
@Repository
public interface CustodyRecordRepository extends JpaRepository<CustodyRecordEntity, Long>, JpaSpecificationExecutor<CustodyRecordEntity> {

    boolean existsByCustodyNumber(String custodyNumber);

    boolean existsByEvidence_IdAndTransferStatus(Long evidenceId, TransferStatus transferStatus);

    Integer countByEvidence_Id(Long evidenceId);

    List<CustodyRecordEntity> findByEvidence_IdOrderByCustodySequenceAsc(Long evidenceId);

    Page<CustodyRecordEntity> findByEvidence_Id(Long evidenceId, Pageable pageable);

    Optional<CustodyRecordEntity> findByCustodyNumber(String custodyNumber);

    long countByTransferStatus(TransferStatus transferStatus);

    long countByTransferredAtBetween(OffsetDateTime start, OffsetDateTime end);

    List<CustodyRecordEntity> findTop5ByOrderByTransferredAtDesc();

    List<CustodyRecordEntity> findByTransferStatus(TransferStatus status);
}
