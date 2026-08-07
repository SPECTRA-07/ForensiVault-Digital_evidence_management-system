package com.dems.audit;

import com.dems.enums.AuditAction;
import com.dems.enums.AuditEntityType;
import com.dems.enums.AuditStatus;
import jakarta.persistence.criteria.Predicate;
import org.springframework.data.jpa.domain.Specification;

import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

/**
 * Utility specification builder for dynamic multi-criteria searching and filtering on Audit Log records.
 */
public class AuditLogSpecification {

    private AuditLogSpecification() {
        throw new UnsupportedOperationException("Utility class should not be instantiated");
    }

    public static Specification<AuditLogEntity> filterAuditLogs(
            AuditAction action,
            AuditEntityType entityType,
            String moduleName,
            String username,
            AuditStatus status,
            OffsetDateTime startDate,
            OffsetDateTime endDate,
            String entityReference,
            String ipAddress,
            String correlationId,
            Boolean active
    ) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();

            if (action != null) {
                predicates.add(criteriaBuilder.equal(root.get("action"), action));
            }

            if (entityType != null) {
                predicates.add(criteriaBuilder.equal(root.get("entityType"), entityType));
            }

            if (moduleName != null && !moduleName.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("moduleName"), moduleName.trim()));
            }

            if (username != null && !username.trim().isEmpty()) {
                String pattern = "%" + username.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("username")), pattern));
            }

            if (status != null) {
                predicates.add(criteriaBuilder.equal(root.get("status"), status));
            }

            if (startDate != null) {
                predicates.add(criteriaBuilder.greaterThanOrEqualTo(root.get("actionTimestamp"), startDate));
            }

            if (endDate != null) {
                predicates.add(criteriaBuilder.lessThanOrEqualTo(root.get("actionTimestamp"), endDate));
            }

            if (entityReference != null && !entityReference.trim().isEmpty()) {
                String pattern = "%" + entityReference.trim().toLowerCase() + "%";
                predicates.add(criteriaBuilder.like(criteriaBuilder.lower(root.get("entityReference")), pattern));
            }

            if (ipAddress != null && !ipAddress.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("ipAddress"), ipAddress.trim()));
            }

            if (correlationId != null && !correlationId.trim().isEmpty()) {
                predicates.add(criteriaBuilder.equal(root.get("correlationId"), correlationId.trim()));
            }

            if (active != null) {
                predicates.add(criteriaBuilder.equal(root.get("active"), active));
            }

            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }
}
