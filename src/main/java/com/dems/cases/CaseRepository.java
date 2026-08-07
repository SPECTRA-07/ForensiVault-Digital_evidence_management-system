package com.dems.cases;

import com.dems.enums.CaseStatus;
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
 * Spring Data JPA Repository for CaseEntity supporting Specifications and dashboard aggregation queries.
 */
@Repository
public interface CaseRepository extends JpaRepository<CaseEntity, Long>, JpaSpecificationExecutor<CaseEntity> {

    boolean existsByCaseNumber(String caseNumber);

    boolean existsByCrimeNumber(String crimeNumber);

    Optional<CaseEntity> findByCaseNumber(String caseNumber);

    Page<CaseEntity> findByAssignedOfficer_Email(String email, Pageable pageable);

    long countByStatus(CaseStatus status);

    long countByCreatedAtBetween(OffsetDateTime start, OffsetDateTime end);

    @Query("SELECT c.status, COUNT(c) FROM CaseEntity c GROUP BY c.status")
    List<Object[]> countByStatusGroup();

    @Query("SELECT c.crimeType, COUNT(c) FROM CaseEntity c GROUP BY c.crimeType")
    List<Object[]> countByCrimeTypeGroup();

    @Query("SELECT c.severity, COUNT(c) FROM CaseEntity c GROUP BY c.severity")
    List<Object[]> countBySeverityGroup();
}
