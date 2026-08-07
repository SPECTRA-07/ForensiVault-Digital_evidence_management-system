package com.dems.audit;

import com.dems.enums.AuditAction;
import com.dems.enums.AuditEntityType;
import com.dems.enums.AuditStatus;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

/**
 * Spring Data JPA Repository for AuditLogEntity supporting Specification searching and dashboard aggregation queries.
 */
@Repository
public interface AuditLogRepository extends JpaRepository<AuditLogEntity, Long>, JpaSpecificationExecutor<AuditLogEntity> {

    boolean existsByAuditNumber(String auditNumber);

    Page<AuditLogEntity> findByPerformedBy_Id(Long userId, Pageable pageable);

    Page<AuditLogEntity> findByEntityTypeAndEntityId(AuditEntityType entityType, Long entityId, Pageable pageable);

    long countByStatus(AuditStatus status);

    long countByAction(AuditAction action);

    long countByEntityType(AuditEntityType entityType);

    long countByActionTimestampAfter(OffsetDateTime timestamp);

    long countByActionTimestampBetween(OffsetDateTime start, OffsetDateTime end);

    @Query("SELECT a.action, COUNT(a) FROM AuditLogEntity a GROUP BY a.action")
    List<Object[]> countByActionGroup();

    @Query("SELECT a.moduleName, COUNT(a) FROM AuditLogEntity a GROUP BY a.moduleName")
    List<Object[]> countByModuleNameGroup();

    @Query("SELECT a.status, COUNT(a) FROM AuditLogEntity a GROUP BY a.status")
    List<Object[]> countByStatusGroup();

    @Query("SELECT a.username, COUNT(a) FROM AuditLogEntity a GROUP BY a.username ORDER BY COUNT(a) DESC")
    List<Object[]> findTopActiveUsers(Pageable pageable);

    List<AuditLogEntity> findTop5ByOrderByActionTimestampDesc();
}
